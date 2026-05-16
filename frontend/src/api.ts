export type UploadResponse = {
  importWorkflowId: string;
  importRunId: string;
  statementName: string;
};

export type ImportStepStatus = {
  stepName: string;
  status: string;
  readCount: number;
  writeCount: number;
};

export type ImportStatus = {
  workflowId: string;
  statementName: string;
  status: string;
  createdAt: string | null;
  startedAt: string | null;
  completedAt: string | null;
  steps: ImportStepStatus[];
  failureMessages: string[];
};

export type IncomeFromSellsRow = {
  id: number;
  dateAcquired: string;
  dateSold: string;
  symbol: string;
  securityName: string;
  isin: string;
  country: string;
  quantity: number;
  costBasis: number;
  grossProceeds: number;
  grossPnl: number;
  currency: string;
};

export type OtherIncomeFeesRow = {
  id: number;
  date: string;
  symbol: string;
  securityName: string;
  isin: string;
  country: string;
  grossAmount: number;
  withholdingTax: string;
  netAmount: string;
  currency: string;
};

async function request<T>(input: RequestInfo, init?: RequestInit): Promise<T> {
  const response = await fetch(input, init);
  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with status ${response.status}`);
  }
  return response.json() as Promise<T>;
}

export async function uploadCsv(statementName: string, file: File): Promise<UploadResponse> {
  const formData = new FormData();
  formData.append("name", statementName);
  formData.append("file", file);

  return request<UploadResponse>("/spring-boot-api/upload-csv", {
    method: "POST",
    body: formData
  });
}

export async function fetchImportStatus(workflowId: string): Promise<ImportStatus> {
  return request<ImportStatus>(`/spring-boot-api/imports/${workflowId}`);
}

export async function fetchIncomeFromSells(): Promise<IncomeFromSellsRow[]> {
  return request<IncomeFromSellsRow[]>("/spring-boot-api/income-from-sells");
}

export async function fetchOtherIncomeFees(): Promise<OtherIncomeFeesRow[]> {
  return request<OtherIncomeFeesRow[]>("/spring-boot-api/other-income-fees");
}
