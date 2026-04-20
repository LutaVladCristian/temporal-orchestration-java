interface Props {
  state: 'uploading' | 'polling' | 'error';
}

const messages = {
  uploading: '⏳ Uploading file…',
  polling:   '⚙️ Processing CSV, please wait…',
  error:     '❌ An error occurred. Please try again.',
};

const colors = {
  uploading: '#e8f0fe',
  polling:   '#fff8e1',
  error:     '#fdecea',
};

export default function StatusBanner({ state }: Props) {
  return (
    <div style={{
      maxWidth: 520,
      margin: '0 auto 24px',
      padding: '14px 20px',
      borderRadius: 6,
      background: colors[state],
      fontFamily: 'sans-serif',
      fontSize: 14,
    }}>
      {messages[state]}
    </div>
  );
}
