import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import HomePage from './Pages/HomePage';
import TournamentPage from './Pages/TournamentPage/TournamentPage';
import NotFoundPage from './Pages/NotFoundPage';
import Layout from './Pages/Layout';

const router = createBrowserRouter([
  {
    path: "/",
    element: <Layout />, // Layout wraps all child routes
    errorElement: <NotFoundPage />,
    children: [
      { index: true, element: <HomePage /> },
      { path: "tournament", element: <TournamentPage /> },
      // Add more child routes here
    ],
  },
]);

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <RouterProvider router={router}/>
  </StrictMode>,
)