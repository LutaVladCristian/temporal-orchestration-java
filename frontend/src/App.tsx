import { useEffect, useMemo, useRef, useState } from "react";
import { AgGridReact } from "ag-grid-react";
import type { ColDef } from "ag-grid-community";
import {
  fetchImportStatus,
  fetchIncomeFromSells,
  fetchOtherIncomeFees,
  type IncomeFromSellsRow,
  type ImportStatus,
  type OtherIncomeFeesRow,
  uploadCsv
} from "./api";

import "ag-grid-community/styles/ag-grid.css";
import "ag-grid-community/styles/ag-theme-quartz.css";

type GridView = "sells" | "other";

const terminalStatuses = new Set(["COMPLETED", "FAILED", "STOPPED", "ABANDONED"]);
const failedStatuses = new Set(["FAILED", "STOPPED", "ABANDONED"]);

const defaultColDef: ColDef = {
  sortable: true,
  filter: true,
  resizable: true,
  floatingFilter: true,
  minWidth: 120
};

function formatCurrency(value: number) {
  return new Intl.NumberFormat("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value);
}

function formatDateTime(value: string | null) {
  if (!value) {
    return "Pending";
  }

  return new Intl.DateTimeFormat("en-GB", {
    dateStyle: "medium",
    timeStyle: "medium"
  }).format(new Date(value));
}

function fileNameToStatementName(fileName: string) {
  return fileName.replace(/\.[^/.]+$/, "").replace(/[_-]+/g, " ").trim();
}

function aggregateImportStatus(importStatus: ImportStatus | null) {
  if (!importStatus) {
    return "IDLE";
  }

  if (failedStatuses.has(importStatus.status)) {
    return "FAILED";
  }

  if (importStatus.status === "COMPLETED") {
    return "COMPLETED";
  }

  if (importStatus.status === "RUNNING") {
    return "STARTED";
  }

  return "IDLE";
}

export default function App() {
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [statementName, setStatementName] = useState("");
  const [dragActive, setDragActive] = useState(false);
  const [activeView, setActiveView] = useState<GridView>("sells");
  const [sellsSearch, setSellsSearch] = useState("");
  const [otherSearch, setOtherSearch] = useState("");
  const [importStatus, setImportStatus] = useState<ImportStatus | null>(null);
  const [uploading, setUploading] = useState(false);
  const [loadingData, setLoadingData] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [incomeFromSells, setIncomeFromSells] = useState<IncomeFromSellsRow[]>([]);
  const [otherIncomeFees, setOtherIncomeFees] = useState<OtherIncomeFeesRow[]>([]);

  const currentSearch = activeView === "sells" ? sellsSearch : otherSearch;

  const incomeFromSellsColumns = useMemo<ColDef<IncomeFromSellsRow>[]>(
    () => [
      { field: "dateSold", headerName: "Date sold", sort: "desc" },
      { field: "dateAcquired", headerName: "Date acquired" },
      { field: "symbol", headerName: "Symbol", minWidth: 110 },
      { field: "securityName", headerName: "Security", minWidth: 220, flex: 1.2 },
      { field: "isin", headerName: "ISIN", minWidth: 170 },
      { field: "country", headerName: "Country", minWidth: 110 },
      { field: "quantity", headerName: "Quantity", type: "numericColumn" },
      {
        field: "costBasis",
        headerName: "Cost basis",
        valueFormatter: (params) => formatCurrency(Number(params.value ?? 0))
      },
      {
        field: "grossProceeds",
        headerName: "Gross proceeds",
        valueFormatter: (params) => formatCurrency(Number(params.value ?? 0))
      },
      {
        field: "grossPnl",
        headerName: "Gross P/L",
        valueFormatter: (params) => formatCurrency(Number(params.value ?? 0))
      },
      { field: "currency", headerName: "Currency", minWidth: 110 }
    ],
    []
  );

  const otherIncomeColumns = useMemo<ColDef<OtherIncomeFeesRow>[]>(
    () => [
      { field: "date", headerName: "Date", sort: "desc" },
      { field: "symbol", headerName: "Symbol", minWidth: 110 },
      { field: "securityName", headerName: "Security", minWidth: 220, flex: 1.2 },
      { field: "isin", headerName: "ISIN", minWidth: 170 },
      { field: "country", headerName: "Country", minWidth: 110 },
      {
        field: "grossAmount",
        headerName: "Gross amount",
        valueFormatter: (params) => formatCurrency(Number(params.value ?? 0))
      },
      { field: "withholdingTax", headerName: "Withholding tax", minWidth: 160 },
      { field: "netAmount", headerName: "Net amount", minWidth: 140 },
      { field: "currency", headerName: "Currency", minWidth: 110 }
    ],
    []
  );

  const summary = useMemo(() => {
    const realizedPnl = incomeFromSells.reduce((sum, row) => sum + Number(row.grossPnl ?? 0), 0);
    const grossProceeds = incomeFromSells.reduce((sum, row) => sum + Number(row.grossProceeds ?? 0), 0);
    const grossOtherIncome = otherIncomeFees.reduce((sum, row) => sum + Number(row.grossAmount ?? 0), 0);

    return {
      sellsCount: incomeFromSells.length,
      otherCount: otherIncomeFees.length,
      realizedPnl,
      grossProceeds,
      grossOtherIncome
    };
  }, [incomeFromSells, otherIncomeFees]);

  async function loadTables() {
    setLoadingData(true);
    setErrorMessage(null);

    try {
      const [sellsRows, otherRows] = await Promise.all([
        fetchIncomeFromSells(),
        fetchOtherIncomeFees()
      ]);

      setIncomeFromSells(sellsRows);
      setOtherIncomeFees(otherRows);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Failed to load database views.");
    } finally {
      setLoadingData(false);
    }
  }

  useEffect(() => {
    void loadTables();
  }, []);

  useEffect(() => {
    if (!importStatus || terminalStatuses.has(importStatus.status)) {
      return undefined;
    }

    const intervalId = window.setInterval(async () => {
      try {
        const nextStatus = await fetchImportStatus(importStatus.workflowId);
        setImportStatus(nextStatus);

        if (terminalStatuses.has(nextStatus.status)) {
          window.clearInterval(intervalId);
          setUploading(false);

          if (nextStatus.status === "COMPLETED") {
            void loadTables();
          } else {
            const failureMessage = nextStatus.failureMessages.join(" | ");
            if (failureMessage) {
              setErrorMessage(failureMessage);
            }
          }
        }
      } catch (error) {
        setUploading(false);
        setErrorMessage(error instanceof Error ? error.message : "Failed to poll the Temporal workflow.");
        window.clearInterval(intervalId);
      }
    }, 1500);

    return () => window.clearInterval(intervalId);
  }, [importStatus]);

  function handleFileSelected(file: File | null) {
    if (!file) {
      return;
    }

    setSelectedFile(file);
    setErrorMessage(null);
    setStatementName((currentName) => currentName || fileNameToStatementName(file.name));
  }

  async function handleUpload() {
    if (!selectedFile) {
      setErrorMessage("Select a CSV file before starting the import.");
      return;
    }

    if (!statementName.trim()) {
      setErrorMessage("Add a statement name before starting the import.");
      return;
    }

    setUploading(true);
    setErrorMessage(null);

    try {
      const uploadResponse = await uploadCsv(statementName.trim(), selectedFile);
      setImportStatus({
        workflowId: uploadResponse.importWorkflowId,
        statementName: uploadResponse.statementName,
        status: "RUNNING",
        createdAt: null,
        startedAt: null,
        completedAt: null,
        steps: [
          { stepName: "sellsStep", status: "RUNNING", readCount: 0, writeCount: 0 },
          { stepName: "otherIncomeStep", status: "RUNNING", readCount: 0, writeCount: 0 }
        ],
        failureMessages: []
      });
    } catch (error) {
      setUploading(false);
      setErrorMessage(error instanceof Error ? error.message : "Failed to upload CSV.");
    }
  }

  const dropZoneLabel = selectedFile
    ? `${selectedFile.name} (${Math.max(1, Math.round(selectedFile.size / 1024))} KB)`
    : "Drop a CSV here or browse from disk";

  const importStatusSummary = aggregateImportStatus(importStatus);

  return (
    <div className="app-shell">
      <header className="hero">
        <div>
          <p className="eyebrow">Trading statement ingestion</p>
          <h1>Load CSV data and inspect the imported database views.</h1>
        </div>
        <div className="hero-meta">
          <div>
            <span>Backend</span>
            <strong>Temporal + PostgreSQL</strong>
          </div>
          <div>
            <span>Frontend</span>
            <strong>React + TypeScript + AG Grid</strong>
          </div>
        </div>
      </header>

      <main className="page-grid">
        <section className="section-panel uploader-panel">
          <div className="section-heading">
            <div>
              <p className="section-kicker">1. Upload</p>
              <h2>Send a statement and start two batch runs.</h2>
            </div>
            <button className="secondary-button" type="button" onClick={() => void loadTables()}>
              Refresh tables
            </button>
          </div>

          <div
            className={`drop-zone ${dragActive ? "drag-active" : ""}`}
            onDragEnter={(event) => {
              event.preventDefault();
              setDragActive(true);
            }}
            onDragOver={(event) => {
              event.preventDefault();
              setDragActive(true);
            }}
            onDragLeave={(event) => {
              event.preventDefault();
              setDragActive(false);
            }}
            onDrop={(event) => {
              event.preventDefault();
              setDragActive(false);
              handleFileSelected(event.dataTransfer.files.item(0));
            }}
          >
            <input
              ref={fileInputRef}
              className="hidden-input"
              type="file"
              accept=".csv,text/csv"
              onChange={(event) => handleFileSelected(event.target.files?.item(0) ?? null)}
            />
            <div className="drop-zone-copy">
              <span className="drop-zone-icon">CSV</span>
              <div>
                <strong>{dropZoneLabel}</strong>
                <p>Expected broker statement format with the two batch sections already supported by the backend.</p>
              </div>
            </div>
            <button className="secondary-button" type="button" onClick={() => fileInputRef.current?.click()}>
              Browse
            </button>
          </div>

          <div className="form-row">
            <label className="field">
              <span>Statement name</span>
              <input
                value={statementName}
                onChange={(event) => setStatementName(event.target.value)}
                placeholder="2025 tax year statement"
              />
            </label>
            <button className="primary-button" type="button" onClick={handleUpload} disabled={uploading}>
              {uploading ? "Processing..." : "Start import"}
            </button>
          </div>

          {errorMessage ? <p className="error-banner">{errorMessage}</p> : null}
        </section>

        <section className="section-panel status-panel">
          <div className="section-heading compact">
            <div>
              <p className="section-kicker">2. Import status</p>
              <h2>Track the current Temporal workflow execution.</h2>
            </div>
          </div>

          <div className="status-chip-row">
            <div className={`status-chip status-${importStatusSummary.toLowerCase()}`}>
              {importStatusSummary}
            </div>
            {importStatus ? <span className="muted">Workflow {importStatus.workflowId}</span> : null}
          </div>

          {importStatus ? (
            <div className="job-status-list">
              <article className="job-status-card">
                <div className="job-status-header">
                  <div>
                    <strong>{importStatus.statementName}</strong>
                    <div className="muted">{importStatus.workflowId}</div>
                  </div>
                  <div className={`status-chip status-${importStatus.status.toLowerCase()}`}>
                    {importStatus.status}
                  </div>
                </div>

                <dl className="status-metadata">
                  <div>
                    <dt>Created</dt>
                    <dd>{formatDateTime(importStatus.createdAt)}</dd>
                  </div>
                  <div>
                    <dt>Started</dt>
                    <dd>{formatDateTime(importStatus.startedAt)}</dd>
                  </div>
                  <div>
                    <dt>Finished</dt>
                    <dd>{formatDateTime(importStatus.completedAt)}</dd>
                  </div>
                  <div>
                    <dt>Platform</dt>
                    <dd>Temporal workflow</dd>
                  </div>
                </dl>

                <div className="step-list">
                  {importStatus.steps.map((step) => (
                    <article key={step.stepName} className="step-item">
                      <div className="step-header">
                        <strong>{step.stepName}</strong>
                        <span>{step.status}</span>
                      </div>
                      <div className="step-stats">
                        <span>Read {step.readCount}</span>
                        <span>Written {step.writeCount}</span>
                      </div>
                    </article>
                  ))}
                </div>
              </article>
            </div>
          ) : (
            <p className="muted">No import launched yet.</p>
          )}
        </section>
      </main>

      <section className="summary-strip">
        <article>
          <span>Sell rows</span>
          <strong>{summary.sellsCount}</strong>
        </article>
        <article>
          <span>Other income rows</span>
          <strong>{summary.otherCount}</strong>
        </article>
        <article>
          <span>Gross proceeds</span>
          <strong>{formatCurrency(summary.grossProceeds)}</strong>
        </article>
        <article>
          <span>Realized P/L</span>
          <strong>{formatCurrency(summary.realizedPnl)}</strong>
        </article>
        <article>
          <span>Gross other income</span>
          <strong>{formatCurrency(summary.grossOtherIncome)}</strong>
        </article>
      </section>

      <section className="section-panel data-panel">
        <div className="section-heading">
          <div>
            <p className="section-kicker">3. Database views</p>
            <h2>Review the two imported tables with AG Grid.</h2>
          </div>
          <div className="toolbar">
            <div className="tab-group" role="tablist" aria-label="Database views">
              <button
                type="button"
                className={activeView === "sells" ? "tab active" : "tab"}
                onClick={() => setActiveView("sells")}
              >
                Income from sells
              </button>
              <button
                type="button"
                className={activeView === "other" ? "tab active" : "tab"}
                onClick={() => setActiveView("other")}
              >
                Other income & fees
              </button>
            </div>

            <label className="search-field">
              <span>Search</span>
              <input
                value={currentSearch}
                onChange={(event) =>
                  activeView === "sells"
                    ? setSellsSearch(event.target.value)
                    : setOtherSearch(event.target.value)
                }
                placeholder="Filter current grid"
              />
            </label>
          </div>
        </div>

        <div className="grid-stage">
          <div className={activeView === "sells" ? "grid-card active" : "grid-card hidden"}>
            <div className="ag-theme-quartz grid-surface">
              <AgGridReact<IncomeFromSellsRow>
                rowData={incomeFromSells}
                columnDefs={incomeFromSellsColumns}
                defaultColDef={defaultColDef}
                quickFilterText={sellsSearch}
                animateRows
                pagination
                paginationPageSize={12}
                suppressCellFocus
                loading={loadingData}
              />
            </div>
          </div>

          <div className={activeView === "other" ? "grid-card active" : "grid-card hidden"}>
            <div className="ag-theme-quartz grid-surface">
              <AgGridReact<OtherIncomeFeesRow>
                rowData={otherIncomeFees}
                columnDefs={otherIncomeColumns}
                defaultColDef={defaultColDef}
                quickFilterText={otherSearch}
                animateRows
                pagination
                paginationPageSize={12}
                suppressCellFocus
                loading={loadingData}
              />
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
