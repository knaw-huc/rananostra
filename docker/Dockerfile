FROM maven:3.3-jdk-8-alpine as build

WORKDIR /build

COPY pom.xml pom.xml
COPY src/main src/main
COPY src/test src/test

RUN mvn clean package

# We derive the final image from Debian with Frog installed to save us the
# trouble of establishing a shared port between two containers.
#
# Replace this with an OpenJDK base image once one using Debian buster has
# been released. Only buster has Frog 0.15.
FROM debian:buster-slim

# mkdir: https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=863199
RUN apt-get update && \
    mkdir -p /usr/share/man/man1 && \
    apt-get -y install frog frogdata openjdk-8-jre-headless && \
    rm -rf /var/lib/apt/lists

WORKDIR /opt/rananostra
COPY --from=build /build/target/appassembler/ .
COPY docker docker

EXPOSE 8080 8081

CMD docker/start.sh
