import { Link } from 'react-router-dom';

import { api } from '../api.js';
import { useLoad } from '../hooks/useLoad.js';
import { Skeleton } from '../components/Field.jsx';
import { dateTime } from '../format.js';

function EmptyThumb() {
  return (
    <span className="catch-card__thumb catch-card__thumb--empty" aria-hidden="true">
      <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor"
           strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
        <path d="M2 12c3.5-5 7-7 11-7 4 0 7 3 9 7-2 4-5 7-9 7-4 0-7.5-2-11-7Z" />
        <circle cx="17" cy="11" r="0.8" fill="currentColor" stroke="none" />
      </svg>
    </span>
  );
}

export default function Journal() {
  const { data: catches, error, loading } = useLoad(() => api.journal());

  return (
    <main>
      <h1>Catch journal</h1>

      <div className="actions">
        <Link className="button button--primary" to="/catches/new">
          Log a catch
        </Link>
      </div>

      {error && <p className="notice notice--error">{error}</p>}
      {loading && <Skeleton count={3} />}

      {catches && catches.length === 0 && (
        <p className="empty">
          Nothing logged yet. Your first catch will show up here.
        </p>
      )}

      {catches && catches.length > 0 && (
        <ul className="catch-list">
          {catches.map((entry) => (
            <li key={entry.id}>
              <Link className="catch-card" to={`/catches/${entry.id}`}>
                {entry.hasPhoto ? (
                  <img className="catch-card__thumb" src={entry.photoUrl} alt="" loading="lazy" />
                ) : (
                  <EmptyThumb />
                )}

                <span className="catch-card__body">
                  <span className="catch-card__title">{entry.species}</span>
                  <span className="catch-card__date">{dateTime(entry.caughtAt)}</span>

                  {(entry.weightKg != null || entry.lengthCm != null) && (
                    <span className="catch-card__tags">
                      {entry.weightKg != null && <span className="tag">{entry.weightKg} kg</span>}
                      {entry.lengthCm != null && <span className="tag">{entry.lengthCm} cm</span>}
                    </span>
                  )}
                </span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
