import { Link } from 'react-router-dom';

import { api } from '../api.js';
import { useLoad } from '../hooks/useLoad.js';
import { Skeleton } from '../components/Field.jsx';
import { humanise } from '../format.js';

export default function TackleBox() {
  const { data: lures, error, loading } = useLoad(() => api.lures());

  return (
    <main>
      <h1>Tackle box</h1>

      <div className="actions">
        <Link className="button button--primary" to="/lures/new">
          Add a lure
        </Link>
      </div>

      {error && <p className="notice notice--error">{error}</p>}
      {loading && <Skeleton count={3} />}

      {lures && lures.length === 0 && (
        <p className="empty">
          No lures yet. Add one and it becomes selectable when you log a catch.
        </p>
      )}

      {lures && lures.length > 0 && (
        <ul className="lure-list">
          {lures.map((lure) => (
            <li key={lure.id} className="lure-card">
              <div className="lure-card__name">{lure.displayName}</div>
              <div className="lure-card__meta">
                <span className="chip">{humanise(lure.type)}</span>
                {lure.size && <span className="chip">{lure.size}</span>}
                {lure.weightGrams != null && <span className="chip">{lure.weightGrams} g</span>}
                {lure.presentation && <span className="chip">{humanise(lure.presentation)}</span>}
              </div>
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
