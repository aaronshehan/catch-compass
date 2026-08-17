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

/**
 * Shows the shape of what is coming rather than the word "Loading".
 * On a slow connection this is the difference between a page that feels
 * broken and one that feels like it is working.
 */
export function Skeleton({ count = 3 }) {
  return (
    <div className="skeleton" aria-hidden="true">
      {Array.from({ length: count }, (_, i) => (
        <div key={i} className="skeleton__card" />
      ))}
    </div>
  );
}

/** A collapsible section, so optional fields do not make a wall on a phone. */
export function Group({ title, hint, open = false, children }) {
  return (
    <details className="group" open={open}>
      <summary>
        <span>{title}</span>
        {hint && <span className="summary-hint">{hint}</span>}
      </summary>
      <div className="group__body">{children}</div>
    </details>
  );
}
