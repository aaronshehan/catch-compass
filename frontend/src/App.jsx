import { Link, NavLink, Outlet } from 'react-router-dom';

export default function App() {
  return (
    <>
      <header className="app-header">
        <Link className="brand" to="/catches">
          CatchCompass
        </Link>
        <nav>
          <NavLink to="/catches" className={({ isActive }) => (isActive ? 'active' : '')}>
            Journal
          </NavLink>
          <NavLink to="/lures" className={({ isActive }) => (isActive ? 'active' : '')}>
            Tackle
          </NavLink>
        </nav>
      </header>

      <Outlet />
    </>
  );
}
