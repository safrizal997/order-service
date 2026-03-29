# ============================================
# Order Service - Dockerfile
# ============================================
# Multi-stage build: Maven build → lightweight JRE runtime
# Usage: docker build -t payment-service .
# ============================================

# --- Stage 1: Build ---
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper dan pom.xml dulu (cache dependency layer)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (layer ini di-cache selama pom.xml tidak berubah)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build tanpa test
RUN ./mvnw package -DskipTests -B

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Security: jalankan sebagai non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy JAR dari build stage
COPY --from=builder /app/target/*.jar app.jar

# Set ownership
RUN chown appuser:appgroup app.jar

USER appuser

# Spring Boot default port
EXPOSE 8080

# JVM tuning untuk container
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
