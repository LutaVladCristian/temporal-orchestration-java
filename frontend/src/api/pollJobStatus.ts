import client from './client';

export type JobStatus = 'STARTING' | 'STARTED' | 'COMPLETED' | 'FAILED' | 'STOPPED';

export async function getJobStatus(executionId: number): Promise<JobStatus> {
  const { data } = await client.get<{ status: JobStatus }>(`/job-status/${executionId}`);
  return data.status;
}

export function pollUntilDone(
  executionId: number,
  onDone: () => void,
  onError: () => void,
  intervalMs = 2000
): () => void {
  const id = setInterval(async () => {
    try {
      const status = await getJobStatus(executionId);
      if (status === 'COMPLETED') { clearInterval(id); onDone(); }
      if (status === 'FAILED' || status === 'STOPPED') { clearInterval(id); onError(); }
    } catch {
      clearInterval(id);
      onError();
    }
  }, intervalMs);
  return () => clearInterval(id);
}
