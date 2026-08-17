import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';

import { useAuth } from './auth/AuthContext.jsx';

export default function App() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  async function signOut() {
    await logout();
    navigate('/login', { replace: true });
  }

  return (
    <>
      <header className="app-header">
        <Link className="brand" to={user ? '/catches' : '/login'}>
          CatchCompass
        </Link>

        {user && (
          <nav>
            <NavLink to="/catches" className={({ isActive }) => (isActive ? 'active' : '')}>
              Journal
            </NavLink>
            <button type="button" className="button--ghost signout" onClick={signOut}>
              Sign out
            </button>
          </nav>
        )}
      </header>

      <Outlet />
    </>
  );
}
