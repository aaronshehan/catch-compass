import { Link } from 'react-router-dom';

import { api } from '../api.js';
import { useLoad } from '../hooks/useLoad.js';
import { LoadState } from '../components/Field.jsx';
import { humanise, orNotRecorded } from '../format.js';

export default function TackleBox() {
  const { data: lures, error, loading } = useLoad(() => api.lures());

  return (
    <main>
      <h1>Tackle box</h1>

      <div className="actions">
        <Link className="button button--primary" to="/lures/new">
          Add a lure
        </Link>
        <Link className="button" to="/catches">
          Catch journal
        </Link>
      </div>

      <LoadState loading={loading} error={error} />

      {lures && lures.length === 0 && (
        <p className="empty">
          No lures yet. Add one and it will be selectable when you log a catch.
        </p>
      )}

      {lures && lures.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Lure</th>
                <th>Type</th>
                <th>Size</th>
                <th>Weight</th>
                <th>Presentation</th>
              </tr>
            </thead>
            <tbody>
              {lures.map((lure) => (
                <tr key={lure.id}>
                  <td>{lure.displayName}</td>
                  <td>{humanise(lure.type)}</td>
                  <td>{orNotRecorded(lure.size)}</td>
                  <td>{lure.weightGrams != null ? `${lure.weightGrams} g` : '-'}</td>
                  <td>{humanise(lure.presentation) ?? '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </main>
  );
}
