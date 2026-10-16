
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build
COPY . .

RUN mvn -B -ntp -pl comitas-core -am package \
    && cp comitas-core/target/comitas-core-*.jar /tmp/server.jar

# Create a minimal Java 25 runtime
FROM eclipse-temurin:25-jdk-alpine AS jre-builder

WORKDIR /srv
COPY --from=builder /tmp/server.jar /srv/server.jar

# Detect required Java modules and build a trimmed runtime
RUN set -eu; \
    MODULES="$(jdeps \
        --multi-release 25 \
        --ignore-missing-deps \
        --print-module-deps \
        /srv/server.jar)" \
    && echo "Detected modules: $MODULES" \
    && jlink \
        --add-modules "${MODULES},jdk.crypto.ec" \
        --strip-debug \
        --no-man-pages \
        --no-header-files \
        --compress=2 \
        --output /opt/java-minimal

# Minimal runtime image
FROM alpine:3.24.2

WORKDIR /srv

RUN apk -U upgrade \
    && apk add --no-cache ca-certificates \
    && rm -rf /var/cache/apk/*

COPY --from=jre-builder /opt/java-minimal /opt/java
COPY --from=jre-builder /srv/server.jar /srv/server.jar

RUN touch /srv/server.properties /srv/tokens.secret

CMD ["/opt/java/bin/java", "--enable-native-access=ALL-UNNAMED", "-jar", "server.jar"]
