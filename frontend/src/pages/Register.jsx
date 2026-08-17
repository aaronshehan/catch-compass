import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';

import { ApiError } from '../api.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { Field } from '../components/Field.jsx';

export default function Register() {
  const { user, register } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState(null);
  const [busy, setBusy] = useState(false);

  if (user) {
    return <Navigate to="/catches" replace />;
  }

  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    setErrors({});
    setMessage(null);

    try {
      await register(username, password);
      navigate('/catches', { replace: true });
    } catch (e) {
      if (e instanceof ApiError) {
        setErrors(e.fieldErrors);
        setMessage(e.message);
      } else {
        setMessage('Something went wrong creating your account.');
      }
      setBusy(false);
    }
  }

  return (
    <main className="auth-page">
      <h1>Create an account</h1>
      <p className="muted auth-intro">
        Catches, photos and locations stay private to you.
      </p>

      {message && <p className="notice notice--error">{message}</p>}

      <form onSubmit={submit}>
        <fieldset>
          <Field id="username" label="Username" error={errors.username}>
            <input
              id="username"
              type="text"
              autoComplete="username"
              autoCapitalize="none"
              autoCorrect="off"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              aria-invalid={errors.username ? 'true' : undefined}
              required
            />
          </Field>

          <Field id="password" label="Password" error={errors.password}>
            <input
              id="password"
              type="password"
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              aria-invalid={errors.password ? 'true' : undefined}
              required
            />
            <span className="field-hint">
              At least 12 characters. Length matters more than symbols, so a
              short phrase you will remember beats something cryptic.
            </span>
          </Field>
        </fieldset>

        <div className="submit-bar">
          <button type="submit" disabled={busy}>
            {busy ? 'Creating...' : 'Create account'}
          </button>
        </div>
      </form>

      <p className="auth-switch">
        Already have an account? <Link to="/login">Sign in</Link>
      </p>
    </main>
  );
}
