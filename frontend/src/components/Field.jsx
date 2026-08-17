/**
 * Label, input and error message as one unit.
 *
 * The error comes straight from the server's `errors` map, keyed by field name,
 * so a new validation rule on the backend shows up here with no frontend change.
 */
export function Field({ id, label, error, children }) {
  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      {children}
      {error && <span className="error">{error}</span>}
    </div>
  );
}

/** Renders whichever of loading / error / nothing applies, or null when ready. */
export function LoadState({ loading, error }) {
  if (error) return <p className="notice notice--error">{error}</p>;
  if (loading) return <p className="muted">Loading...</p>;
  return null;
}
