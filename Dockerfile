FROM maven:3.3-jdk-8-alpine as javabuild

WORKDIR /build

COPY pom.xml pom.xml
COPY src/main src/main
COPY src/test src/test

RUN mvn clean package


# We build Frog from source so that we can disable its multithreading (OpenMP)
# entirely. It seems like Frog does not always keep to its promise of not
# multi-threading in server mode, causing multiple spawned Frog processes to
# compete for CPU time and memory.
FROM debian:stretch-slim as frogbuild

RUN apt-get update && \
    apt-get -y install \
        g++ libboost-regex-dev libbz2-dev libexttextcat-dev libicu-dev \
        libtar-dev libtool libxml2-dev make pkg-config wget zlib1g-dev

WORKDIR /build
COPY docker/download.sh .
COPY docker/downloads.txt .
RUN ./download.sh
COPY docker/build.sh .
RUN ./build.sh


FROM openjdk:8-jre-slim-stretch

RUN apt-get update && \
    apt-get -y install libboost-regex1.62.0 libexttextcat-2.0-0 libtar0 libxml2 && \
    rm -rf /var/lib/apt/lists

WORKDIR /opt/rananostra
COPY --from=javabuild /build/target/appassembler/ .
COPY --from=frogbuild /usr/local /usr/local
COPY docker docker

EXPOSE 8080 8081

CMD docker/start.sh
