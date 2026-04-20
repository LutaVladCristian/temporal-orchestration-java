# Plan: React/TypeScript Frontend + Backend Wiring

## Context
The backend has a POST `/spring-boot-api/upload-csv` endpoint and two DB tables (`income_from_sells`, `other_income_fees`) but the `UploadCsvService` was not yet implemented. The frontend needed to send a CSV file, wait for a Spring Batch job to process it, then display both tables in AG Grid. Polling was chosen to detect job completion.

---

## Part 1 — Backend changes

### 1.1 CORS configuration
**File:** `BackEndServer/src/main/java/com/example/BackEndServer/config/WebConfig.java`
- Allows `http://localhost:5173` (Vite dev server) on all routes

### 1.2 JPA Entities
**Files** in `server/entity/`:
- `IncomeFromSells.java` — maps `income_from_sells` table
- `OtherIncomeFees.java` — maps `other_income_fees` table

### 1.3 JPA Repositories
**Files** in `server/repository/`:
- `IncomeFromSellsRepository.java` — extends `JpaRepository<IncomeFromSells, Long>`
- `OtherIncomeFeesRepository.java` — extends `JpaRepository<OtherIncomeFees, Long>`

### 1.4 Spring Batch job
**File:** `server/jobs/CsvBatchConfig.java`
- `processCsvJob` with two steps:
  - Step 1: parse `Income from Sells` section → write to `income_from_sells`
  - Step 2: parse `Other income & fees` section → write to `other_income_fees`
- Reader: splits CSV bytes on the blank line separating sections, builds `FlatFileItemReader` from `ByteArrayResource`
- Processor: maps CSV row POJO → JPA entity
- Writer: `RepositoryItemWriter`

### 1.5 Job status tracking
- **Endpoint:** `GET /spring-boot-api/job-status/{executionId}` — queries `JobExplorer`, returns `{ "status": "COMPLETED" | "STARTED" | "FAILED" }`

### 1.6 Data endpoints
- `GET /spring-boot-api/income-from-sells` — returns `List<IncomeFromSells>`
- `GET /spring-boot-api/other-income-fees` — returns `List<OtherIncomeFees>`

### 1.7 UploadCsvService
- Reads `MultipartFile` bytes, launches `processCsvJob` via `JobLauncher`
- Returns job execution ID

### 1.8 UploadCsvController
- Changed return type from `String` redirect to `ResponseEntity<Map<String, Long>>` with `{ "jobExecutionId": <id> }`

### 1.9 application.properties
- `spring.batch.jdbc.initialize-schema=always`

---

## Part 2 — Frontend

**Location:** `frontend/` at repo root  
**Stack:** Vite + React + TypeScript, AG Grid, react-dropzone, axios

### File structure
```
frontend/src/
  components/
    DropZone.tsx              — drag-and-drop area + name input
    StatusBanner.tsx          — uploading / processing / error states
    IncomeFromSellsGrid.tsx   — AG Grid for income_from_sells
    OtherIncomeFeesGrid.tsx   — AG Grid for other_income_fees
  api/
    client.ts                 — axios instance → http://localhost:8080
    uploadCsv.ts              — POST multipart form
    pollJobStatus.ts          — polls GET /job-status/:id every 2s
    fetchTables.ts            — GET both data endpoints
  App.tsx                     — state machine
```

### App state machine
```
idle → uploading → polling → done
                           → error
```

---

## Running the full stack

```bash
# 1. Database
cd BackEndServer && docker compose up -d

# 2. Backend
cd BackEndServer && mvn spring-boot:run

# 3. Frontend
cd frontend && npm run dev
```

Open `http://localhost:5173`, enter a name, drop the CSV, click Upload & Process. The UI polls every 2s until the batch job completes, then renders both AG Grid tables.