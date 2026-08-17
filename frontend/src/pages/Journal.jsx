import { Link } from 'react-router-dom';

import { api } from '../api.js';
import { useLoad } from '../hooks/useLoad.js';
import { Skeleton } from '../components/Field.jsx';
import { logDate } from '../format.js';

function EmptyThumb() {
  return (
    <span className="log-row__thumb log-row__thumb--empty" aria-hidden="true">
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
        <ol className="log">
          {catches.map((entry, index) => (
            <li key={entry.id}>
              <Link className="log-row" to={`/catches/${entry.id}`}>
                {/* Entry number: newest first, so the count runs down the page
                    like a ledger written from the back. */}
                <span className="log-row__num">
                  {String(catches.length - index).padStart(2, '0')}
                </span>

                {entry.hasPhoto ? (
                  <img className="log-row__thumb" src={entry.photoUrl} alt="" loading="lazy" />
                ) : (
                  <EmptyThumb />
                )}

                <span className="log-row__body">
                  <span className="log-row__species">{entry.species}</span>
                  <span className="log-row__date">{logDate(entry.caughtAt)}</span>
                </span>

                <span className="log-row__data">
                  {entry.weightKg != null && (
                    <>
                      {entry.weightKg} kg
                      <br />
                    </>
                  )}
                  {entry.lengthCm != null && <>{entry.lengthCm} cm</>}
                </span>
              </Link>
            </li>
          ))}
        </ol>
      )}
    </main>
  );
}
