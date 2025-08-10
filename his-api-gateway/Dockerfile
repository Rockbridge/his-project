# Multi-Stage Docker Build for API Gateway
FROM eclipse-temurin:21-jdk AS build

# Set working directory
WORKDIR /app

# Copy Maven configuration
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Make mvnw executable (for compatibility)
RUN chmod +x ./mvnw || true

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B || mvn dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN ./mvnw package -DskipTests -B || mvn package -DskipTests -B

# Production Stage - Use standard JRE (not alpine)
FROM eclipse-temurin:21-jre

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create application user for security
RUN groupadd -g 1001 appgroup && \
    useradd -r -u 1001 -g appgroup appuser

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to appuser
RUN chown appuser:appgroup app.jar

# Create logs directory
RUN mkdir -p logs && chown appuser:appgroup logs

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0"
ENV SPRING_PROFILES_ACTIVE=docker

# Start application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels for metadata
LABEL maintainer="PVS Team"
LABEL description="API Gateway for Hospital Information System"
LABEL version="1.0.0"