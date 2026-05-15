# Local Development

## Scope

This runbook covers local setup for PostgreSQL, the Spring Boot backend, and the React frontend.

## Prerequisites

- Java 21
- Maven available as `mvn`
- Node.js 22 or newer
- npm
- Docker Desktop or equivalent Docker runtime
- A PostgreSQL client if you want to inspect the database manually

## Project layout

- Frontend: `frontend/`
- Backend: `spring-server/`
- Database scripts: `database-setup/`

## 1. Start PostgreSQL

From `spring-server/`:

```bash
docker compose up -d
```

## 2. Confirm the database

The compose setup creates `server_db` automatically on first startup.

```sql
SELECT current_database();
```

Connection defaults:

- host: `localhost`
- port: `5432`
- user: `postgres`
- password: `MyStrongP@ssword1`

## 3. Apply repository SQL scripts

Run the scripts in `database-setup/` against `server_db` in this order:

- `database-setup/version1/01_initial_set_up.sql`
- `database-setup/version2/01_create_income_from_sells_table.sql`
- `database-setup/version2/02_create_other_income_fees_table copy.sql`
- `database-setup/version2/03_create_spring_batch_metadata_tables.sql`

Notes:

- `01_initial_set_up.sql` is written for `psql` and uses `\connect`
- the scripts create `app` and `batch` schemas
- the database `search_path` is set to `app, batch, public`

## 4. Verify backend configuration

Current backend defaults in `application.properties` already point at local PostgreSQL:

- `jdbc:postgresql://localhost:5432/server_db`
- username `postgres`
- password `MyStrongP@ssword1`

## 5. Start the backend locally

From `spring-server/`:

```bash
mvn spring-boot:run
```

Expected base URL:

```text
http://localhost:8080/spring-boot-api
```

## 6. Start the frontend locally

From `frontend/`:

```bash
npm install
npm run dev
```

Expected URL:

```text
http://localhost:5173
```

The Vite dev server proxies `/spring-boot-api` to `http://localhost:8080`.

## 7. Test an upload

Option 1: use the frontend upload screen.

Option 2: use curl:

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

### App cannot connect to PostgreSQL

Check:

- the `db` container is running
- port `5432` is exposed
- `server_db` exists
- `app` and `batch` schemas exist
- credentials match `application.properties`

### Upload starts but batch execution fails early

Likely causes:

- app tables were not created
- Spring Batch metadata tables are missing
- CSV format does not match the expected section headers or column layout

### Frontend loads but API calls fail

Check:

- the backend is running on port `8080`
- the frontend is running through Vite rather than opened as a static file
- browser requests target `/spring-boot-api/...` and not a hardcoded alternate port

### PostgreSQL starts but app still fails on startup

The compose file has no readiness gate. Retry after PostgreSQL finishes initialization.

## Useful commands

From `spring-server/`:

```bash
mvn clean test
mvn clean package -DskipTests
docker compose up -d --build
docker compose down
```

From `frontend/`:

```bash
npm run dev
npm run build
```
