import { useState, useCallback } from 'react';
import DropZone from './components/DropZone';
import StatusBanner from './components/StatusBanner';
import IncomeFromSellsGrid from './components/IncomeFromSellsGrid';
import OtherIncomeFeesGrid from './components/OtherIncomeFeesGrid';
import { uploadCsv } from './api/uploadCsv';
import { pollUntilDone } from './api/pollJobStatus';
import { fetchIncomeFromSells, fetchOtherIncomeFees } from './api/fetchTables';
import type { IncomeFromSellsRow, OtherIncomeFeesRow } from './api/fetchTables';

type AppState = 'idle' | 'uploading' | 'polling' | 'done' | 'error';

export default function App() {
  const [appState, setAppState]       = useState<AppState>('idle');
  const [name, setName]               = useState('');
  const [file, setFile]               = useState<File | null>(null);
  const [sells, setSells]             = useState<IncomeFromSellsRow[]>([]);
  const [otherIncome, setOtherIncome] = useState<OtherIncomeFeesRow[]>([]);

  const handleUpload = useCallback(async () => {
    if (!file || !name.trim()) return;
    setAppState('uploading');
    try {
      const jobExecutionId = await uploadCsv(name, file);
      setAppState('polling');
      pollUntilDone(
        jobExecutionId,
        async () => {
          const [sellsData, otherData] = await Promise.all([
            fetchIncomeFromSells(),
            fetchOtherIncomeFees(),
          ]);
          setSells(sellsData);
          setOtherIncome(otherData);
          setAppState('done');
        },
        () => setAppState('error'),
      );
    } catch {
      setAppState('error');
    }
  }, [file, name]);

  const handleRetry = () => {
    setAppState('idle');
    setFile(null);
    setName('');
  };

  if (appState === 'done') {
    return (
      <div style={{ padding: '40px 32px' }}>
        <h2 style={{ fontFamily: 'sans-serif', marginBottom: 32 }}>Results — {name}</h2>
        <div style={{ marginBottom: 48 }}>
          <IncomeFromSellsGrid rows={sells} />
        </div>
        <OtherIncomeFeesGrid rows={otherIncome} />
        <button onClick={handleRetry} style={{ marginTop: 32, padding: '10px 24px', cursor: 'pointer' }}>
          Upload another file
        </button>
      </div>
    );
  }

  const busy = appState === 'uploading' || appState === 'polling';

  return (
    <div>
      {(appState === 'uploading' || appState === 'polling' || appState === 'error') && (
        <StatusBanner state={appState === 'error' ? 'error' : appState} />
      )}
      <DropZone
        name={name}
        onNameChange={setName}
        file={file}
        onFileDrop={setFile}
        onUpload={handleUpload}
        disabled={busy}
      />
      {appState === 'error' && (
        <div style={{ textAlign: 'center', marginTop: 16 }}>
          <button onClick={handleRetry} style={{ padding: '8px 20px', cursor: 'pointer' }}>
            Try again
          </button>
        </div>
      )}
    </div>
  );
}
