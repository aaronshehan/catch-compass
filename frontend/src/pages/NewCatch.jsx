import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import { api, ApiError } from '../api.js';
import { useLoad } from '../hooks/useLoad.js';
import { Field, Group, Skeleton } from '../components/Field.jsx';
import { humanise } from '../format.js';

const EMPTY_CATCH = {
  speciesId: '',
  caughtAt: '',
  lureType: '',
  lureDescription: '',
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
    Promise.all([api.species(), api.lureTypes(), api.conditionsOptions()]).then(
      ([species, lureTypes, options]) => ({ species, lureTypes, options }),
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
          `Got it, accurate to about ${Math.round(accuracy)} m. Correct it below if it looks wrong.`,
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
    setLocationStatus('Cleared. This catch will be saved without coordinates.');
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
        'Filled in from Open-Meteo. Check it and correct anything that looks wrong.' +
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

  // A rejected field inside a collapsed section would be invisible, and the
  // form would look like it failed for no reason. Any group holding an error
  // opens itself.
  const hasError = (...keys) => keys.some((key) => errors[key]);
  const hasConditionsError = Object.keys(errors).some((key) => key.startsWith('conditions.'));

  const locationSet = form.latitude !== '' && form.longitude !== '';

  return (
    <main>
      <Link className="back-link" to="/catches">
        &#8592; Journal
      </Link>

      <h1>Log a catch</h1>

      {loadError && <p className="notice notice--error">{loadError}</p>}
      {message && <p className="notice notice--error">{message}</p>}
      {errors.locationPairComplete && (
        <p className="notice notice--error">{errors.locationPairComplete}</p>
      )}
      {loading && <Skeleton count={3} />}

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

            <Field id="photo" label="Photo" error={errors.photo}>
              <input
                id="photo"
                type="file"
                accept="image/jpeg,image/png"
                capture="environment"
                onChange={(e) => setPhoto(e.target.files[0] ?? null)}
              />
            </Field>

            <Field id="lureType" label="Lure" error={errors.lureType}>
              <select
                id="lureType"
                value={form.lureType}
                onChange={(e) => update('lureType', e.target.value)}
              >
                <option value="">No lure recorded</option>
                {data.lureTypes.map((type) => (
                  <option key={type} value={type}>
                    {humanise(type)}
                  </option>
                ))}
              </select>
            </Field>

            {form.lureType && (
              <Field
                id="lureDescription"
                label="Lure details"
                error={errors.lureDescription}
              >
                <input
                  id="lureDescription"
                  type="text"
                  placeholder="Rapala Shad Rap, firetiger, 3 inch"
                  value={form.lureDescription}
                  onChange={(e) => update('lureDescription', e.target.value)}
                  aria-invalid={errors.lureDescription ? 'true' : undefined}
                />
              </Field>
            )}
          </fieldset>

          <Group
            title="Measurements"
            hint="optional"
            open={hasError('weightKg', 'lengthCm', 'circumferenceCm')}
          >
            <div className="field-pair">
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
            </div>

            <Field id="circumferenceCm" label="Girth (cm)" error={errors.circumferenceCm}>
              <input
                id="circumferenceCm"
                type="number"
                step="0.01"
                value={form.circumferenceCm}
                onChange={(e) => update('circumferenceCm', e.target.value)}
                aria-invalid={errors.circumferenceCm ? 'true' : undefined}
              />
            </Field>
          </Group>

          <Group
            title="Location"
            hint={locationSet ? 'set' : 'optional'}
            open={locationSet || hasError('latitude', 'longitude', 'locationPairComplete')}
          >
            <div className="actions">
              <button type="button" onClick={useMyLocation} disabled={locating}>
                {locating ? 'Locating...' : 'Use my location'}
              </button>
              <button type="button" className="button--ghost" onClick={clearLocation}>
                Clear
              </button>
            </div>

            {locationStatus && (
              <p className="notice" role="status" aria-live="polite">
                {locationStatus}
              </p>
            )}

            <div className="field-pair">
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
            </div>

            <Field
              id="locationAccuracyMeters"
              label="Accuracy (m)"
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
          </Group>

          <Group
            title="Conditions"
            hint={conditions.conditionsSource === 'MANUAL' ? 'optional' : 'from weather'}
            open={hasConditionsError}
          >
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

            <div className="field-pair">
              <Field
                id="airTemperatureC"
                label="Air (°C)"
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
                label="Water (°C)"
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
            </div>

            <div className="field-pair">
              <Field
                id="windSpeedMetersPerSecond"
                label="Wind (m/s)"
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
                label="From (degrees)"
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
            </div>

            <div className="field-pair">
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
            </div>

            <div className="field-pair">
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
            </div>

            <Field
              id="observedAt"
              label="Observed at"
              error={errors['conditions.observedAt']}
            >
              <input
                id="observedAt"
                type="datetime-local"
                value={conditions.observedAt}
                onChange={(e) => updateConditions('observedAt', e.target.value)}
              />
            </Field>
          </Group>

          <Group title="Notes" hint="optional" open={hasError('notes')}>
            <Field id="notes" label="Anything worth remembering" error={errors.notes}>
              <textarea
                id="notes"
                rows="4"
                value={form.notes}
                onChange={(e) => update('notes', e.target.value)}
              />
            </Field>
          </Group>

          <div className="submit-bar">
            <button type="submit" disabled={saving}>
              {saving ? 'Saving...' : 'Save catch'}
            </button>
          </div>
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
