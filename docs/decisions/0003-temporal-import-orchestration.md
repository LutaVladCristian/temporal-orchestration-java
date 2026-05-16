# 0003: Use Temporal for CSV Import Orchestration

- Status: accepted
- Date: 2026-05-16

## Context

The repository imports one broker CSV statement into two relational tables:

- `app.income_from_sells`
- `app.other_income_fees`

The import flow is asynchronous from the caller's perspective:

- the frontend uploads a file
- the backend returns immediately after starting the import
- the UI polls for progress
- each import step performs file I/O and database writes

The current codebase already implements this flow with Temporal:

- `UploadCsvService` starts `CsvImportWorkflow`
- `CsvImportWorkflowImpl` runs `importSells` and `importOtherIncome` in parallel
- `CsvImportActivitiesImpl` reads the stored upload and writes rows through JPA repositories
- `TemporalWorkerConfig` starts the worker in the Spring Boot process

Older repository structure and notes were built around Spring Batch. That model is no longer the intended orchestration layer for imports.

## Decision

Use Temporal as the import orchestration platform and stop modeling CSV ingestion as Spring Batch job execution.

## Rationale

- the implemented runtime flow already uses Temporal workflows, activities, and worker polling
- workflow history and state live durably in Temporal instead of depending on local async execution state
- frontend polling maps cleanly to a workflow status query model
- parallel activity execution is explicit in workflow code
- Temporal is a better fit for long-running, retryable orchestration than keeping job-launch concerns inside the request path

## Consequences

### Positive

- durable workflow state and execution history
- explicit workflow ids and run ids returned to callers
- clearer separation between HTTP request handling, orchestration, and persistence work
- room to extend the import with compensation, retries, signals, or child workflows later

### Negative

- local development now requires a Temporal service in addition to PostgreSQL
- operators need Temporal-specific troubleshooting knowledge and tooling
- the repository still contains some Spring Batch remnants in build and configuration that can confuse contributors until they are removed

## Follow-up work

- remove unused Spring Batch dependencies and configuration from `spring-server/`
- clean up database setup notes that still imply a `batch` schema is required for runtime imports
- add integration tests that cover workflow start, status polling, and activity persistence
