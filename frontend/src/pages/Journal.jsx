import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

import { api } from '../api.js';
import { dateTime } from '../format.js';

export default function Journal() {
  const [catches, setCatches] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;

    api
      .journal()
      .then((data) => {
        if (!cancelled) setCatches(data);
      })
      .catch((e) => {
        if (!cancelled) setError(e.message);
      });

    // Guards against setting state after the component is gone, which React's
    // StrictMode makes obvious in development by mounting everything twice.
    return () => {
      cancelled = true;
    };
  }, []);

  if (error) {
    return (
      <main>
        <h1>Catch journal</h1>
        <p className="notice notice--error">{error}</p>
      </main>
    );
  }

  if (catches === null) {
    return (
      <main>
        <h1>Catch journal</h1>
        <p className="muted">Loading...</p>
      </main>
    );
  }

  return (
    <main>
      <h1>Catch journal</h1>

      <div className="actions">
        <Link className="button button--primary" to="/catches/new">
          Log a catch
        </Link>
        <Link className="button" to="/lures">
          Tackle box
        </Link>
      </div>

      {catches.length === 0 ? (
        <p className="empty">No catches logged yet.</p>
      ) : (
        <ul className="catch-list">
          {catches.map((entry) => (
            <li key={entry.id}>
              <Link className="catch-card" to={`/catches/${entry.id}`}>
                {entry.hasPhoto ? (
                  <img className="catch-card__thumb" src={entry.photoUrl} alt="" />
                ) : (
                  <span
                    className="catch-card__thumb catch-card__thumb--empty"
                    aria-hidden="true"
                  >
                    &#9679;
                  </span>
                )}

                <span className="catch-card__body">
                  <span className="catch-card__title">{entry.species}</span>
                  <span className="catch-card__meta">
                    {dateTime(entry.caughtAt)}
                    {entry.weightKg != null && ` - ${entry.weightKg} kg`}
                    {entry.lengthCm != null && ` - ${entry.lengthCm} cm`}
                  </span>
                </span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
