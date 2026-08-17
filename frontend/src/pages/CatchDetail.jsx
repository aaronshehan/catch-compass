import { Link, useParams } from 'react-router-dom';

import { api } from '../api.js';
import { useLoad } from '../hooks/useLoad.js';
import { Skeleton } from '../components/Field.jsx';
import { dateTime, humanise, measurement, orNotRecorded } from '../format.js';

function Stat({ value, unit, label }) {
  const missing = value === null || value === undefined;
  return (
    <div className="stat">
      <span className={missing ? 'stat__value stat__value--none' : 'stat__value'}>
        {missing ? '–' : `${value}`}
        {!missing && unit && <span className="muted" style={{ fontSize: '0.8rem' }}> {unit}</span>}
      </span>
      <span className="stat__label">{label}</span>
    </div>
  );
}

function Facts({ children }) {
  return (
    <div className="panel">
      <dl className="facts">{children}</dl>
    </div>
  );
}

function Fact({ label, children }) {
  return (
    <div>
      <dt>{label}</dt>
      <dd>{children}</dd>
    </div>
  );
}

export default function CatchDetail() {
  const { id } = useParams();
  const { data: entry, error, loading } = useLoad(() => api.catchDetail(id), [id]);

  return (
    <main>
      <Link className="back-link" to="/catches">
        &#8592; Journal
      </Link>

      {error && <p className="notice notice--error">{error}</p>}
      {loading && <Skeleton count={2} />}

      {entry && (
        <>
          {entry.hasPhoto && (
            <img className="hero-photo" src={entry.photoUrl} alt="Photo of the catch" />
          )}

          <h1>{entry.species.commonName}</h1>

          <div className="stat-row">
            <Stat value={entry.measurements.weightKg} unit="kg" label="Weight" />
            <Stat value={entry.measurements.lengthCm} unit="cm" label="Length" />
            <Stat value={entry.measurements.circumferenceCm} unit="cm" label="Girth" />
          </div>

          <Facts>
            <Fact label="Caught">{dateTime(entry.caughtAt)}</Fact>
            <Fact label="Species">
              {entry.species.scientificName ? (
                <em className="muted">{entry.species.scientificName}</em>
              ) : (
                entry.species.commonName
              )}
            </Fact>
            <Fact label="Location">
              {entry.location ? (
                <>
                  {entry.location.latitude}, {entry.location.longitude}
                  {entry.location.accuracyMeters != null && (
                    <span className="muted"> &plusmn;{entry.location.accuracyMeters} m</span>
                  )}
                </>
              ) : (
                <span className="muted">Not recorded</span>
              )}
            </Fact>
            {entry.notes && <Fact label="Notes">{entry.notes}</Fact>}
          </Facts>

          <span className="section-label">Lure</span>
          {entry.lure ? (
            <Facts>
              <Fact label="Type">{humanise(entry.lure.type)}</Fact>
              {entry.lure.description && (
                <Fact label="Details">{entry.lure.description}</Fact>
              )}
            </Facts>
          ) : (
            <p className="empty">No lure recorded.</p>
          )}

          <span className="section-label">Conditions</span>
          {entry.conditions ? (
            <Facts>
              <Fact label="Air">{measurement(entry.conditions.airTemperatureC, '°C')}</Fact>
              <Fact label="Water">
                {measurement(entry.conditions.waterTemperatureC, '°C')}
              </Fact>
              <Fact label="Wind">
                {entry.conditions.windSpeedMetersPerSecond != null ||
                entry.conditions.windDirectionDegrees != null ? (
                  <>
                    {entry.conditions.windSpeedMetersPerSecond != null &&
                      `${entry.conditions.windSpeedMetersPerSecond} m/s`}
                    {entry.conditions.windDirectionLabel &&
                      ` from ${entry.conditions.windDirectionLabel}`}
                  </>
                ) : (
                  <span className="muted">Not recorded</span>
                )}
              </Fact>
              <Fact label="Sky">
                {humanise(entry.conditions.skyCondition) ?? (
                  <span className="muted">Not recorded</span>
                )}
              </Fact>
              <Fact label="Pressure">
                {measurement(entry.conditions.barometricPressureHpa, 'hPa')}
              </Fact>
              <Fact label="Tide">
                {entry.conditions.tideState || entry.conditions.tideHeightMeters != null ? (
                  <>
                    {humanise(entry.conditions.tideState)}
                    {entry.conditions.tideHeightMeters != null &&
                      ` at ${entry.conditions.tideHeightMeters} m`}
                  </>
                ) : (
                  <span className="muted">Not recorded</span>
                )}
              </Fact>
              <Fact label="Source">
                <span className="muted">{humanise(entry.conditions.source)}</span>
              </Fact>
            </Facts>
          ) : (
            <p className="empty">No conditions recorded.</p>
          )}
        </>
      )}
    </main>
  );
}
