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

Uploads a trading statement CSV, stores it on local disk, and starts one Temporal workflow that runs the two import activities in parallel.

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
  "importWorkflowId": "csv-import-3ce84c52-503f-4df8-9501-a8a8e1d5f178",
  "importRunId": "2dcf8f20-77e8-42c2-9855-b6d638a58d79",
  "statementName": "2025 tax year"
}
```

#### Notes

- The controller returns as soon as the Temporal workflow is started.
- The backend stores the upload on local disk before the workflow starts.
- The workflow currently runs two activities:
  - `sellsStep`
  - `otherIncomeStep`
- The `name` parameter is used for workflow status and echoed in the response. It is not persisted in the database.
- Multipart upload limits are `10MB` for both the file and request size.

### `GET /imports/{workflowId}`

Returns the current Temporal workflow status for a previously started import.

#### Example

```bash
curl http://localhost:8080/spring-boot-api/imports/csv-import-3ce84c52-503f-4df8-9501-a8a8e1d5f178
```

#### Success response

```json
{
  "workflowId": "csv-import-3ce84c52-503f-4df8-9501-a8a8e1d5f178",
  "statementName": "2025 tax year",
  "status": "RUNNING",
  "createdAt": "2026-05-15T14:23:11.038Z",
  "startedAt": "2026-05-15T14:23:11.038Z",
  "completedAt": null,
  "steps": [
    {
      "stepName": "sellsStep",
      "status": "COMPLETED",
      "readCount": 35,
      "writeCount": 35
    },
    {
      "stepName": "otherIncomeStep",
      "status": "RUNNING",
      "readCount": 0,
      "writeCount": 0
    }
  ],
  "failureMessages": []
}
```

#### Possible statuses

- `RUNNING`
- `COMPLETED`
- `FAILED`

#### Error response

- `404 Not Found` if the workflow id does not exist

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

The controller does not define a custom exception model. Unhandled failures from upload start or workflow execution currently surface through Spring Boot's default error handling.

Typical failure classes:

- invalid or unreadable upload
- Temporal workflow start failure
- workflow activity failure during CSV parsing or persistence
- database connection failure
- CSV parsing failure

## API limitations

- No auth
- No pagination
- No filtering
- No statement-level scoping; `GET` endpoints return all rows in their tables
- No delete or reprocessing workflow
- No stable contract for validation errors
- No statement-level idempotency for repeated imports of the same file
