import { AgGridReact } from 'ag-grid-react';
import type { ColDef } from 'ag-grid-community';
import type { OtherIncomeFeesRow } from '../api/fetchTables';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';

const columns: ColDef<OtherIncomeFeesRow>[] = [
  { field: 'date',           headerName: 'Date',            sortable: true, filter: true },
  { field: 'symbol',         headerName: 'Symbol',          sortable: true, filter: true },
  { field: 'securityName',   headerName: 'Security Name',   sortable: true, filter: true, flex: 2 },
  { field: 'isin',           headerName: 'ISIN',            sortable: true },
  { field: 'country',        headerName: 'Country',         sortable: true, filter: true },
  { field: 'grossAmount',    headerName: 'Gross Amount',    sortable: true },
  { field: 'withholdingTax', headerName: 'Withholding Tax', sortable: true },
  { field: 'netAmount',      headerName: 'Net Amount',      sortable: true },
  { field: 'currency',       headerName: 'Currency' },
];

export default function OtherIncomeFeesGrid({ rows }: { rows: OtherIncomeFeesRow[] }) {
  return (
    <div>
      <h3 style={{ fontFamily: 'sans-serif', marginBottom: 8 }}>Other Income &amp; Fees</h3>
      <div className="ag-theme-alpine" style={{ height: 400 }}>
        <AgGridReact rowData={rows} columnDefs={columns} defaultColDef={{ flex: 1, minWidth: 100 }} />
      </div>
    </div>
  );
}
