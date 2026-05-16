# Production Debugging

## Scope

This repository does not contain a full production deployment stack. This runbook treats "production" as any long-running deployment of the Spring Boot backend with PostgreSQL behind it.

## First triage checklist

1. Confirm whether the failure is:
   - upload/API
   - Temporal workflow execution
   - database connectivity
   - data quality
2. Capture:
   - request timestamp
   - workflow id if available
   - backend logs
   - database connectivity state

## Backend checks

### Verify app reachability

- `GET /spring-boot-api/imports/{workflowId}` for a known id
- container or process health
- port `8080` listener

### Inspect logs

Prioritize:

- Temporal workflow start and worker exceptions
- PostgreSQL connection failures
- CSV parsing and type conversion errors
- startup failures tied to missing tables

## Database checks

### Connectivity

Verify:

- PostgreSQL process is healthy
- target database is `server_db`
- credentials are correct

### Schema presence

Confirm the application tables exist:

- `app.income_from_sells`
- `app.other_income_fees`

## Common failure modes

### Missing application tables

Symptoms:

- insert failures during activity writes
- startup succeeds but uploads fail

Action:

- run the repository SQL scripts against `server_db`

### CSV format drift

Symptoms:

- parse errors
- empty extracted sections
- partial imports

Action:

- compare the incoming file with `src/main/resources/2025_tax_year_statement.csv`
- verify section headers:
  - `Income from Sells`
  - `Other income & fees`
- verify a blank line separates sections

### PostgreSQL readiness race

Symptoms:

- backend fails after restart even though the DB container is up

Action:

- restart the backend after PostgreSQL is fully ready
- add health checks and readiness logic to the deployment

### Temporal service unavailable

Symptoms:

- upload request fails immediately
- workflow status stays unavailable
- worker startup logs show Temporal connection failures

Action:

- verify the Temporal service is reachable on the configured target
- confirm the namespace is `default` unless overridden
- check that the worker started with the Spring Boot process

## Data validation queries

Useful checks after a reported import:

```sql
SELECT COUNT(*) FROM app.income_from_sells;
SELECT COUNT(*) FROM app.other_income_fees;
```

Spot-check recent rows:

```sql
SELECT * FROM app.income_from_sells ORDER BY id DESC LIMIT 20;
SELECT * FROM app.other_income_fees ORDER BY id DESC LIMIT 20;
```

## Hardening follow-ups

Every recurring incident should feed one of:

- better input validation
- schema automation
- startup readiness checks
- integration tests around upload and Temporal workflow execution
