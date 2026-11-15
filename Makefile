# =====================================================================
# Gurukul Tutor - Microservices Monorepo Management Makefile
# =====================================================================
# Supports individual microservices, infrastructure, global commands,
# and composable multi-target execution.

# Auto-detect microservices with pom.xml (excluding target directories)
DETECTED_SERVICES := $(filter-out target%,$(notdir $(patsubst %/,%,$(dir $(wildcard */pom.xml)))))
SERVICES ?= $(if $(DETECTED_SERVICES),$(DETECTED_SERVICES),auth-service)

# Default target
.DEFAULT_GOAL := help

# ---------------------------------------------------------------------
# Help & Documentation
# ---------------------------------------------------------------------
help: ## Show this help message
	@echo "====================================================================="
	@echo " Gurukul Tutor - Monorepo Microservices Management"
	@echo " Detected Microservices: $(SERVICES)"
	@echo "====================================================================="
	@echo ""
	@echo "GLOBAL COMMANDS (Affects all services and/or infrastructure):"
	@awk 'BEGIN {FS = ":.*?## "} !/MAKEFILE_LIST/ && index($$2, "[GLOBAL]") {printf "  \033[36m%-25s\033[0m %s\n", $$1, substr($$2, 10)}' $(MAKEFILE_LIST) | sort
	@echo ""
	@echo "INFRASTRUCTURE COMMANDS (PostgreSQL & Keycloak):"
	@awk 'BEGIN {FS = ":.*?## "} !/MAKEFILE_LIST/ && index($$2, "[INFRA]") {printf "  \033[36m%-25s\033[0m %s\n", $$1, substr($$2, 9)}' $(MAKEFILE_LIST) | sort
	@echo ""
	@echo "MICROSERVICE COMMANDS (Per-service lifecycle: <service>-<action>):"
	@printf "  \033[36m%-25s\033[0m %s\n" "<service>-up" "Build and start service container in Docker"
	@printf "  \033[36m%-25s\033[0m %s\n" "<service>-down" "Stop and remove service container"
	@printf "  \033[36m%-25s\033[0m %s\n" "<service>-restart" "Clean restart of service container"
	@printf "  \033[36m%-25s\033[0m %s\n" "<service>-build" "Package jar and build Docker image for service"
	@printf "  \033[36m%-25s\033[0m %s\n" "<service>-test" "Run Testcontainers integration & unit tests"
	@printf "  \033[36m%-25s\033[0m %s\n" "<service>-clean" "Clean build artifacts for service"
	@printf "  \033[36m%-25s\033[0m %s\n" "<service>-run" "Run service locally with Spring Boot"
	@printf "  \033[36m%-25s\033[0m %s\n" "<service>-logs" "Stream live logs for service container"
	@printf "  \033[36m%-25s\033[0m %s\n" "<service>" "Shortcut for <service>-up (e.g. make auth-service)"
	@echo ""
	@echo "COMPOSITION EXAMPLES:"
	@echo "  make auth-service infra up     Start both auth-service and infra"
	@echo "  make up                        Start infra + all detected microservices"
	@echo "  make down                      Stop all microservices + infra"
	@echo "  make test                      Run tests for all microservices"
	@echo "  make test SERVICE=auth-service Run tests for a specific service"
	@echo ""
	@echo "HELPERS & TOOLS:"
	@awk 'BEGIN {FS = ":.*?## "} !/MAKEFILE_LIST/ && index($$2, "[TOOL]") {printf "  \033[36m%-25s\033[0m %s\n", $$1, substr($$2, 8)}' $(MAKEFILE_LIST) | sort
	@echo "====================================================================="

# ---------------------------------------------------------------------
# Global Commands (Multi-service & Full Stack)
# ---------------------------------------------------------------------
up: infra-up apps-up ## [GLOBAL] Start full stack (infra + all microservices)
	@echo "---------------------------------------------------------------------"
	@echo "Stack is running:"
	@echo "  Keycloak:     http://localhost:8081 (Admin: admin/admin)"
	@echo "  Auth Service: http://localhost:8080"
	@echo "  Health Check: http://localhost:8080/actuator/health"
	@echo "---------------------------------------------------------------------"

down: apps-down infra-down ## [GLOBAL] Stop and remove all containers (apps + infra)

restart: down up ## [GLOBAL] Clean restart of the entire stack

build: ## [GLOBAL] Build all microservices (or pass SERVICE=<name>)
ifdef SERVICE
	cd $(SERVICE) && ./mvnw clean package -DskipTests
	docker compose -f apps/docker-compose.yml build $(SERVICE)
else
	@for svc in $(SERVICES); do \
		echo "=== Building $$svc ==="; \
		(cd $$svc && ./mvnw clean package -DskipTests) || exit 1; \
	done
	docker compose -f apps/docker-compose.yml build
endif

test: ## [GLOBAL] Run tests for all microservices (or pass SERVICE=<name>)
ifdef SERVICE
	cd $(SERVICE) && ./mvnw test
else
	@for svc in $(SERVICES); do \
		echo "=== Running tests for $$svc ==="; \
		(cd $$svc && ./mvnw test) || exit 1; \
	done
endif

clean: ## [GLOBAL] Clean build targets for all microservices (or pass SERVICE=<name>)
ifdef SERVICE
	cd $(SERVICE) && ./mvnw clean
else
	@for svc in $(SERVICES); do \
		echo "=== Cleaning $$svc ==="; \
		(cd $$svc && ./mvnw clean) || exit 1; \
	done
endif

ps: ## [GLOBAL] Check status of all containers (infra + applications)
	@echo "=== Infrastructure Containers ==="
	@docker compose -f infra/docker-compose.yml ps
	@echo ""
	@echo "=== Application Containers ==="
	@docker compose -f apps/docker-compose.yml ps

health: ## [GLOBAL] Query auth-service health endpoint
	@curl -s http://localhost:8080/actuator/health | jq . || curl -i http://localhost:8080/actuator/health

# Application Compose generic targets
apps-up: ## [GLOBAL] Start all application containers (or pass SERVICE=<name>)
ifdef SERVICE
	docker compose -f apps/docker-compose.yml up -d --build $(SERVICE)
else
	docker compose -f apps/docker-compose.yml up -d --build
endif

apps-down: ## [GLOBAL] Stop all application containers (or pass SERVICE=<name>)
ifdef SERVICE
	docker compose -f apps/docker-compose.yml stop $(SERVICE) && docker compose -f apps/docker-compose.yml rm -f $(SERVICE)
else
	docker compose -f apps/docker-compose.yml down
endif

apps-logs: ## [GLOBAL] View logs for all application containers
ifdef SERVICE
	docker compose -f apps/docker-compose.yml logs -f $(SERVICE)
else
	docker compose -f apps/docker-compose.yml logs -f
endif

# ---------------------------------------------------------------------
# Infrastructure Commands (PostgreSQL & Keycloak)
# ---------------------------------------------------------------------
infra-up: ## [INFRA] Start infrastructure containers (PostgreSQL + Keycloak)
	docker compose -f infra/docker-compose.yml up -d

infra-down: ## [INFRA] Stop infrastructure containers
	docker compose -f infra/docker-compose.yml down

infra-restart: infra-down infra-up ## [INFRA] Restart infrastructure containers

infra-logs: ## [INFRA] View live logs for infrastructure containers
	docker compose -f infra/docker-compose.yml logs -f

infra-ps: ## [INFRA] View status of infrastructure containers
	docker compose -f infra/docker-compose.yml ps

# Alias for composition: 'make infra' or 'make auth-service infra up'
infra: infra-up

# ---------------------------------------------------------------------
# Dynamic Per-Service Rules Generator
# Generates <service>-up, <service>-down, <service>-build,
# <service>-test, <service>-clean, <service>-run, <service>-logs
# ---------------------------------------------------------------------
define SERVICE_RULES
$(1)-build:
	cd $(1) && ./mvnw clean package -DskipTests
	docker compose -f apps/docker-compose.yml build $(1)

$(1)-test:
	cd $(1) && ./mvnw test

$(1)-clean:
	cd $(1) && ./mvnw clean

$(1)-run:
	cd $(1) && ./mvnw spring-boot:run

$(1)-up: infra-up
	docker compose -f apps/docker-compose.yml up -d --build $(1)

$(1)-down:
	docker compose -f apps/docker-compose.yml stop $(1) && docker compose -f apps/docker-compose.yml rm -f $(1)

$(1)-restart: $(1)-down $(1)-up

$(1)-logs:
	docker compose -f apps/docker-compose.yml logs -f $(1)

# Service name target (e.g. 'make $(1)' or 'make $(1) infra up')
$(1): $(1)-up
endef

$(foreach svc,$(SERVICES),$(eval $(call SERVICE_RULES,$(svc))))

# ---------------------------------------------------------------------
# Keycloak & Developer Helpers
# ---------------------------------------------------------------------
keycloak-token-client: ## [TOOL] Fetch Keycloak JWT using client_credentials grant
	@curl -s -X POST "http://localhost:8081/realms/gurukul/protocol/openid-connect/token" \
		-H "Content-Type: application/x-www-form-urlencoded" \
		-d "grant_type=client_credentials" \
		-d "client_id=auth-service" \
		-d "client_secret=gurukul-auth-service-secret-change-me" | jq -r .access_token

keycloak-token-user: ## [TOOL] Fetch Keycloak JWT using password grant for user sateesh
	@curl -s -X POST "http://localhost:8081/realms/gurukul/protocol/openid-connect/token" \
		-H "Content-Type: application/x-www-form-urlencoded" \
		-d "grant_type=password" \
		-d "client_id=auth-service" \
		-d "client_secret=gurukul-auth-service-secret-change-me" \
		-d "username=sateesh" \
		-d "password=password123" | jq -r .access_token

git-status: ## [TOOL] Display git working tree status
	git status

# ---------------------------------------------------------------------
# Phony Targets Declaration
# ---------------------------------------------------------------------
.PHONY: help up down restart build test clean ps health \
        infra infra-up infra-down infra-restart infra-logs infra-ps \
        apps-up apps-down apps-logs \
        keycloak-token-client keycloak-token-user git-status \
        $(SERVICES) \
        $(foreach svc,$(SERVICES),$(svc)-up $(svc)-down $(svc)-build $(svc)-test $(svc)-clean $(svc)-run $(svc)-logs $(svc)-restart)