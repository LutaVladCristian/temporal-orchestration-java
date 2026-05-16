# Architecture

## Current scope

This repository currently contains a React frontend and a Spring Boot backend that ingest broker CSV statements, orchestrate parsing with Temporal, persist normalized rows into PostgreSQL, and expose the stored rows for UI review.

## System context

The implemented system is a UI-driven ingestion workflow backed by PostgreSQL and Temporal:

```text
React UI
  -> UploadCsvController
  -> UploadCsvService
  -> UploadStorageService
  -> Temporal WorkflowClient
  -> CsvImportWorkflow
     -> importSells activity
     -> importOtherIncome activity
  -> Temporal Worker
  -> local Temporal service
  -> JPA repositories
  -> PostgreSQL
  -> AG Grid data views
```

## Main components

### Frontend application

- Location: `frontend/`
- Stack:
  - React
  - TypeScript
  - Vite
  - AG Grid Community

Responsibilities:

- drag-and-drop CSV upload
- statement naming
- Temporal workflow polling
- summary metrics for imported rows
- two separate AG Grid views for the persisted tables

### Spring Boot application

- Entry point: `spring-server/src/main/java/com/example/springserver/SpringServerApplication.java`
- Stack:
  - Java 21
  - Spring Boot 3.5
  - Spring Web
  - Temporal Java SDK
  - Spring Data JPA
  - PostgreSQL JDBC driver

### HTTP layer

- `UploadCsvController`
  - `POST /spring-boot-api/upload-csv`
  - `GET /spring-boot-api/imports/{workflowId}`
  - `GET /spring-boot-api/income-from-sells`
  - `GET /spring-boot-api/other-income-fees`

### Service layer

- `UploadCsvService`
  - stores the uploaded file reference
  - starts one Temporal workflow for the import
  - queries workflow status for frontend polling

- `UploadStorageService`
  - writes uploads to a local storage directory
  - returns a file reference that can be passed into the workflow

### Temporal orchestration layer

- `CsvImportWorkflow`
  - owns the end-to-end import lifecycle
  - tracks workflow status and per-step completion state
  - runs the two import activities in parallel

- `CsvImportActivitiesImpl`
  - reads the stored CSV file
  - extracts supported statement sections
  - maps rows into JPA entities
  - persists rows through repositories

- `TemporalWorkerConfig`
  - registers the workflow and activities on the import task queue
  - starts the in-process worker with the Spring Boot app

### Persistence layer

- Entities:
  - `IncomeFromSells`
  - `OtherIncomeFees`
- Repositories:
  - `IncomeFromSellsRepository`
  - `OtherIncomeFeesRepository`

### Database

- Engine: PostgreSQL 17
- Local/container orchestration: `spring-server/compose.yaml`
- Migration scripts: `database-setup/`
- Schemas:
  - `app` for ingestion tables

## Request and processing flow

### CSV upload flow

1. The frontend sends multipart form data with `name` and `file`.
2. `UploadCsvController` builds `UploadCsvInputDto`.
3. `UploadStorageService` writes the file to local disk and returns a stored upload reference.
4. `UploadCsvService` starts one Temporal workflow with the statement name and stored upload reference.
5. The controller returns the workflow id immediately.
6. The frontend polls `GET /imports/{workflowId}` until the workflow reaches a terminal status.
7. The workflow starts two activities in parallel:
   - `sellsStep`
   - `otherIncomeStep`
8. When the workflow completes successfully, the frontend refreshes the two read endpoints and shows the imported rows in AG Grid.

### Temporal platform model

Temporal is the durable execution platform in this repository:

- Workflows hold orchestration state and execution history
- Activities perform I/O and database work
- Workers poll the Temporal task queue and execute workflow and activity tasks
- The local Temporal service exposes gRPC on `localhost:7233`
- The local Temporal Web UI runs on `http://localhost:8233`

The current implementation keeps CSV file contents out of Temporal payloads. Only statement metadata and a stored file reference are passed into the workflow. This avoids pushing multi-megabyte uploads through workflow and activity arguments.

### CSV parsing approach

The parser is deliberately simple and format-coupled:

- It looks for a line that exactly matches a section header.
- It reads until the first blank line after that header.
- It then treats the section as a comma-delimited file with a fixed column list.

This works for the sample statement format in `spring-server/src/main/resources/2025_tax_year_statement.csv`, but it is not resilient to schema drift, quoted commas, renamed headers, or missing blank-line separators.

## Data model

### `income_from_sells`

Captures realized sale transactions:

- acquisition date
- sale date
- symbol
- security name
- ISIN
- country
- quantity
- cost basis
- gross proceeds
- gross profit and loss
- currency

### `other_income_fees`

Captures non-sale rows such as dividends and fee-like income records:

- date
- symbol
- security name
- ISIN
- country
- gross amount
- withholding tax
- net amount
- currency

### Schema layout

The repository SQL setup uses a single PostgreSQL database, `server_db`, with:

- `app.income_from_sells`
- `app.other_income_fees`

The committed SQL scripts create and evolve the `app` schema tables. The current Temporal implementation writes only to those `app` tables.

## Notable implementation details

- The backend stores uploaded CSV files on local disk before starting the workflow.
- The Temporal worker runs inside the Spring Boot process for local development simplicity.
- The two import activities execute in parallel inside one workflow.
- Activity retries are currently capped at one attempt to avoid duplicate inserts in the absence of statement-level idempotency.
- CORS accepts localhost origins on arbitrary ports to support local Vite dev servers.
- Schema management is manual. Hibernate DDL is disabled with `spring.jpa.hibernate.ddl-auto=none`.
- The repository does not include an automated migration runner; contributors still apply the SQL scripts manually.

## Gaps and risks

### Functional gaps

- No authentication or authorization
- No AI/MCP/RAG layer in the current code
- No tax rule engine or jurisdiction-specific calculation logic

### Operational gaps

- No automated application table bootstrap beyond container-created `server_db`
- No migration runner; schema creation still depends on manually applying the repository SQL scripts
- No readiness handling between app startup and PostgreSQL readiness in `compose.yaml`
- No Temporal service orchestration in repository Docker Compose; local development currently expects `temporal server start-dev`
- Minimal backend test coverage: only a context load test exists

### Code and schema inconsistencies

- Database column name `date_aquired` is misspelled in both tables and entity mappings.
- `OtherIncomeFees.withholdingTax` and `netAmount` are stored as strings, not numeric amounts.
- The CSV parsing logic assumes exact section names and exact column order.

## Intended next architectural step

If this repository continues toward the stated product goal, the next major layer should sit after ingestion:

```text
CSV ingestion -> Temporal-orchestrated import history -> normalized transaction store -> tax domain service -> AI/RAG reasoning layer -> user-facing explanation/report
```

That would separate deterministic bookkeeping from AI-assisted interpretation and explanation.
