import { AgGridReact } from 'ag-grid-react';
import type { ColDef } from 'ag-grid-community';
import type { IncomeFromSellsRow } from '../api/fetchTables';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';

const columns: ColDef<IncomeFromSellsRow>[] = [
  { field: 'dateAcquired',  headerName: 'Date Acquired',  sortable: true, filter: true },
  { field: 'dateSold',      headerName: 'Date Sold',      sortable: true, filter: true },
  { field: 'symbol',        headerName: 'Symbol',         sortable: true, filter: true },
  { field: 'securityName',  headerName: 'Security Name',  sortable: true, filter: true, flex: 2 },
  { field: 'isin',          headerName: 'ISIN',           sortable: true },
  { field: 'country',       headerName: 'Country',        sortable: true, filter: true },
  { field: 'quantity',      headerName: 'Quantity',       sortable: true },
  { field: 'costBasis',     headerName: 'Cost Basis',     sortable: true },
  { field: 'grossProceeds', headerName: 'Gross Proceeds', sortable: true },
  { field: 'grossPnl',      headerName: 'Gross P&L',      sortable: true },
  { field: 'currency',      headerName: 'Currency' },
];

export default function IncomeFromSellsGrid({ rows }: { rows: IncomeFromSellsRow[] }) {
  return (
    <div>
      <h3 style={{ fontFamily: 'sans-serif', marginBottom: 8 }}>Income from Sells</h3>
      <div className="ag-theme-alpine" style={{ height: 400 }}>
        <AgGridReact rowData={rows} columnDefs={columns} defaultColDef={{ flex: 1, minWidth: 100 }} />
      </div>
    </div>
  );
}
