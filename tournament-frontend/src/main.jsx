import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider, BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import HomePage from './Pages/HomePage';
import TournamentPage from './Pages/TournamentPage';
import NotFoundPage from './Pages/NotFoundPage';
import SingleElimEventMatch from './Components/SingleElimEvent/SingleElimEventMatch';

const router = createBrowserRouter([
  {
    path: "/",
    element: <HomePage/>,
    errorElement: <NotFoundPage/>
  },
  {
    path:"/tournament",
    element: <TournamentPage/>
  },
  {
    path:"/testmatch",
    element: <SingleElimEventMatch playerTop= "player1" playerBottom='player2' winner = "player1" arrOfScore={[[21,16], [15, 21], [22,20]]}/>
  }
]);

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <RouterProvider router={router}/>
  </StrictMode>,
)
