# Local Development

## Scope

This runbook covers local setup for the backend and SQL Server based on the current repository state.

## Prerequisites

- Java 21
- Maven available as `mvn`
- Docker Desktop or equivalent Docker runtime
- A SQL client if you want to inspect or create the database manually

## Project layout

- Backend: `spring-server/`
- Database scripts: `database-setup/`

The previously referenced `frontend/` application is deleted in the current worktree, so these steps only cover the backend.

## 1. Start SQL Server

From `spring-server/`:

```bash
docker compose up -d
```

## 2. Create the database

Connect to the SQL Server container and create:

```sql
CREATE DATABASE SERVER_DB;
```

Connection defaults:

- host: `localhost`
- port: `1433`
- user: `sa`
- password: `MyStrongP@ssword1`

## 3. Apply repository SQL scripts

Run the scripts in `database-setup/` against `SERVER_DB`.

At minimum, that includes:

- `database-setup/version2/01_create_income_from_sells_table.sql`
- `database-setup/version2/02_create_other_income_fees_table copy.sql`

## 4. Verify backend configuration

Current backend defaults in `application.properties` already point at local SQL Server:

- `jdbc:sqlserver://localhost:1433;databaseName=SERVER_DB;encrypt=false;trustServerCertificate=true`
- username `sa`
- password `MyStrongP@ssword1`

## 5. Start the backend locally

From `spring-server/`:

```bash
mvn spring-boot:run
```

Expected port:

```text
http://localhost:8080
```

## 6. Test an upload

Use curl or a REST client:

```bash
curl -X POST http://localhost:8080/spring-boot-api/upload-csv \
  -F "name=2025 tax year" \
  -F "file=@src/main/resources/2025_tax_year_statement.csv"
```

Then poll the returned job id:

```bash
curl http://localhost:8080/spring-boot-api/job-status/1
```

## Common local failures

### App cannot connect to SQL Server

Check:

- the `db` container is running
- port `1433` is exposed
- `SERVER_DB` exists
- credentials match `application.properties`

### Upload starts but batch execution fails early

Likely causes:

- app tables were not created
- Spring Batch metadata tables are missing
- CSV format does not match the expected section headers or column layout

### SQL Server starts but app still fails on startup

The compose file has no readiness gate. Retry after SQL Server finishes initialization.

## Useful commands

From `spring-server/`:

```bash
mvn clean test
mvn clean package -DskipTests
docker compose up -d --build
docker compose down
```
