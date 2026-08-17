import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';

import App from './App.jsx';
import { AuthProvider } from './auth/AuthContext.jsx';
import { RequireAuth } from './auth/RequireAuth.jsx';

import Journal from './pages/Journal.jsx';
import CatchDetail from './pages/CatchDetail.jsx';
import NewCatch from './pages/NewCatch.jsx';
import TackleBox from './pages/TackleBox.jsx';
import NewLure from './pages/NewLure.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import './styles.css';

/** Everything behind an account goes through the same gate. */
const guarded = (element) => <RequireAuth>{element}</RequireAuth>;

const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      { path: 'login', element: <Login /> },
      { path: 'register', element: <Register /> },

      { index: true, element: guarded(<Journal />) },
      { path: 'catches', element: guarded(<Journal />) },
      // Static paths outrank the dynamic one, so /catches/new is not read
      // as a catch with the id "new".
      { path: 'catches/new', element: guarded(<NewCatch />) },
      { path: 'catches/:id', element: guarded(<CatchDetail />) },
      { path: 'lures', element: guarded(<TackleBox />) },
      { path: 'lures/new', element: guarded(<NewLure />) },
    ],
  },
]);

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  </StrictMode>,
);
