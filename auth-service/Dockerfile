# Use a multi-stage build to reduce image size
# Stage 1: Build the application
FROM eclipse-temurin:27-jdk-alpine AS builder

# Install Maven
RUN apk add --no-cache curl autoconf bison texinfo gawk gcc git libtool make musl-dev

WORKDIR /build

# Copy Maven wrapper and pom.xml (paths relative to build context = project root)
COPY auth-service/mvnw .
COPY auth-service/mvnw.cmd .
COPY auth-service/.mvn .mvn
COPY auth-service/pom.xml .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY auth-service/src src

# Build the JAR file
RUN ./mvnw package -DskipTests -B

# Stage 2: Runtime image
FROM eclipse-temurin:27-jre-alpine

# Create non-root user
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

# Create app directory
RUN mkdir -p /app && chown appuser:appuser /app
WORKDIR /app

# Copy the Spring Boot fat JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Run using java directly
ENTRYPOINT ["java", "-jar", "/app/app.jar"]