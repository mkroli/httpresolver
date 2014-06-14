FROM dockerfile/java
MAINTAINER mkroli

ADD build.sbt /tmp/build/build.sbt
ADD project/plugins.sbt /tmp/build/project/plugins.sbt
ADD src /tmp/build/src

WORKDIR /tmp/build
RUN wget http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.2/sbt-launch.jar; \
    java -XX:MaxPermSize=128m -jar sbt-launch.jar pack-archive; \
    mkdir -p /opt/httpresolver; \
    tar -xzv -C /opt/httpresolver --strip-components=1 -f /tmp/build/target/httpresolver*.tar.gz; \
    rm -rf /tmp/build /root/.sbt /root/.ivy2
WORKDIR /opt/httpresolver

EXPOSE 8080

ENTRYPOINT ["/opt/httpresolver/bin/httpresolver"]
