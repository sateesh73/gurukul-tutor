# Infra

Docker Compose infrastructure for the **gurukul-tutor** services.

Currently provides a single **PostgreSQL 16** container that the `auth-service`
connects to.

## Start / stop

```bash
# from the project root (gurukul-tutor/)
docker compose -f infra/docker-compose.yml up -d     # start in background
docker compose -f infra/docker-compose.yml down     # stop and remove containers
docker compose -f infra/docker-compose.yml ps       # status
```

## Defaults

| Setting        | Default              | Override via env var |
|----------------|----------------------|----------------------|
| Database       | `authdb`             | `POSTGRES_DB`        |
| User           | `authuser`           | `POSTGRES_USER`      |
| Password       | `authpass`           | `POSTGRES_PASSWORD`  |
| Host port      | `5432`               | `POSTGRES_PORT`      |

## Schema

`infra/init-db/01-init.sql` runs once on first DB creation (mounted into the
container's `/docker-entrypoint-initdb.d`). It creates the `users` table used by
the auth service.

## How the auth service connects

`auth-service/src/main/resources/application.yaml` points at the compose
service name `postgres` (resolvable as a hostname on the shared `gurukul-net`
network):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/authdb
    username: ${DB_USERNAME:authuser}
    password: ${DB_PASSWORD:authpass}
```

For local development outside Docker you can override with `DB_USERNAME` /
`DB_PASSWORD` / `SPRING_DATASOURCE_URL`.