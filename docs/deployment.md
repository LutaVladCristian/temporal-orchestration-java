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

- PostgreSQL should listen on `localhost:5432`
- Backend should listen on `localhost:8080`

## Database bootstrap requirements

The compose file creates `server_db`, but the repo instructions still require manual execution of SQL scripts under `database-setup/`.

This is important because:

- Hibernate DDL is disabled
- app tables and schemas are not auto-created
- Spring Batch schema auto-init is disabled with `spring.batch.jdbc.initialize-schema=never`

Apply the scripts in order:

1. `database-setup/version1/01_initial_set_up.sql`
2. `database-setup/version2/01_create_income_from_sells_table.sql`
3. `database-setup/version2/02_create_other_income_fees_table copy.sql`
4. `database-setup/version2/03_create_spring_batch_metadata_tables.sql`

The initial setup script uses `psql` meta-commands and creates:

- database `server_db`
- schema `app`
- schema `batch`
- database `search_path` of `app, batch, public`

## Production concerns

The current compose setup is not production-ready.

### Database startup sequencing

`depends_on` only guarantees container start order, not database readiness. The backend can still start before PostgreSQL is accepting connections.

### Secrets

Passwords are committed in local defaults and compose config. Replace them with environment-managed secrets before any non-local deployment.

### Batch metadata schema

Spring Batch requires metadata tables. The repository includes an explicit PostgreSQL SQL script for them in `database-setup/version2/03_create_spring_batch_metadata_tables.sql`, but a deployment plan still needs to ensure that script is applied consistently.

### Persistence and backup

The compose volume `pgdata` persists the PostgreSQL data directory locally, but there is no backup, retention, or restore procedure in the repository.

## Recommended production baseline

For a serious deployment, add at least:

1. Database creation and migration automation
2. Secret management
3. Health checks and readiness gates
4. Structured logging and log aggregation
5. Backup and restore procedures
6. Auth before external exposure
