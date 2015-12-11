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

import java.net._

import com.github.mkroli.dns4s.akka.Dns
import com.github.mkroli.dns4s.dsl._
import com.github.mkroli.dns4s.section.ResourceRecord

import akka.actor._
import akka.io.IO
import akka.pattern._

trait DnsHandlerComponent {
  self: AkkaComponent =>

  case class DnsRequest(question: String)
  case class DnsResponse(address: Inet4Address)

  lazy val dnsHandlerActor = actorSystem.actorOf(Props(new DnsHandlerActor))

  lazy val dnsActor = IO(Dns)(actorSystem)

  dnsActor ? Dns.Bind(
    dnsHandlerActor,
    port = 0)

  class DnsHandlerActor extends Actor {
    import context.dispatcher

    object FirstARecord {
      def unapply(l: Seq[ResourceRecord]) = l.collectFirst {
        case ARecord(a) => a
      }
    }

    override def receive = {
      case DnsRequest(question) =>
        (dnsActor ? Dns.DnsPacket(Query ~ Questions(QName(question)), new InetSocketAddress("8.8.8.8", 53))).collect {
          case Response(_) ~ Answers(FirstARecord(a)) => DnsResponse(a.address)
        }.pipeTo(sender)
    }
  }
}
