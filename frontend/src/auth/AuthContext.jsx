import { createContext, useCallback, useContext, useEffect, useState } from 'react';

import { api } from '../api.js';

const AuthContext = createContext(null);

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error('useAuth must be used inside <AuthProvider>');
  return value;
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);

  // Distinct from "signed out": on first load we genuinely do not know yet, and
  // rendering the login screen during that gap would flash it at signed-in users.
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    let cancelled = false;

    // Asking the server is the only reliable answer. The session lives in an
    // httpOnly cookie the page cannot read, which is exactly why it is safe.
    api
      .me()
      .then((account) => {
        if (!cancelled) setUser(account);
      })
      .catch(() => {
        if (!cancelled) setUser(null);
      })
      .finally(() => {
        if (!cancelled) setChecking(false);
      });

    const onExpired = () => setUser(null);
    window.addEventListener('auth:expired', onExpired);

    return () => {
      cancelled = true;
      window.removeEventListener('auth:expired', onExpired);
    };
  }, []);

  const login = useCallback(async (username, password) => {
    const account = await api.login(username, password);
    setUser(account);
    return account;
  }, []);

  const register = useCallback(async (username, password) => {
    await api.register(username, password);
    // Registering does not sign you in, so do it explicitly rather than
    // leaving someone on a login form immediately after creating an account.
    const account = await api.login(username, password);
    setUser(account);
    return account;
  }, []);

  const logout = useCallback(async () => {
    try {
      await api.logout();
    } finally {
      // Clear locally even if the call failed: the user asked to be signed out,
      // and leaving them looking signed in would be worse than a stale session.
      setUser(null);
    }
  }, []);

  return (
    <AuthContext.Provider value={{ user, checking, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
