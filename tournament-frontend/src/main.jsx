import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import HomePageContent from './Pages/HomePage/HomePageContent';
import TournamentPage from './Pages/TournamentPage/TournamentMainPage';
import TournamentIndividualPage from './Pages/TournamentPage/TournamentIndividualPage';
import TournamentControl from "./Pages/TournamentControl/TournamentControl";
import EventPage from "./Pages/Event/EventPage.jsx";
import PlayerPage from "./Pages/Player/PlayerPage.jsx";
import NotFoundPage from './Pages/NotFoundPage';
import Layout from './Pages/Layout';
import RegisterPage from './Pages/LoginAndRegistration/RegisterPage';
import LoginSuccessPage from './Pages/LoginAndRegistration/LoginSuccessPage';
import LoginPage from './Pages/LoginAndRegistration/LoginPage';


const router = createBrowserRouter([
  {
    path: "/",
    element: <Layout />, // Layout wraps all child routes
    errorElement: <NotFoundPage />, 
    children: [
      { index: true, element: <HomePageContent /> },
      { path: "tournament", element: <TournamentPage /> },
      { path: "tournament/:tournamentId", element: <TournamentIndividualPage /> },
      { path: "tournamentControl", element: <TournamentControl/>},
      { path: "tournament/:tournamentId/event/:eventIndex/draw", element: <EventPage/>},
      { path: "player/:playerId", element: <PlayerPage/>},
      { path: "register", element: <RegisterPage/> },
      { path: "login-success", element: <LoginSuccessPage/> },
      { path: "login", element: <LoginPage/> }
      // Add more child routes here
    ],
  },
]);

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <RouterProvider router={router}/>
  </StrictMode>,
)