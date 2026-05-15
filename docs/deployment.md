# Deployment

## Current deployment model

The repository currently supports a simple containerized local deployment for:

- Microsoft SQL Server 2022
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

Defaults are also present in `application.properties`, which is convenient for local development but should not be relied on for real deployment.

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

- SQL Server should listen on `localhost:1433`
- Backend should listen on `localhost:8080`

## Database bootstrap requirements

The compose file does not create `SERVER_DB` for you in a reliable, application-owned way. The repo instructions still require manual database creation and manual execution of SQL scripts under `database-setup/`.

This is important because:

- Hibernate DDL is disabled
- app tables are not auto-created
- Spring Batch schema auto-init is disabled with `spring.batch.jdbc.initialize-schema=never`

## Production concerns

The current compose setup is not production-ready.

### Database startup sequencing

`depends_on` only guarantees container start order, not database readiness. The backend can still start before SQL Server is accepting connections.

### Secrets

Passwords are committed in local defaults and compose config. Replace them with environment-managed secrets before any non-local deployment.

### Batch metadata schema

Spring Batch typically requires metadata tables. The repository does not include explicit SQL Server migration scripts for them, and auto-init is disabled. A deployment plan needs to address that before batch execution can be considered reliable.

### Persistence and backup

The compose volume `mssqldata` persists the SQL Server data directory locally, but there is no backup, retention, or restore procedure in the repository.

## Recommended production baseline

For a serious deployment, add at least:

1. Database creation and migration automation
2. Secret management
3. Health checks and readiness gates
4. Structured logging and log aggregation
5. Backup and restore procedures
6. Auth before external exposure
