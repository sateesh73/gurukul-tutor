# AGENTS.md - Gurukul Tutor Development Guide

## Architecture Overview

**gurukul-tutor** is a polylithic microservices platform for online tutoring, built with Spring Boot 4.0.6 and Java 25. The project currently contains:

- **user-service**: Spring Boot REST API handling company/user management
- **Infrastructure**: Docker Compose with PostgreSQL 16 (port 5433) and Kafka KRaft (port 29092)

### Key Architectural Decisions

1. **Clean Layered Architecture** (per-module)
   - `/web` → REST controllers (package-private classes, public endpoints only)
   - `/internal` → Service layer (interfaces + implementations) + JPA entities + repositories
   - `/model` → DTOs using Java records (CompanyRequest/Response)
   - **Insight**: Controllers dependency-inject services; services inject repositories. No cross-module service calls yet (monolith).

2. **String UUID Identifiers**
   - All entities use `@GeneratedValue(strategy = GenerationType.UUID)` with String type
   - Convention: `companyId` follows snake_case in SQL, camelCase in Java
   - Example: Entities use `companyId` (Java), DB column is `company_id`

3. **DTO Separation Pattern**
   - Request DTOs (e.g., `CompanyRequest`) use Lombok `@Builder` + Java `record` syntax
   - Response DTOs (e.g., `CompanyResponse`) mirror request but add computed fields like `id`
   - Service layer handles mapping; no auto-mapping library currently (MapStruct not used)

4. **Database Schema**
   - SQL initialization in `user-service/src/main/resources/schema.sql` (executed on startup via `spring.sql.init.mode: always`)
   - Tables in `gurukul` schema (not public)
   - No liquibase/flyway; relies on Hibernate `hbm2ddl.auto: update` + manual schema.sql

## Developer Workflows

### Build & Run

```bash
# Build the project (Maven, runs from user-service directory)
cd user-service
mvn clean package

# Start infrastructure (from project root)
docker-compose up -d

# Run user-service (port 8090)
mvn spring-boot:run
```

### Testing Strategy

1. **Unit Tests** (`*ServiceImplTest.java`)
   - Use Mockito to mock repositories
   - Extend `ExtendWith(MockitoExtension.class)`
   - Example: `CompanyServiceImplTest` mocks `CompanyRepo` and verifies service logic

2. **Integration Tests** (`*ServiceIntegrationTest.java`)
   - Use `@Import(TestcontainersConfiguration.class)` to spinup test PostgreSQL container
   - Annotate with `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`
   - TestContainers auto-wires @ServiceConnection PostgreSQL for testing

3. **No Mocking of DB in Integration Tests**
   - TestContainers spins real PostgreSQL 16-alpine; tests hit actual repository layer
   - Containers are ephemeral and auto-cleaned after each test class

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CompanyServiceImplTest
```

### API Endpoints (user-service)

```
POST   /api/v1/company          → createCompany
GET    /api/v1/company/{id}     → getCompanyById
PUT    /api/v1/company/{id}     → updateCompany
DELETE /api/v1/company/{id}     → deleteCompany
```

## Code Patterns & Conventions

### Service Layer Pattern

```java
public interface CompanyService {
    void createCompany(CompanyRequest request);
    CompanyResponse getCompanyById(String id);
    void updateCompany(String id, CompanyRequest request);
    void deleteCompany(String id);
}

@Service
@RequiredArgsConstructor // Lombok: constructor injection
@Slf4j                   // SLF4J logging
class CompanyServiceImpl implements CompanyService {
    private final CompanyRepo repo;  // Auto-wired via constructor
    
    // Logs business operations, validates input, catches exceptions
}
```

### Entity Model Pattern

- Use Lombok `@Data` (generates getters, setters, equals, hashCode, toString)
- Use `@AllArgsConstructor + @Builder` for flexible construction
- **Always include explicit `@Column(name = ...)` annotations** for clarity (Java camelCase → SQL snake_case)
- Keep entities package-private (no `public` modifier); only expose through DTOs

### DTO as Records

```java
@Builder
public record CompanyResponse(
    String id,
    String name,
    String email,
    // ...
) { }
```

### Testing Assertions

- Use JUnit 5 static imports: `import static org.junit.jupiter.api.Assertions.*`
- Mockito: `@Mock` for dependencies, `@InjectMocks` for service under test
- Verify repository calls: `verify(repo).save(argThat(entity -> entity.getName().equals("expected")))`
- Use ArgumentCaptor for capturing and inspecting saved objects

## Integration Points

### PostgreSQL Connection
- Host: `localhost`, Port: `5433` (see docker-compose.yml)
- Credentials: `username: gurukul`, `password: tutor`, `database: gurukul_db`
- Spring config in `application.yaml` reads `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` env vars

### Kafka (KRaft Mode)
- **Broker address**: `localhost:29092` (from host machine)
- **Internal Docker address**: `kafka:9092`
- Currently not wired into user-service; infrastructure only

### Data Flow Example (Company CRUD)
1. **POST /api/v1/company** → `CompanyController.createCompany(CompanyRequest)`
2. → `CompanyService.createCompany()` validates & logs
3. → maps `CompanyRequest` → `Company` entity
4. → `CompanyRepo.save(company)` persists to PostgreSQL
5. → retrieves via `CompanyResponse.builder()...`

## Critical Files & Import Rules

| Purpose | Path | Notes |
|---------|------|-------|
| Main App Class | `user-service/src/main/java/.../UserServiceApplication.java` | Bootstrap entry point |
| SQL Schema | `user-service/src/main/resources/schema.sql` | Executed on startup |
| Config | `user-service/src/main/resources/application.yaml` | Datasource, JPA settings, server port |
| Company Module | `user-service/src/main/java/.../company/` | `/internal`, `/web`, `/model` |
| Tests | `user-service/src/test/java/.../company/internal/` | Unit + integration tests |

## Lombok Dependencies & Annotations

- `@Data` → getters, setters, equals, hashCode, toString
- `@AllArgsConstructor` → constructor with all fields
- `@NoArgsConstructor` → no-arg constructor (required by JPA)
- `@Builder` → builder pattern
- `@RequiredArgsConstructor` → constructor for `final` fields (used for dependency injection)
- `@Slf4j` → `log` field for SLF4J logging

## Common Pitfalls & Solutions

| Issue | Root Cause | Fix |
|-------|-----------|-----|
| Entities are `package-private` | Design: only expose via DTOs | Use `CompanyResponse` in API layer; never return entity directly |
| UUID String vs numeric ID mismatch | Documentation clarifies ongoing decision | Use `String` type with `@GeneratedValue(strategy = GenerationType.UUID)` |
| Test database fails to spin up | TestContainers requires Docker | Ensure Docker daemon running; check `TestcontainersConfiguration` |
| Service layer exception not caught | No global error handler yet | Manually catch and rethrow as `IllegalArgumentException` in services |

## When Adding a New Module (e.g., lessons-service)

1. **Replicate structure**:
   ```
   lessons-service/
   ├── pom.xml (parent: spring-boot-starter-parent 4.0.6)
   ├── src/main/java/com/tutor/gurukul/lesson/{internal,web,model}
   └── src/test/java/com/tutor/gurukul/lesson/{internal}
   ```

2. **Follow entity patterns**: Package-private JPA entities in `/internal`, DTOs in `/model`

3. **Test setup**: Import `TestcontainersConfiguration` for integration tests

4. **No inter-service communication yet**: Services call their own repositories only

---

**Last Updated**: May 2026 | **Java Version**: 25 | **Spring Boot**: 4.0.6

