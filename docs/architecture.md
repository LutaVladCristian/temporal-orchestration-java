# Architecture

## Current scope

This repository currently contains a React frontend and a Spring Boot backend that ingest broker CSV statements, parse them with Spring Batch, persist normalized rows into PostgreSQL, and expose the stored rows for UI review.

## System context

The implemented system is a UI-driven ingestion workflow backed by a relational database:

```text
React UI
  -> UploadCsvController
  -> UploadCsvService
  -> async Spring Batch job
     -> sells step
     -> other income step
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
- batch job polling
- summary metrics for imported rows
- two separate AG Grid views for the persisted tables

### Spring Boot application

- Entry point: `spring-server/src/main/java/com/example/springserver/SpringServerApplication.java`
- Stack:
  - Java 21
  - Spring Boot 3.5
  - Spring Web
  - Spring Batch
  - Spring Data JPA
  - PostgreSQL JDBC driver

### HTTP layer

- `UploadCsvController`
  - `POST /spring-boot-api/upload-csv`
  - `GET /spring-boot-api/job-status/{executionId}`
  - `GET /spring-boot-api/income-from-sells`
  - `GET /spring-boot-api/other-income-fees`

### Service layer

- `UploadCsvService`
  - reads the uploaded `MultipartFile`
  - builds a batch job from the uploaded byte array
  - launches the job with a timestamped job parameter set

### Batch infrastructure

- `BatchInfrastructureConfig`
  - provides an async `JobLauncher` backed by `SimpleAsyncTaskExecutor`
  - allows the UI to receive a job execution id immediately and poll for progress

### Batch processing layer

- `CsvBatchConfig`
  - splits the uploaded CSV into named sections
  - runs a two-step job:
    - `sellsStep`
    - `otherIncomeStep`
  - maps each row into JPA entities
  - persists rows through repository writers

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
  - `batch` for Spring Batch metadata

## Request and processing flow

### CSV upload flow

1. The frontend sends multipart form data with `name` and `file`.
2. `UploadCsvController` builds `UploadCsvInputDto`.
3. `UploadCsvService` reads the file into memory as `byte[]`.
4. `CsvBatchConfig.processCsvJob(csvBytes)` creates a job instance bound to that upload.
5. `TaskExecutorJobLauncher` starts the batch job asynchronously.
6. The controller returns `jobExecutionId` immediately.
7. The frontend polls `GET /job-status/{executionId}` until the job reaches a terminal status.
8. The frontend refreshes the two read endpoints and shows the imported rows in AG Grid.

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
- `batch.*` Spring Batch metadata tables and sequences

The initial database script sets `search_path` to `app, batch, public` so the current JPA mappings and Spring Batch defaults continue to work without schema-qualified table names in the Java code.

## Notable implementation details

- The batch job is launched programmatically; `spring.batch.job.enabled=false` only disables auto-run at startup.
- Uploaded CSV files are processed fully in memory as `byte[]`.
- The async job launcher uses `SimpleAsyncTaskExecutor`, so job execution is decoupled from the HTTP request thread.
- CORS accepts localhost origins on arbitrary ports to support local Vite dev servers.
- Schema management is manual. Hibernate DDL is disabled with `spring.jpa.hibernate.ddl-auto=none`.
- The initial setup script assumes `psql` because it uses `\connect`.

## Gaps and risks

### Functional gaps

- No authentication or authorization
- No AI/MCP/RAG layer in the current code
- No tax rule engine or jurisdiction-specific calculation logic

### Operational gaps

- No automated application table bootstrap beyond container-created `server_db`
- No migration runner; schema creation still depends on manually applying the repository SQL scripts
- No readiness handling between app startup and PostgreSQL readiness in `compose.yaml`
- Minimal backend test coverage: only a context load test exists

### Code and schema inconsistencies

- Database column name `date_aquired` is misspelled in both tables and entity mappings.
- `OtherIncomeFees.withholdingTax` and `netAmount` are stored as strings, not numeric amounts.
- The CSV parsing logic assumes exact section names and exact column order.

## Intended next architectural step

If this repository continues toward the stated product goal, the next major layer should sit after ingestion:

```text
CSV ingestion -> normalized transaction store -> tax domain service -> AI/RAG reasoning layer -> user-facing explanation/report
```

That would separate deterministic bookkeeping from AI-assisted interpretation and explanation.
