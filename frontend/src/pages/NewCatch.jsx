import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import { api, ApiError } from '../api.js';
import { useLoad } from '../hooks/useLoad.js';
import { Field, LoadState } from '../components/Field.jsx';
import { humanise } from '../format.js';

const EMPTY_CATCH = {
  speciesId: '',
  caughtAt: '',
  lureId: '',
  weightKg: '',
  lengthCm: '',
  circumferenceCm: '',
  notes: '',
  latitude: '',
  longitude: '',
  locationAccuracyMeters: '',
  locationRecordedAt: '',
};

const EMPTY_CONDITIONS = {
  airTemperatureC: '',
  waterTemperatureC: '',
  windSpeedMetersPerSecond: '',
  windDirectionDegrees: '',
  tideHeightMeters: '',
  tideState: '',
  barometricPressureHpa: '',
  skyCondition: '',
  observedAt: '',
  conditionsSource: 'MANUAL',
};

/** Skips blanks so optional fields arrive absent rather than as empty strings. */
function append(data, name, value) {
  if (value !== null && value !== undefined && value !== '') {
    data.append(name, value);
  }
}

export default function NewCatch() {
  const navigate = useNavigate();

  const { data, error: loadError, loading } = useLoad(() =>
    Promise.all([api.species(), api.lures(), api.conditionsOptions()]).then(
      ([species, lures, options]) => ({ species, lures, options }),
    ),
  );

  const [form, setForm] = useState(EMPTY_CATCH);
  const [conditions, setConditions] = useState(EMPTY_CONDITIONS);
  const [photo, setPhoto] = useState(null);

  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState(null);
  const [saving, setSaving] = useState(false);

  const [locationStatus, setLocationStatus] = useState('');
  const [locating, setLocating] = useState(false);
  const [conditionsStatus, setConditionsStatus] = useState('');
  const [fetchingConditions, setFetchingConditions] = useState(false);

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function updateConditions(field, value) {
    setConditions((current) => ({
      ...current,
      [field]: value,
      // Touching a fetched value downgrades the source, which is what the
      // conditions_source column exists to record.
      conditionsSource:
        current.conditionsSource === 'WEATHER_API'
          ? 'WEATHER_API_EDITED'
          : current.conditionsSource,
    }));
  }

  function useMyLocation() {
    if (!('geolocation' in navigator)) {
      setLocationStatus('This browser cannot provide your location. Enter coordinates below.');
      return;
    }
    if (!window.isSecureContext) {
      setLocationStatus('Location needs an HTTPS connection. Enter coordinates below.');
      return;
    }

    setLocating(true);
    setLocationStatus('Finding your location...');

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude, accuracy } = position.coords;
        setForm((current) => ({
          ...current,
          latitude: latitude.toFixed(6),
          longitude: longitude.toFixed(6),
          locationAccuracyMeters: accuracy ? accuracy.toFixed(2) : '',
          locationRecordedAt: new Date(position.timestamp).toISOString(),
        }));
        setLocationStatus(
          `Location captured, accurate to about ${Math.round(accuracy)} m. ` +
            'Check it below and correct it if it looks wrong.',
        );
        setLocating(false);
      },
      (e) => {
        setLocationStatus(explainLocationFailure(e));
        setLocating(false);
      },
      { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 },
    );
  }

  function clearLocation() {
    setForm((current) => ({
      ...current,
      latitude: '',
      longitude: '',
      locationAccuracyMeters: '',
      locationRecordedAt: '',
    }));
    setLocationStatus('Location cleared. This catch will be saved without coordinates.');
  }

  async function lookUpConditions() {
    if (!form.latitude || !form.longitude) {
      setConditionsStatus('Set a location first - conditions are looked up for that spot.');
      return;
    }
    if (!form.caughtAt) {
      setConditionsStatus('Set the catch time first - conditions are looked up for that moment.');
      return;
    }

    setFetchingConditions(true);
    setConditionsStatus('Looking up conditions...');

    try {
      const result = await api.conditions(
        form.latitude,
        form.longitude,
        new Date(form.caughtAt).toISOString(),
      );

      if (!result.weatherAvailable) {
        setConditionsStatus(
          'No weather data for that place and time. Enter conditions manually below.',
        );
        return;
      }

      setConditions((current) => ({
        ...current,
        airTemperatureC: result.airTemperatureC ?? current.airTemperatureC,
        windSpeedMetersPerSecond:
          result.windSpeedMetersPerSecond ?? current.windSpeedMetersPerSecond,
        windDirectionDegrees: result.windDirectionDegrees ?? current.windDirectionDegrees,
        barometricPressureHpa: result.barometricPressureHpa ?? current.barometricPressureHpa,
        skyCondition: result.skyCondition ?? current.skyCondition,
        tideHeightMeters: result.tideHeightMeters ?? current.tideHeightMeters,
        tideState: result.tideState ?? current.tideState,
        observedAt: result.observedAt ? result.observedAt.substring(0, 16) : current.observedAt,
        conditionsSource: 'WEATHER_API',
      }));

      setConditionsStatus(
        'Conditions filled in from Open-Meteo. Check them and correct anything that looks wrong.' +
          (result.tideState ? '' : ' Tide data is not available from this provider.'),
      );
    } catch {
      setConditionsStatus('Could not reach the weather service. Enter conditions manually below.');
    } finally {
      setFetchingConditions(false);
    }
  }

  async function submit(event) {
    event.preventDefault();
    setSaving(true);
    setErrors({});
    setMessage(null);

    const body = new FormData();
    Object.entries(form).forEach(([name, value]) => append(body, name, value));
    Object.entries(conditions).forEach(([name, value]) =>
      append(body, `conditions.${name}`, value),
    );
    if (photo) {
      body.append('photo', photo);
    }

    try {
      const saved = await api.createCatch(body);
      navigate(`/catches/${saved.id}`);
    } catch (e) {
      if (e instanceof ApiError) {
        setErrors(e.fieldErrors);
        setMessage(e.message);
      } else {
        setMessage('Something went wrong saving the catch.');
      }
      setSaving(false);
    }
  }

  return (
    <main>
      <div className="actions">
        <Link className="button" to="/catches">
          Back to journal
        </Link>
      </div>

      <h1>Log a catch</h1>

      <LoadState loading={loading} error={loadError} />
      {message && <p className="notice notice--error">{message}</p>}
      {errors.locationPairComplete && (
        <p className="notice notice--error">{errors.locationPairComplete}</p>
      )}

      {data && (
        <form onSubmit={submit}>
          <fieldset>
            <legend>The catch</legend>

            <Field id="speciesId" label="Species" error={errors.speciesId}>
              <select
                id="speciesId"
                value={form.speciesId}
                onChange={(e) => update('speciesId', e.target.value)}
                aria-invalid={errors.speciesId ? 'true' : undefined}
              >
                <option value="">Choose a species</option>
                {data.species.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.commonName}
                  </option>
                ))}
              </select>
            </Field>

            <Field id="caughtAt" label="Caught at" error={errors.caughtAt}>
              <input
                id="caughtAt"
                type="datetime-local"
                value={form.caughtAt}
                onChange={(e) => update('caughtAt', e.target.value)}
                aria-invalid={errors.caughtAt ? 'true' : undefined}
              />
            </Field>

            <Field id="lureId" label="Lure" error={errors.lureId}>
              <select
                id="lureId"
                value={form.lureId}
                onChange={(e) => update('lureId', e.target.value)}
              >
                <option value="">No lure recorded</option>
                {data.lures.map((lure) => (
                  <option key={lure.id} value={lure.id}>
                    {lure.displayName}
                  </option>
                ))}
              </select>
            </Field>

            <Field id="photo" label="Photo" error={errors.photo}>
              <input
                id="photo"
                type="file"
                accept="image/jpeg,image/png"
                capture="environment"
                onChange={(e) => setPhoto(e.target.files[0] ?? null)}
              />
            </Field>
          </fieldset>

          <fieldset>
            <legend>Measurements (optional)</legend>

            <Field id="weightKg" label="Weight (kg)" error={errors.weightKg}>
              <input
                id="weightKg"
                type="number"
                step="0.001"
                value={form.weightKg}
                onChange={(e) => update('weightKg', e.target.value)}
                aria-invalid={errors.weightKg ? 'true' : undefined}
              />
            </Field>

            <Field id="lengthCm" label="Length (cm)" error={errors.lengthCm}>
              <input
                id="lengthCm"
                type="number"
                step="0.01"
                value={form.lengthCm}
                onChange={(e) => update('lengthCm', e.target.value)}
                aria-invalid={errors.lengthCm ? 'true' : undefined}
              />
            </Field>

            <Field
              id="circumferenceCm"
              label="Circumference (cm)"
              error={errors.circumferenceCm}
            >
              <input
                id="circumferenceCm"
                type="number"
                step="0.01"
                value={form.circumferenceCm}
                onChange={(e) => update('circumferenceCm', e.target.value)}
                aria-invalid={errors.circumferenceCm ? 'true' : undefined}
              />
            </Field>
          </fieldset>

          <fieldset>
            <legend>Location (optional)</legend>

            <div className="actions">
              <button type="button" onClick={useMyLocation} disabled={locating}>
                {locating ? 'Locating...' : 'Use my current location'}
              </button>
              <button type="button" onClick={clearLocation}>
                Clear location
              </button>
            </div>

            {locationStatus && (
              <p className="notice" role="status" aria-live="polite">
                {locationStatus}
              </p>
            )}

            <Field id="latitude" label="Latitude" error={errors.latitude}>
              <input
                id="latitude"
                type="number"
                step="any"
                value={form.latitude}
                onChange={(e) => update('latitude', e.target.value)}
                aria-invalid={errors.latitude ? 'true' : undefined}
              />
            </Field>

            <Field id="longitude" label="Longitude" error={errors.longitude}>
              <input
                id="longitude"
                type="number"
                step="any"
                value={form.longitude}
                onChange={(e) => update('longitude', e.target.value)}
                aria-invalid={errors.longitude ? 'true' : undefined}
              />
            </Field>

            <Field
              id="locationAccuracyMeters"
              label="Accuracy (metres)"
              error={errors.locationAccuracyMeters}
            >
              <input
                id="locationAccuracyMeters"
                type="number"
                step="0.01"
                value={form.locationAccuracyMeters}
                onChange={(e) => update('locationAccuracyMeters', e.target.value)}
              />
            </Field>
          </fieldset>

          <fieldset>
            <legend>Conditions (optional)</legend>

            <div className="actions">
              <button type="button" onClick={lookUpConditions} disabled={fetchingConditions}>
                {fetchingConditions ? 'Looking up...' : 'Look up conditions'}
              </button>
            </div>

            {conditionsStatus && (
              <p className="notice" role="status" aria-live="polite">
                {conditionsStatus}
              </p>
            )}

            <Field
              id="airTemperatureC"
              label="Air temperature (C)"
              error={errors['conditions.airTemperatureC']}
            >
              <input
                id="airTemperatureC"
                type="number"
                step="0.1"
                value={conditions.airTemperatureC}
                onChange={(e) => updateConditions('airTemperatureC', e.target.value)}
                aria-invalid={errors['conditions.airTemperatureC'] ? 'true' : undefined}
              />
            </Field>

            <Field
              id="waterTemperatureC"
              label="Water temperature (C)"
              error={errors['conditions.waterTemperatureC']}
            >
              <input
                id="waterTemperatureC"
                type="number"
                step="0.1"
                value={conditions.waterTemperatureC}
                onChange={(e) => updateConditions('waterTemperatureC', e.target.value)}
                aria-invalid={errors['conditions.waterTemperatureC'] ? 'true' : undefined}
              />
            </Field>

            <Field
              id="windSpeedMetersPerSecond"
              label="Wind speed (m/s)"
              error={errors['conditions.windSpeedMetersPerSecond']}
            >
              <input
                id="windSpeedMetersPerSecond"
                type="number"
                step="0.01"
                value={conditions.windSpeedMetersPerSecond}
                onChange={(e) => updateConditions('windSpeedMetersPerSecond', e.target.value)}
                aria-invalid={
                  errors['conditions.windSpeedMetersPerSecond'] ? 'true' : undefined
                }
              />
            </Field>

            <Field
              id="windDirectionDegrees"
              label="Wind direction (degrees it blows from)"
              error={errors['conditions.windDirectionDegrees']}
            >
              <input
                id="windDirectionDegrees"
                type="number"
                step="1"
                min="0"
                max="359"
                value={conditions.windDirectionDegrees}
                onChange={(e) => updateConditions('windDirectionDegrees', e.target.value)}
                aria-invalid={errors['conditions.windDirectionDegrees'] ? 'true' : undefined}
              />
            </Field>

            <Field id="skyCondition" label="Sky" error={errors['conditions.skyCondition']}>
              <select
                id="skyCondition"
                value={conditions.skyCondition}
                onChange={(e) => updateConditions('skyCondition', e.target.value)}
              >
                <option value="">Not recorded</option>
                {data.options.skyConditions.map((sky) => (
                  <option key={sky} value={sky}>
                    {humanise(sky)}
                  </option>
                ))}
              </select>
            </Field>

            <Field
              id="barometricPressureHpa"
              label="Pressure (hPa)"
              error={errors['conditions.barometricPressureHpa']}
            >
              <input
                id="barometricPressureHpa"
                type="number"
                step="0.1"
                value={conditions.barometricPressureHpa}
                onChange={(e) => updateConditions('barometricPressureHpa', e.target.value)}
                aria-invalid={errors['conditions.barometricPressureHpa'] ? 'true' : undefined}
              />
            </Field>

            <Field id="tideState" label="Tide" error={errors['conditions.tideState']}>
              <select
                id="tideState"
                value={conditions.tideState}
                onChange={(e) => updateConditions('tideState', e.target.value)}
              >
                <option value="">Not recorded</option>
                {data.options.tideStates.map((tide) => (
                  <option key={tide} value={tide}>
                    {humanise(tide)}
                  </option>
                ))}
              </select>
            </Field>

            <Field
              id="tideHeightMeters"
              label="Tide height (m)"
              error={errors['conditions.tideHeightMeters']}
            >
              <input
                id="tideHeightMeters"
                type="number"
                step="0.01"
                value={conditions.tideHeightMeters}
                onChange={(e) => updateConditions('tideHeightMeters', e.target.value)}
                aria-invalid={errors['conditions.tideHeightMeters'] ? 'true' : undefined}
              />
            </Field>

            <Field id="observedAt" label="Conditions observed at" error={errors['conditions.observedAt']}>
              <input
                id="observedAt"
                type="datetime-local"
                value={conditions.observedAt}
                onChange={(e) => updateConditions('observedAt', e.target.value)}
              />
            </Field>
          </fieldset>

          <fieldset>
            <legend>Notes</legend>
            <Field id="notes" label="Notes" error={errors.notes}>
              <textarea
                id="notes"
                rows="4"
                value={form.notes}
                onChange={(e) => update('notes', e.target.value)}
              />
            </Field>
          </fieldset>

          <button type="submit" disabled={saving}>
            {saving ? 'Saving...' : 'Save catch'}
          </button>
        </form>
      )}
    </main>
  );
}

function explainLocationFailure(error) {
  switch (error.code) {
    case error.PERMISSION_DENIED:
      return 'Location permission was denied. You can still type coordinates below.';
    case error.POSITION_UNAVAILABLE:
      return 'Your device could not work out where it is. Try again, or enter coordinates below.';
    case error.TIMEOUT:
      return 'Finding your location took too long. Try again, or enter coordinates below.';
    default:
      return 'Location is unavailable. Enter coordinates below.';
  }
}
