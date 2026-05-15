# trading-tax-calculator-ai

This repository contains a Spring Boot ingestion service and a React + TypeScript frontend for loading broker CSV statements, starting parallel Spring Batch imports, and reviewing the imported database rows in AG Grid.

The broader AI-assisted tax calculator described in older repo notes is still not implemented in the current application code. The runnable surface in this checkout is the CSV ingestion workflow and its database views.

## Documentation

- [Architecture](docs/architecture.md)
- [API](docs/api.md)
- [Auth](docs/auth.md)
- [Deployment](docs/deployment.md)
- [Local development runbook](docs/runbooks/local-dev.md)
- [Production debugging runbook](docs/runbooks/production-debugging.md)
- [ADR 0001: auth strategy](docs/decisions/0001-auth-strategy.md)
- [ADR 0002: database choice](docs/decisions/0002-database-choice.md)
- [Contributor guide](CONTRIBUTING.md)

## Quick start

1. Start PostgreSQL from `spring-server/`:
   ```bash
   docker compose up -d
   ```
2. Apply the SQL scripts in order with `psql`:
   - `database-setup/version1/01_initial_set_up.sql`
   - `database-setup/version2/01_create_income_from_sells_table.sql`
   - `database-setup/version2/02_create_other_income_fees_table copy.sql`
   - `database-setup/version2/03_create_spring_batch_metadata_tables.sql`
   - `database-setup/version3/01_expand_symbol_column_lengths.sql`
3. Start the backend:
   ```bash
   cd spring-server
   mvn spring-boot:run
   ```
4. Start the frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

Application URLs:

```text
Frontend: http://localhost:5173
Backend:  http://localhost:8080/spring-boot-api
```

## Common workflow

1. Open the frontend.
2. Drop a CSV file into the upload zone.
3. Start the import and wait for both batch jobs to complete.
4. Review `app.income_from_sells` and `app.other_income_fees` in the two AG Grid views.

## Repository layout

```text
frontend/        React + TypeScript UI
spring-server/   Spring Boot backend
database-setup/  SQL scripts
docs/            project documentation
```
