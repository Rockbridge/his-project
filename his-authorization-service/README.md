# HIS Authorization Service

Skeleton microservice responsible for context-aware RBAC policy evaluation and rule management.

## Features
- Spring Boot 3.3 with Actuator and OpenAPI
- Prepared for PostgreSQL schema `his_authorization`
- Ready for Docker deployment on port 8084
- Dockerfile uses multi-architecture base images (amd64/arm64)

## Build
```bash
./mvnw -DskipTests package
```

## Run
```bash
java -jar target/*.jar
```
