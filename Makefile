.PHONY: help build run stop up down logs shell clean test docker-build docker-run docker-stop docker-logs restart dev prod auth-service infra-up infra-down infra-logs

# Default target
help: ## Show this help message
	@echo "Gurukul Tutor - Microservices Monorepo"
	@echo ""
	@echo "Available commands:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

# Java/Maven commands (default to auth-service)
build: ## Build the project (compile and package)
	cd auth-service && ./mvnw clean package -DskipTests

test: ## Run unit tests
	cd auth-service && ./mvnw test

run: ## Run the application locally (without Docker)
	cd auth-service && ./mvnw spring-boot:run

# Infrastructure commands
infra-up: ## Start infrastructure services (PostgreSQL)
	docker compose -f infra/docker-compose.yml up -d

infra-down: ## Stop infrastructure services (PostgreSQL)
	docker compose -f infra/docker-compose.yml down

infra-logs: ## View infrastructure logs
	docker compose -f infra/docker-compose.yml logs -f

# Docker commands
docker-build: ## Build Docker image (auth-service)
	docker build -t gurukul/auth-service:latest -f auth-service/Dockerfile .

docker-run: infra-up ## Run services in Docker (starts containers in detached mode)
	docker compose -f apps/docker-compose.yml up -d

docker-stop: ## Stop app services only
	docker compose -f apps/docker-compose.yml down

docker-logs: ## View app container logs
	docker compose -f apps/docker-compose.yml logs -f

docker-shell: ## Open a shell in the auth-service container
	docker exec -it auth-service sh

# Combined commands
up: docker-build docker-run ## Build and run Docker containers (infra + app)
	@echo "Auth Service is starting..."
	@echo "API Available at: http://localhost:8080"
	@echo "Health check at: http://localhost:8080/actuator/health"

down: docker-stop infra-down ## Stop and remove all containers (app + infra)

# Cleanup
clean: ## Clean build artifacts
	cd auth-service && ./mvnw clean

# Development helpers
restart: down up ## Restart Docker containers (clean start)
dev: build run ## Build and run locally for development

# Production
prod: ## Build for production (skip tests, optimized)
	cd auth-service && ./mvnw clean package -DskipTests -Pproduction

# Service-specific helpers
auth-service: ## Build and run auth-service
	$(MAKE) up

# Git operations
git-status: ## Show git status
	git status

git-commit: ## Commit changes with message (e.g., make git-commit MSG="fix bug")
ifndef MSG
	$(error MSG is required. Usage: make git-commit MSG="your message")
endif
	git add . && git commit -m "$(MSG)"

git-push: ## Push to remote
	git push origin main