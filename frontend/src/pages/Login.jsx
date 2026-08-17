import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';

import { ApiError } from '../api.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { Field } from '../components/Field.jsx';

export default function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState(null);
  const [busy, setBusy] = useState(false);

  if (user) {
    return <Navigate to="/catches" replace />;
  }

  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    setMessage(null);

    try {
      await login(username, password);
      navigate(location.state?.from?.pathname ?? '/catches', { replace: true });
    } catch (e) {
      setMessage(
        e instanceof ApiError ? e.message : 'Something went wrong signing in.',
      );
      setBusy(false);
    }
  }

  return (
    <main className="auth-page">
      <h1>Sign in</h1>
      <p className="muted auth-intro">Your journal is private to your account.</p>

      {message && <p className="notice notice--error">{message}</p>}

      <form onSubmit={submit}>
        <fieldset>
          <Field id="username" label="Username">
            <input
              id="username"
              type="text"
              autoComplete="username"
              autoCapitalize="none"
              autoCorrect="off"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </Field>

          <Field id="password" label="Password">
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </Field>
        </fieldset>

        <div className="submit-bar">
          <button type="submit" disabled={busy}>
            {busy ? 'Signing in...' : 'Sign in'}
          </button>
        </div>
      </form>

      <p className="auth-switch">
        No account yet? <Link to="/register">Create one</Link>
      </p>
    </main>
  );
}
