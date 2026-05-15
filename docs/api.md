# API

## Base URL

Local default:

```text
http://localhost:8080/spring-boot-api
```

The local frontend proxies this base path from `http://localhost:5173`.

## Content types

- Upload endpoint: `multipart/form-data`
- Read endpoints: `application/json`

## Endpoints

### `POST /upload-csv`

Uploads a trading statement CSV and queues two Spring Batch jobs in parallel.

#### Form fields

- `name`: string
- `file`: CSV file

#### Example

```bash
curl -X POST http://localhost:8080/spring-boot-api/upload-csv \
  -F "name=2025 tax year" \
  -F "file=@spring-server/src/main/resources/2025_tax_year_statement.csv"
```

#### Success response

```json
{
  "sellsJobExecutionId": 123,
  "otherIncomeJobExecutionId": 124,
  "statementName": "2025 tax year"
}
```

#### Notes

- The controller returns as soon as both batch executions are launched.
- The backend reads the uploaded file fully into memory before creating the batch jobs.
- The upload currently launches `processSellsCsvJob` and `processOtherIncomeCsvJob`.
- The `name` parameter is currently only included in job parameters and echoed in the response. It is not persisted in the database.
- Multipart upload limits are `10MB` for both the file and request size.

### `GET /job-status/{executionId}`

Returns the current Spring Batch execution status for a previously started job.

The endpoint remains single-execution. After an upload, the frontend calls it once for `sellsJobExecutionId` and once for `otherIncomeJobExecutionId`.

#### Example

```bash
curl http://localhost:8080/spring-boot-api/job-status/123
```

#### Success response

```json
{
  "executionId": 123,
  "jobName": "processSellsCsvJob",
  "status": "STARTED",
  "exitCode": "EXECUTING",
  "createTime": "2026-05-15T12:23:11.038",
  "startTime": "2026-05-15T12:23:11.139",
  "endTime": null,
  "lastUpdated": "2026-05-15T12:23:11.201",
  "steps": [
    {
      "stepName": "sellsStep",
      "status": "STARTED",
      "readCount": 35,
      "writeCount": 30,
      "commitCount": 3
    }
  ],
  "failureMessages": []
}
```

#### Possible statuses

- `STARTING`
- `STARTED`
- `COMPLETED`
- `FAILED`
- `STOPPED`
- `ABANDONED`

#### Error response

- `404 Not Found` if the execution id does not exist

### `GET /income-from-sells`

Returns all persisted sale rows, ordered by `dateSold DESC, id DESC`.

#### Example response

```json
[
  {
    "id": 1,
    "dateAcquired": "2025-01-24",
    "dateSold": "2025-01-27",
    "symbol": "DAL",
    "securityName": "Delta Air Lines",
    "isin": "US2473617023",
    "country": "US",
    "quantity": 7.78598899,
    "costBasis": 524.62,
    "grossProceeds": 529.48,
    "grossPnl": 4.86,
    "currency": "USD"
  }
]
```

### `GET /other-income-fees`

Returns all persisted non-sale income rows, ordered by `date DESC, id DESC`.

#### Example response

```json
[
  {
    "id": 1,
    "date": "2025-04-03",
    "symbol": "NVDA",
    "securityName": "NVIDIA dividend",
    "isin": "US67066G1040",
    "country": "US",
    "grossAmount": 0.45,
    "withholdingTax": "0.04 RON",
    "netAmount": "0.41 RON",
    "currency": "RON"
  }
]
```

## Error behavior

The controller does not define a custom exception model. Unhandled failures from upload or batch startup currently surface through Spring Boot's default error handling.

Typical failure classes:

- invalid or unreadable upload
- batch job launch failure
- database connection failure
- CSV parsing failure

## API limitations

- No auth
- No pagination
- No filtering
- No statement-level scoping; `GET` endpoints return all rows in their tables
- No delete or reprocessing workflow
- No stable contract for validation errors
