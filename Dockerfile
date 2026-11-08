# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B || true

# Copy source code
COPY src src

# Build the application (skip tests for faster builds)
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/index.html || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
