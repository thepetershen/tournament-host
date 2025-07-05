import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider, BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import HomePage from './Pages/HomePage';
import TournamentPage from './Pages/TournamentPage';
import NotFoundPage from './Pages/NotFoundPage';
import SingleElimBracket from './Components/SingleElimEvent/SingleElimBracket';

function createBracket(numPlayers) {
    // Generate player names
    const players = [];
    for (let i = 1; i <= numPlayers; i++) {
        players.push(`Player ${i}`);
    }

    // Pad to next power of 2 with "BYE"
    const nextPow2 = 2 ** Math.ceil(Math.log2(numPlayers));
    while (players.length < nextPow2) {
        players.push("BYE");
    }

    const rounds = [];
    let currentRoundPlayers = [...players];

    // Helper to generate a random badminton score
    function randomScore() {
        // Best of 3 games, each game to 21
        const game = () => [21, Math.floor(Math.random() * 20) + 10];
        return [game(), game(), game()];
    }

    // Build rounds
    while (currentRoundPlayers.length > 1) {
        const round = [];
        for (let i = 0; i < currentRoundPlayers.length; i += 2) {
            round.push({
                player1: currentRoundPlayers[i],
                player2: currentRoundPlayers[i + 1],
                winner: currentRoundPlayers[i],
                score: randomScore()
            });
        }
        rounds.push(round);
        // Winners advance (for demo, just pick player1 unless BYE)
        currentRoundPlayers = round.map(match =>
            match.player1 === "BYE" ? match.player2 :
            match.player2 === "BYE" ? match.player1 :
            match.player1 // always player1 for demo
        );
    }

    return rounds;
}



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
    path:"/testtournament",
    element: <SingleElimBracket draw = {createBracket(16)}/>
  }
]);

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <RouterProvider router={router}/>
  </StrictMode>,
)
