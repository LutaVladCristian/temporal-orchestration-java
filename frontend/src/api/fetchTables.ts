import client from './client';

export interface IncomeFromSellsRow {
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
}

export interface OtherIncomeFeesRow {
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
}

export async function fetchIncomeFromSells(): Promise<IncomeFromSellsRow[]> {
  const { data } = await client.get<IncomeFromSellsRow[]>('/income-from-sells');
  return data;
}

export async function fetchOtherIncomeFees(): Promise<OtherIncomeFeesRow[]> {
  const { data } = await client.get<OtherIncomeFeesRow[]>('/other-income-fees');
  return data;
}
