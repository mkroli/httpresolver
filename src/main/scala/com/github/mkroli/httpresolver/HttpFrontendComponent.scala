/*
 * Copyright 2014 Michael Krolikowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mkroli.httpresolver

import akka.actor.Props
import akka.io.IO
import akka.pattern.ask
import spray.can.Http
import spray.http.StatusCodes
import spray.routing.ExceptionHandler
import spray.routing.HttpServiceActor

trait HttpFrontendComponent {
  self: AkkaComponent with DnsHandlerComponent =>

  lazy val httpFrontendActor = actorSystem.actorOf(Props(new HttpFrontendActor))

  IO(Http)(actorSystem) ? Http.Bind(
    httpFrontendActor,
    interface = "0.0.0.0",
    port = 8080)

  class HttpFrontendActor extends HttpServiceActor {
    import context.dispatcher

    implicit def exceptionHandler = ExceptionHandler {
      case _: NoSuchElementException => ctx => ctx.complete(StatusCodes.NotFound)
    }

    val urlRegex = "/(?:(https?)://)?([^/]+)(/.*)?".r

    override def receive = runRoute {
      get {
        requestUri { uri =>
          val (proto, host, path) = uri.path.toString match {
            case urlRegex(protocol, host, path) =>
              (Option(protocol).getOrElse("http"), host, Option(path).getOrElse(""))
          }
          val query = uri.query.toString match {
            case s if s.isEmpty => s
            case s => s"?$s"
          }
          onSuccess((dnsHandlerActor ? DnsRequest(host)).mapTo[DnsResponse]) {
            case DnsResponse(addr) =>
              redirect(s"${proto}://${addr}${path}${query}", StatusCodes.TemporaryRedirect)
          }
        }
      }
    }
  }
}
