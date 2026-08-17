import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import { api, ApiError } from '../api.js';
import { useLoad } from '../hooks/useLoad.js';
import { Field, Group, Skeleton } from '../components/Field.jsx';
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

  const detailErrors = ['brand', 'model', 'color', 'size', 'weightGrams', 'notes'].some(
    (key) => errors[key],
  );

  return (
    <main>
      <Link className="back-link" to="/lures">
        &#8592; Tackle box
      </Link>

      <h1>Add a lure</h1>

      {optionsError && <p className="notice notice--error">{optionsError}</p>}
      {message && <p className="notice notice--error">{message}</p>}
      {loading && <Skeleton count={2} />}

      {options && (
        <form onSubmit={submit}>
          <fieldset>
            <legend>What is it</legend>

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

            <div className="field-pair">
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
            </div>
          </fieldset>

          <Group title="Details" hint="optional" open={detailErrors}>
            <Field id="color" label="Colour" error={errors.color}>
              <input
                id="color"
                type="text"
                value={form.color}
                onChange={(e) => update('color', e.target.value)}
              />
            </Field>

            <div className="field-pair">
              <Field id="size" label="Size" error={errors.size}>
                <input
                  id="size"
                  type="text"
                  placeholder="3 inch, #4"
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
            </div>

            <Field id="presentation" label="Usually worked as" error={errors.presentation}>
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
          </Group>

          <div className="submit-bar">
            <button type="submit" disabled={saving}>
              {saving ? 'Saving...' : 'Save lure'}
            </button>
          </div>
        </form>
      )}
    </main>
  );
}
