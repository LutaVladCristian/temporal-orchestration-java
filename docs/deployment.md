# Deployment

## Current deployment model

The repository currently supports a simple containerized local deployment for:

- PostgreSQL 17
- Spring Boot backend

There is no committed production deployment configuration, no reverse proxy config, and no infrastructure-as-code in this repository.

## Artifacts

### Backend container

- Dockerfile: `spring-server/Dockerfile`
- Runtime image base: `eclipse-temurin:21-jdk`
- Expected input artifact: `target/*.jar`

### Compose setup

- Compose file: `spring-server/compose.yaml`
- Services:
  - `db`
  - `app`

## Environment variables

The backend uses these runtime variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Shared defaults live in `spring-server/src/main/resources/application.yaml`.

`spring-server/src/main/resources/application.properties` is reserved for local-development secret overrides, currently the PostgreSQL username and password. Real deployments should provide credentials through environment-managed secrets instead.

## Local container deployment

### 1. Build the application jar

Run from `spring-server/`:

```bash
mvn clean package -DskipTests
```

### 2. Start the compose stack

```bash
docker compose up -d --build
```

### 3. Verify services

- PostgreSQL should listen on `localhost:5432`
- Backend should listen on `localhost:8080`

## Database bootstrap requirements

The compose file creates `server_db`, but the repo instructions still require manual execution of SQL scripts under `database-setup/`.

This is important because:

- Hibernate DDL is disabled
- app tables and schemas are not auto-created
- the application still uses hand-applied SQL for the `app` schema

Apply the scripts in order:

1. `database-setup/version1/01_initial_set_up.sql`
2. `database-setup/version2/01_create_income_from_sells_table.sql`
3. `database-setup/version2/02_create_other_income_fees_table copy.sql`

The repository SQL scripts create:

- database `server_db`
- schema `app`
- the application tables under `app`

The runtime CSV import path is Temporal-based and writes only to the `app` tables. Older notes about a dedicated Spring Batch schema no longer match the committed SQL setup.

## Production concerns

The current compose setup is not production-ready.

### Database startup sequencing

`depends_on` only guarantees container start order, not database readiness. The backend can still start before PostgreSQL is accepting connections.

### Secrets

Passwords are committed in local defaults and compose config. Replace them with environment-managed secrets before any non-local deployment.

### Temporal service dependency

The backend now depends on a reachable Temporal service in addition to PostgreSQL. The repository does not include Temporal in `spring-server/compose.yaml`, so any deployment plan needs an explicit Temporal service or Temporal Cloud target.
### Persistence and backup

The compose volume `pgdata` persists the PostgreSQL data directory locally, but there is no backup, retention, or restore procedure in the repository.

## Recommended production baseline

For a serious deployment, add at least:

1. Database creation and migration automation
2. Secret management
3. Temporal service provisioning and connectivity management
4. Health checks and readiness gates
5. Structured logging and log aggregation
6. Backup and restore procedures
7. Auth before external exposure
