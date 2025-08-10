# Makefile
.PHONY: help build test run-dev stop clean

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $1, $2}' $(MAKEFILE_LIST)

build: ## Build the application
	./mvnw clean package

test: ## Run tests
	./mvnw test

run-dev: ## Start development environment
	chmod +x start-dev.sh
	./start-dev.sh

stop: ## Stop all containers
	docker-compose -f docker-compose.dev.yml down

clean: ## Clean build artifacts and containers
	./mvnw clean
	docker-compose -f docker-compose.dev.yml down -v
	docker system prune -f

db-migrate: ## Run database migrations
	./mvnw flyway:migrate

db-reset: ## Reset database
	docker-compose -f docker-compose.dev.yml down -v postgres
	docker-compose -f docker-compose.dev.yml up -d postgres
	sleep 10
	./mvnw flyway:migrate

logs: ## Show application logs
	docker-compose -f docker-compose.dev.yml logs -f encounter-service

api-docs: ## Open API documentation
	open http://localhost:8081/swagger-ui.html
