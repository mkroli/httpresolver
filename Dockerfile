FROM java:openjdk-8-jre
MAINTAINER mkroli

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64

ADD https://raw.githubusercontent.com/paulp/sbt-extras/master/sbt /usr/bin/sbt
RUN chmod 0755 /usr/bin/sbt

ADD build.sbt /tmp/build/build.sbt
ADD project/plugins.sbt /tmp/build/project/plugins.sbt
ADD src /tmp/build/src

WORKDIR /tmp/build
RUN sbt packArchiveTgz && \
    mkdir -p /opt/httpresolver && \
    tar -xz -C /opt/httpresolver --strip-components=1 -f /tmp/build/target/httpresolver*.tar.gz; \
    rm -rf /tmp/build /root/.sbt /root/.ivy2

WORKDIR /opt/httpresolver
EXPOSE 8080
ENTRYPOINT /opt/httpresolver/bin/httpresolver
