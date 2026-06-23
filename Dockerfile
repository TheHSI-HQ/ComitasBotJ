# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Build comitas-api first
COPY ./comitas-api ./comitas-api
WORKDIR /build/comitas-api
RUN mvn clean install

# Build comitas-core
WORKDIR /build
COPY ./comitas-core ./comitas-core
WORKDIR /build/comitas-core
RUN mvn clean package

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /srv

# Copy the built JAR
COPY --from=builder /build/comitas-core/target/comitas-core-*.jar /srv/server.jar

# Create empty configuration file
RUN touch /srv/server.properties
RUN touch /srv/tokens.secret

CMD ["java", "-jar", "server.jar"]
