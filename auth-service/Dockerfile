# syntax=docker/dockerfile:1

# ==============================================================================
# Stage 1: Build and extract layered JAR
# ==============================================================================
FROM eclipse-temurin:27-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and pom.xml first to leverage Docker layer caching
COPY auth-service/mvnw .
COPY auth-service/mvnw.cmd .
COPY auth-service/.mvn .mvn
COPY auth-service/pom.xml .

# Ensure mvnw has executable permissions (prevents permission denied errors)
RUN chmod +x mvnw

# Download dependencies using BuildKit cache mount for /root/.m2
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -B

# Copy application source code
COPY auth-service/src src

# Build the fat JAR and extract Spring Boot layers using unified jarmode=tools
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests -B && \
    java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination extracted

# ==============================================================================
# Stage 2: Minimal Runtime Image
# ==============================================================================
FROM eclipse-temurin:27-jre-alpine

# Install curl in the runtime image so the Docker Compose healthcheck succeeds
RUN apk add --no-cache curl && \
    addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

WORKDIR /app
RUN chown appuser:appuser /app

# Copy extracted Spring Boot layers in order from least-frequently to most-frequently changed
COPY --from=builder --chown=appuser:appuser /build/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appuser /build/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appuser /build/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appuser /build/extracted/application/ ./

USER appuser

EXPOSE 8080

# Container-aware JVM flags + fast startup flags
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Launch via extracted Spring Boot JarLauncher
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]