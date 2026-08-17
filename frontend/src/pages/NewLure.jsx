import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import { api, ApiError } from '../api.js';
import { useLoad } from '../hooks/useLoad.js';
import { Field, LoadState } from '../components/Field.jsx';
import { humanise } from '../format.js';

const EMPTY = {
  lureType: '',
  brand: '',
  model: '',
  color: '',
  size: '',
  weightGrams: '',
  presentation: '',
  notes: '',
};

export default function NewLure() {
  const navigate = useNavigate();
  const { data: options, error: optionsError, loading } = useLoad(() => api.lureOptions());

  const [form, setForm] = useState(EMPTY);
  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState(null);
  const [saving, setSaving] = useState(false);

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submit(event) {
    event.preventDefault();
    setSaving(true);
    setErrors({});
    setMessage(null);

    // Empty strings become null so optional fields stay genuinely absent
    // rather than arriving as "" and failing type conversion.
    const payload = Object.fromEntries(
      Object.entries(form).map(([key, value]) => [key, value === '' ? null : value]),
    );

    try {
      await api.createLure(payload);
      navigate('/lures');
    } catch (e) {
      if (e instanceof ApiError) {
        setErrors(e.fieldErrors);
        setMessage(e.message);
      } else {
        setMessage('Something went wrong saving the lure.');
      }
      setSaving(false);
    }
  }

  return (
    <main>
      <div className="actions">
        <Link className="button" to="/lures">
          Back to tackle box
        </Link>
      </div>

      <h1>Add a lure</h1>

      <LoadState loading={loading} error={optionsError} />
      {message && <p className="notice notice--error">{message}</p>}

      {options && (
        <form onSubmit={submit}>
          <fieldset>
            <legend>Lure</legend>

            <Field id="lureType" label="Type" error={errors.lureType}>
              <select
                id="lureType"
                value={form.lureType}
                onChange={(e) => update('lureType', e.target.value)}
                aria-invalid={errors.lureType ? 'true' : undefined}
              >
                <option value="">Choose a type</option>
                {options.types.map((type) => (
                  <option key={type} value={type}>
                    {humanise(type)}
                  </option>
                ))}
              </select>
            </Field>

            <Field id="brand" label="Brand" error={errors.brand}>
              <input
                id="brand"
                type="text"
                value={form.brand}
                onChange={(e) => update('brand', e.target.value)}
              />
            </Field>

            <Field id="model" label="Model" error={errors.model}>
              <input
                id="model"
                type="text"
                value={form.model}
                onChange={(e) => update('model', e.target.value)}
              />
            </Field>

            <Field id="color" label="Colour" error={errors.color}>
              <input
                id="color"
                type="text"
                value={form.color}
                onChange={(e) => update('color', e.target.value)}
              />
            </Field>

            <Field id="size" label="Size" error={errors.size}>
              <input
                id="size"
                type="text"
                placeholder="e.g. 3 inch, #4"
                value={form.size}
                onChange={(e) => update('size', e.target.value)}
              />
            </Field>

            <Field id="weightGrams" label="Weight (g)" error={errors.weightGrams}>
              <input
                id="weightGrams"
                type="number"
                step="0.01"
                value={form.weightGrams}
                onChange={(e) => update('weightGrams', e.target.value)}
                aria-invalid={errors.weightGrams ? 'true' : undefined}
              />
            </Field>

            <Field id="presentation" label="Presentation" error={errors.presentation}>
              <select
                id="presentation"
                value={form.presentation}
                onChange={(e) => update('presentation', e.target.value)}
              >
                <option value="">Not specified</option>
                {options.presentations.map((p) => (
                  <option key={p} value={p}>
                    {humanise(p)}
                  </option>
                ))}
              </select>
            </Field>

            <Field id="notes" label="Notes" error={errors.notes}>
              <textarea
                id="notes"
                rows="3"
                value={form.notes}
                onChange={(e) => update('notes', e.target.value)}
              />
            </Field>
          </fieldset>

          <button type="submit" disabled={saving}>
            {saving ? 'Saving...' : 'Save lure'}
          </button>
        </form>
      )}
    </main>
  );
}
