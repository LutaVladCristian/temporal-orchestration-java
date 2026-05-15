# Architecture

## Current scope

This repository currently contains a Spring Boot backend that ingests broker CSV statements, parses them with Spring Batch, and persists normalized rows into PostgreSQL.

The larger product direction described in repo notes is an AI-assisted trading tax calculator using MCP and RAG. That layer is not present in the current application code. In the current checkout, the previously referenced `frontend/` application is also deleted from the working tree.

## System context

The implemented system is a backend ingestion service with a relational database:

```text
CSV statement upload
  -> UploadCsvController
  -> UploadCsvService
  -> Spring Batch job
     -> sells step
     -> other income step
  -> JPA repositories
  -> PostgreSQL
```

## Main components

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
  - Reads the uploaded `MultipartFile`
  - Builds a batch job from the uploaded byte array
  - Launches the job with a timestamped job parameter set

### Batch layer

- `CsvBatchConfig`
  - Splits the uploaded CSV into named sections
  - Runs a two-step job:
    - `sellsStep`
    - `otherIncomeStep`
  - Maps each row into JPA entities
  - Persists rows through repository writers

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

## Request and processing flow

### CSV upload flow

1. Client sends multipart form data with `name` and `file`.
2. `UploadCsvController` builds `UploadCsvInputDto`.
3. `UploadCsvService` reads the file into memory as `byte[]`.
4. `CsvBatchConfig.processCsvJob(csvBytes)` creates a job instance bound to that upload.
5. Spring Batch runs:
   - `sellsStep`: parses `Income from Sells`
   - `otherIncomeStep`: parses `Other income & fees`
6. Rows are saved through Spring Data repositories.
7. Client can poll job status by execution id.

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

## Notable implementation details

- The batch job is launched programmatically; `spring.batch.job.enabled=false` only disables auto-run at startup.
- Uploaded CSV files are processed fully in memory as `byte[]`.
- CORS is open only to `http://localhost:5173`.
- Schema management is manual. Hibernate DDL is disabled with `spring.jpa.hibernate.ddl-auto=none`.

## Gaps and risks

### Functional gaps

- No authentication or authorization
- No AI/MCP/RAG layer in the current code
- No tax rule engine or jurisdiction-specific calculation logic
- No currently present frontend in this checkout

### Operational gaps

- No automated application table bootstrap beyond container-created `server_db`
- No Spring Batch metadata migration scripts in the repository
- No readiness handling between app startup and PostgreSQL readiness in `compose.yaml`
- Minimal test coverage: only a context load test exists

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
