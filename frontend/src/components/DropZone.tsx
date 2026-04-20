import { useDropzone } from 'react-dropzone';

interface Props {
  name: string;
  onNameChange: (v: string) => void;
  file: File | null;
  onFileDrop: (f: File) => void;
  onUpload: () => void;
  disabled: boolean;
}

export default function DropZone({ name, onNameChange, file, onFileDrop, onUpload, disabled }: Props) {
  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    accept: { 'text/csv': ['.csv'] },
    multiple: false,
    disabled,
    onDrop: (files) => files[0] && onFileDrop(files[0]),
  });

  return (
    <div style={{ maxWidth: 520, margin: '80px auto', fontFamily: 'sans-serif' }}>
      <h2 style={{ marginBottom: 24 }}>Trading Tax Calculator</h2>

      <label style={{ display: 'block', marginBottom: 8, fontWeight: 600 }}>Statement name</label>
      <input
        value={name}
        onChange={(e) => onNameChange(e.target.value)}
        disabled={disabled}
        placeholder="e.g. 2025 tax year"
        style={{ width: '100%', padding: '8px 12px', marginBottom: 20, boxSizing: 'border-box', fontSize: 14 }}
      />

      <div
        {...getRootProps()}
        style={{
          border: `2px dashed ${isDragActive ? '#4f6ef7' : '#aaa'}`,
          borderRadius: 8,
          padding: '48px 24px',
          textAlign: 'center',
          background: isDragActive ? '#f0f3ff' : '#fafafa',
          cursor: disabled ? 'not-allowed' : 'pointer',
          transition: 'all 0.15s',
        }}
      >
        <input {...getInputProps()} />
        {file
          ? <p style={{ margin: 0, color: '#333' }}>📄 {file.name}</p>
          : <p style={{ margin: 0, color: '#888' }}>
              {isDragActive ? 'Drop the CSV here' : 'Drag & drop your CSV, or click to select'}
            </p>
        }
      </div>

      <button
        onClick={onUpload}
        disabled={disabled || !file || !name.trim()}
        style={{
          marginTop: 20,
          width: '100%',
          padding: '12px',
          background: '#4f6ef7',
          color: '#fff',
          border: 'none',
          borderRadius: 6,
          fontSize: 15,
          cursor: disabled || !file || !name.trim() ? 'not-allowed' : 'pointer',
          opacity: disabled || !file || !name.trim() ? 0.5 : 1,
        }}
      >
        Upload &amp; Process
      </button>
    </div>
  );
}
