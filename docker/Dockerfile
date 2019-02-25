FROM maven:3.3-jdk-8-alpine as build

WORKDIR /build

COPY pom.xml pom.xml
COPY src/main src/main
COPY src/test src/test

RUN mvn clean package

# We derive the final image from LaMachine to get Frog to save us the trouble
# of establishing a shared port between two containers.
#
# LaMachine is *HUGE* (> 5GB). If space is an issue, we can instead derive
# from Ubuntu and apt install frog, but then we might get an outdated version.
FROM proycon/lamachine

WORKDIR /opt/rananostra
COPY --from=build /build/target/appassembler/ .
COPY docker/config.yml config.yml
COPY docker/start.sh bin/start.sh

EXPOSE 8080 8081

CMD bin/start.sh
