export type UploadResponse = {
  sellsJobExecutionId: number;
  otherIncomeJobExecutionId: number;
  statementName: string;
};

export type StepStatus = {
  stepName: string;
  status: string;
  readCount: number;
  writeCount: number;
  commitCount: number;
};

export type JobStatus = {
  executionId: number;
  jobName: string;
  status: string;
  exitCode: string;
  createTime: string | null;
  startTime: string | null;
  endTime: string | null;
  lastUpdated: string | null;
  steps: StepStatus[];
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

export async function fetchJobStatus(executionId: number): Promise<JobStatus> {
  return request<JobStatus>(`/spring-boot-api/job-status/${executionId}`);
}

export async function fetchIncomeFromSells(): Promise<IncomeFromSellsRow[]> {
  return request<IncomeFromSellsRow[]>("/spring-boot-api/income-from-sells");
}

export async function fetchOtherIncomeFees(): Promise<OtherIncomeFeesRow[]> {
  return request<OtherIncomeFeesRow[]>("/spring-boot-api/other-income-fees");
}
