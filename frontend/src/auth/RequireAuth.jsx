import { Navigate, useLocation } from 'react-router-dom';

import { useAuth } from './AuthContext.jsx';
import { Skeleton } from '../components/Field.jsx';

/**
 * Gate for anything that needs an account.
 *
 * <p>This is convenience, not security. Every one of these routes is also
 * enforced server-side, because a route guard is just JavaScript the visitor
 * controls. It exists so people see a login form instead of a broken page.
 */
export function RequireAuth({ children }) {
  const { user, checking } = useAuth();
  const location = useLocation();

  if (checking) {
    return (
      <main>
        <Skeleton count={2} />
      </main>
    );
  }

  if (!user) {
    // Remember where they were headed so signing in resumes it rather than
    // dumping everyone on the journal.
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
}
