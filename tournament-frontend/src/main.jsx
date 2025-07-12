import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import HomePage from './Pages/HomePage';
import TournamentPage from './Pages/TournamentPage/TournamentMainPage';
import TournamentIndividualPage from './Pages/TournamentPage/TournamentIndividualPage';
import TournamentControl from "./Pages/TournamentControl/TournamentControl";
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
      { path: "tournament/:tournamentId", element: <TournamentIndividualPage /> },
      { path: "tournamentControl", element: <TournamentControl/>}
      // Add more child routes here
    ],
  },
]);

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <RouterProvider router={router}/>
  </StrictMode>,
)