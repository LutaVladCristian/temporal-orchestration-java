import client from './client';

export async function uploadCsv(name: string, file: File): Promise<number> {
  const form = new FormData();
  form.append('name', name);
  form.append('file', file);
  const { data } = await client.post<{ jobExecutionId: number }>('/upload-csv', form);
  return data.jobExecutionId;
}
