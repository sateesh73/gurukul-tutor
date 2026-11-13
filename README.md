# Gurukul Tutor

A Java-based microservices project.

## Current Services

### auth-service

The authentication service handles user management, company management, and authentication/authorization.

## Prerequisites

- Java 27 or higher
- Maven 3.9+ (via Maven wrapper)
- Docker (for running infrastructure)
- Docker Compose (for orchestration)

## Project Structure

```
gurukul-tutor/
├── Makefile                    # Root Makefile for all microservices
├── apps/                       # Application service definitions
│   └── docker-compose.yml     # Docker Compose for auth-service
├── infra/                      # Infrastructure configuration
│   └── docker-compose.yml     # PostgreSQL database service
├── auth-service/               # Authentication microservice
│   ├── Dockerfile             # Multi-stage Docker build
│   ├── .dockerignore          # Docker build exclusions
│   ├── mvnw                   # Maven wrapper
│   ├── pom.xml                # Spring Boot 4.1.2, Java 27
│   └── src/                   # Source code
│       ├── main/
│       │   ├── java/          # Java source files
│       │   └── resources/     # Application configuration
│       └── test/              # Test files
└── README.md
```

## Running the Project

### Quick Start (Using Makefile)

```bash
# Build and run auth-service with Docker
make up

# Check health
curl http://localhost:8080/actuator/health

# Stop services
make down
```

### Available Make Commands

| Command | Description |
|---------|-------------|
| `make help` | Show all available commands |
| `make build` | Build the auth-service locally |
| `make test` | Run unit tests |
| `make run` | Run auth-service locally (Spring Boot) |
| `make up` | Build and run Docker containers |
| `make down` | Stop and remove Docker containers |
| `make dev` | Build and run locally for development |
| `make restart` | Clean restart of containers |
| `make prod` | Production build (skip tests) |
| `make git-status` | Show git status |
| `make git-commit MSG="msg"` | Commit changes |

### Option 1: Using Docker (Recommended)

The project is configured for Docker-based development with proper separation of concerns:
- **Database**: PostgreSQL 16 runs in `infra/docker-compose.yml`
- **Services**: Microservices run in `apps/docker-compose.yml`

```bash
# Start infrastructure (PostgreSQL)
docker compose -f infra/docker-compose.yml up -d

# Start auth-service (connects to postgres via docker network)
make up

# Or use docker-compose directly from apps folder
docker compose -f apps/docker-compose.yml up -d

# Check if it's running
curl http://localhost:8080/actuator/health
```

### Option 2: Local Build (for development)

```bash
# Start infrastructure first
docker compose -f infra/docker-compose.yml up -d

# Build and run auth-service locally
cd auth-service
./mvnw spring-boot:run
```

Or use the Makefile:
```bash
make run
```

### Option 3: Build and Run Manually

```bash
# Build
cd auth-service
./mvnw clean package -DskipTests

# Run
java -jar target/auth-service-*.jar
```

## Docker Configuration

### Building Docker Image

```bash
make docker-build
# or
docker build -t gurukul/auth-service:latest -f auth-service/Dockerfile .
```

### Running with Docker Compose

```bash
make up
# or
docker compose -f apps/docker-compose.yml up -d
```

### Docker Compose Files

- `infra/docker-compose.yml` - Infrastructure (PostgreSQL database)
- `apps/docker-compose.yml` - Application services (auth-service)

The services are configured to communicate over Docker's default network.

## Configuration

Application configuration is in `auth-service/src/main/resources/application.yaml`:
- Database connection via environment variables (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`)
- Server port via `SERVER_PORT` (default: 8080)

## Available Endpoints

Once running, the auth-service is available at `http://localhost:8080`

### Key API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/companies` | POST | Create a new company |
| `/api/v1/companies` | GET | Get all companies |
| `/api/v1/companies/{id}` | GET | Get company by ID |
| `/api/v1/companies/{id}` | PUT | Update company |
| `/api/v1/companies/{id}` | DELETE | Delete company |
| `/api/v1/users` | POST | Create a new user (requires existing company) |
| `/api/v1/users` | GET | Get all users |
| `/api/v1/users/{id}` | GET | Get user by ID |
| `/api/v1/users/{id}` | PUT | Update user |
| `/api/v1/users/{id}` | DELETE | Delete user |
| `/api/v1/users/byusername/{username}` | GET | Find user by username |
| `/api/v1/users/company/{companyId}` | GET | Get users by company |

### Initial Data

On startup, the application creates:
- Default company: **Gurukul** (code: `GURUKUL`, ID: 1)
- Default admin user: **sateesh** (email: `sateesh@gurukul.com`, password: `admin123`)

Note: Password should be changed in production!

## Running Tests

```bash
# Run all tests
make test

# Or from auth-service directory
cd auth-service
./mvnw test
```

### Test Coverage

- Unit tests for User and Company entities
- Service layer tests (CRUD operations)
- Controller tests (REST API endpoints)
- Repository tests (database operations)

## Development

### Project Structure

```bash
auth-service/src/main/java/com/gurukul/authservice/
├── AuthDataServiceInitializer.java    # Default data on startup
├── authservice/                        # Core application
│   └── AuthServiceApplication.java
├── company/                           # Company feature (package-private)
│   ├── CompanyController.java
│   ├── Company.java
│   ├── CompanyRepository.java
│   ├── CompanyService.java
│   └── CompanyServiceImpl.java
├── config/                            # Configuration classes
├── dto/                               # Data Transfer Objects
├── exception/                         # Custom exceptions
├── user/                              # User feature (package-private)
│   ├── Access.java                   # Access levels enum
│   ├── UserController.java
│   ├── User.java
│   ├── UserRepository.java
│   ├── UserService.java
│   └── UserServiceImpl.java
└── web/                               # REST Controllers
```

### Feature-Based Package Structure

The project follows a feature-based architecture where each feature (user, company, auth) is self-contained:
- Internal classes use package-private access (no `public` modifier)
- Services provide public interfaces for cross-feature communication
- Controllers are separated in the `web` package

### Access Levels

Users have hierarchical access levels:
- `ADMIN (1)` - Highest, full access
- `ASSOCIATE (2)` - Company admin
- `TEACHER (3)` - Teacher access
- `STUDENT (4)` - Student access
- `TEMP (5)` - Lowest, for new users

## Adding New Services

When adding a new microservice, create a new directory under the project root following the same patterns as `auth-service`:

```bash
# Create new service directory
mkdir -p new-service/src/{main,test}/{java,resources}
mkdir -p new-service/src/main/java/com/gurukul/newservice/{feature,config,dto,exception,web}
mkdir -p new-service/src/test/java/com/gurukul/newservice

# Copy Maven configuration
cp auth-service/pom.xml new-service/
cp auth-service/mvnw new-service/
cp auth-service/Dockerfile new-service/

# Update in Makefile for the new service
# Update in apps/docker-compose.yml
```

## Database

The application uses PostgreSQL. Initial database schema is auto-created by Hibernate (`ddl-auto: create-drop` in dev).

### Database Connection

Default local connection:
- Host: `localhost`
- Port: `5432`
- Database: `authdb`
- Username: `authuser`
- Password: `authpass`

### Docker PostgreSQL

PostgreSQL runs in `infra/docker-compose.yml`:
```bash
docker compose -f infra/docker-compose.yml up -d
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | Database host |
| `DB_PORT` | `5432` | Database port |
| `DB_NAME` | `authdb` | Database name |
| `DB_USERNAME` | `authuser` | Database username |
| `DB_PASSWORD` | `authpass` | Database password |
| `SERVER_PORT` | `8080` | Server port |

## Troubleshooting

### Port already in use

```bash
# Kill process using port 8080
lsof -ti:8080 | xargs kill -9  # Linux/Mac
# or
netstat -ano | findstr :8080  # Windows
```

### Database connection issues

```bash
# Check if PostgreSQL is running
docker compose -f infra/docker-compose.yml ps

# Check connection
docker compose -f infra/docker-compose.yml exec postgres pg_isready

# View logs
docker compose -f infra/docker-compose.yml logs postgres
```

### Clean rebuild

```bash
# Clean everything
make clean
find . -name "*.jar" -delete

# Rebuild
make build
make up
```