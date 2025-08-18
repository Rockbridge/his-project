# Multi-Stage Docker Build for Identity Service
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x ./mvnw || true
RUN ./mvnw dependency:go-offline -B || mvn dependency:go-offline -B
COPY src src
RUN ./mvnw package -DskipTests -B || mvn package -DskipTests -B

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN groupadd -g 1001 appgroup && useradd -r -u 1001 -g appgroup appuser
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar && mkdir -p logs && chown appuser:appgroup logs
USER appuser
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8083/actuator/health || exit 1
EXPOSE 8083
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0"
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
LABEL maintainer="PVS Team"
LABEL description="Identity and Authorization Service for HIS"
LABEL version="1.0.0"
