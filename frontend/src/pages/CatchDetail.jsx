import { Link, useParams } from 'react-router-dom';

import { api } from '../api.js';
import { useLoad } from '../hooks/useLoad.js';
import { LoadState } from '../components/Field.jsx';
import { dateTime, humanise, measurement, orNotRecorded } from '../format.js';

function Facts({ children }) {
  return <dl className="facts">{children}</dl>;
}

function Fact({ label, children }) {
  return (
    <>
      <dt>{label}</dt>
      <dd>{children}</dd>
    </>
  );
}

export default function CatchDetail() {
  const { id } = useParams();
  const { data: entry, error, loading } = useLoad(() => api.catchDetail(id), [id]);

  return (
    <main>
      <div className="actions">
        <Link className="button" to="/catches">
          Back to journal
        </Link>
      </div>

      <LoadState loading={loading} error={error} />

      {entry && (
        <>
          <h1>{entry.species.commonName}</h1>

          {entry.hasPhoto && (
            <img className="photo" src={entry.photoUrl} alt="Photo of the catch" />
          )}

          <Facts>
            <Fact label="Caught at">{dateTime(entry.caughtAt)}</Fact>
            <Fact label="Species">
              {entry.species.commonName}
              {entry.species.scientificName && (
                <span className="muted"> ({entry.species.scientificName})</span>
              )}
            </Fact>
            <Fact label="Weight">{measurement(entry.measurements.weightKg, 'kg')}</Fact>
            <Fact label="Length">{measurement(entry.measurements.lengthCm, 'cm')}</Fact>
            <Fact label="Circumference">
              {measurement(entry.measurements.circumferenceCm, 'cm')}
            </Fact>
            <Fact label="Location">
              {entry.location ? (
                <>
                  {entry.location.latitude}, {entry.location.longitude}
                  {entry.location.accuracyMeters != null && (
                    <span className="muted"> (+/- {entry.location.accuracyMeters} m)</span>
                  )}
                </>
              ) : (
                'Not recorded'
              )}
            </Fact>
            <Fact label="Location read at">
              {dateTime(entry.location?.recordedAt)}
            </Fact>
            <Fact label="Notes">{orNotRecorded(entry.notes)}</Fact>
          </Facts>

          <h2>Lure</h2>
          {entry.lure ? (
            <Facts>
              <Fact label="Lure">{entry.lure.displayName}</Fact>
              <Fact label="Type">{humanise(entry.lure.type)}</Fact>
              <Fact label="Size">{orNotRecorded(entry.lure.size)}</Fact>
              <Fact label="Weight">{measurement(entry.lure.weightGrams, 'g')}</Fact>
              <Fact label="Presentation">
                {humanise(entry.lure.presentation) ?? 'Not recorded'}
              </Fact>
              <Fact label="Tackle box">
                {entry.lure.stillInTackleBox ? 'Still listed' : 'No longer in your tackle box'}
              </Fact>
            </Facts>
          ) : (
            <p className="empty">No lure recorded.</p>
          )}

          <h2>Conditions</h2>
          {entry.conditions ? (
            <Facts>
              <Fact label="Air temperature">
                {measurement(entry.conditions.airTemperatureC, 'C')}
              </Fact>
              <Fact label="Water temperature">
                {measurement(entry.conditions.waterTemperatureC, 'C')}
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
                  'Not recorded'
                )}
              </Fact>
              <Fact label="Sky">
                {humanise(entry.conditions.skyCondition) ?? 'Not recorded'}
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
                  'Not recorded'
                )}
              </Fact>
              <Fact label="Observed at">{dateTime(entry.conditions.observedAt)}</Fact>
              <Fact label="Source">{humanise(entry.conditions.source)}</Fact>
            </Facts>
          ) : (
            <p className="empty">No conditions recorded.</p>
          )}
        </>
      )}
    </main>
  );
}
