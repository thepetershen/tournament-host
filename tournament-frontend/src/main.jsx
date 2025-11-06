import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import HomePage from './Pages/HomePage/HomePage';
import TournamentPage from './Pages/TournamentPage/TournamentMainPage';
import TournamentIndividualPage from './Pages/TournamentPage/TournamentIndividualPage';
import SignUpPage from './Pages/TournamentPage/SignUpPage';
import TournamentControl from "./Pages/TournamentControl/TournamentControl";
import EventPage from "./Pages/Event/EventPage.jsx";
import PlayerPage from "./Pages/Player/PlayerPage.jsx";
import LeaguesPage from "./Pages/Leagues/LeaguesPage.jsx";
import IndividualLeaguePage from "./Pages/Leagues/IndividualLeaguePage.jsx";
import LeagueControlPanel from "./Pages/Leagues/LeagueControlPanel.jsx";
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
      { index: true, element: <HomePage /> },
      { path: "tournaments", element: <TournamentPage /> },
      { path: "tournament/:tournamentId", element: <TournamentIndividualPage /> },
      { path: "tournament/:tournamentId/signup", element: <SignUpPage /> },
      { path: "tournament/:tournamentId/control", element: <TournamentControl/>},
      { path: "tournament/:tournamentId/event/:eventIndex/draw", element: <EventPage/>},
      { path: "player/:playerId", element: <PlayerPage/>},
      { path: "leagues", element: <LeaguesPage/>},
      { path: "league/:leagueId", element: <IndividualLeaguePage/>},
      { path: "league/:leagueId/control", element: <LeagueControlPanel/>},
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