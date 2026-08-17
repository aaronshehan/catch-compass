import { Link, NavLink, Outlet } from 'react-router-dom';

/**
 * The page shell: the React equivalent of layout.html. Every route renders
 * inside the <Outlet />.
 */
export default function App() {
  return (
    <>
      <header className="app-header">
        <Link className="brand" to="/catches">
          CatchCompass
        </Link>
        <nav>
          <NavLink to="/catches">Journal</NavLink>
          <NavLink to="/lures">Tackle</NavLink>
        </nav>
      </header>

      <Outlet />
    </>
  );
}
