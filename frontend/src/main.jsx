import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';

import App from './App.jsx';
import Journal from './pages/Journal.jsx';
import CatchDetail from './pages/CatchDetail.jsx';
import NewCatch from './pages/NewCatch.jsx';
import TackleBox from './pages/TackleBox.jsx';
import NewLure from './pages/NewLure.jsx';
import './styles.css';

const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      { index: true, element: <Journal /> },
      { path: 'catches', element: <Journal /> },
      // Static paths outrank the dynamic one, so /catches/new is not read
      // as a catch with the id "new".
      { path: 'catches/new', element: <NewCatch /> },
      { path: 'catches/:id', element: <CatchDetail /> },
      { path: 'lures', element: <TackleBox /> },
      { path: 'lures/new', element: <NewLure /> },
    ],
  },
]);

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
);
