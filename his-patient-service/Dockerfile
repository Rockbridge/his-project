FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline -B

# Build application
COPY src src
RUN ./mvnw package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root user
RUN groupadd -g 1001 appgroup && \
    useradd -r -u 1001 -g appgroup appuser

# Copy application
COPY --from=build /app/target/*.jar app.jar

# Set ownership
RUN chown appuser:appgroup app.jar

# Switch to non-root user
USER appuser

# Health check - KORRIGIERTER PORT!
HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# KORRIGIERTER PORT!
EXPOSE 8081

# KORRIGIERTES ENTRYPOINT!
ENTRYPOINT ["java", "-jar", "app.jar"]
