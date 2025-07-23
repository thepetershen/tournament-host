import React, { useEffect, useState } from "react";
import { useParams,Link } from "react-router-dom";
import axios from "axios";

function TournamentIndividualPage() {
  const { tournamentId } = useParams();
  const [tournament, setTournament] = useState(null);
  const [activeTab, setActiveTab] = useState("general");
  const [events, setEvents] = useState([]);
  const [matches, setMatches] = useState([]);
  const [players, setPlayers] = useState([]);

  useEffect(() => {
    // Fetch tournament general info
    axios.get(`http://localhost:8080/api/tournaments/${tournamentId}`)
      .then(res => setTournament(res.data))
      .catch(err => console.error(err));

    // Fetch events
    axios.get(`http://localhost:8080/api/tournaments/${tournamentId}/events`)
      .then(res => setEvents(res.data))
      .catch(err => console.error(err));

    // Fetch matches
    axios.get(`http://localhost:8080/api/tournaments/${tournamentId}/matches`)
      .then(res => setMatches(res.data))
      .catch(err => console.error(err));

    // Fetch players
    axios.get(`http://localhost:8080/api/tournaments/${tournamentId}/users`)
      .then(res => setPlayers(res.data))
      .catch(err => console.error(err));
  }, [tournamentId]);

  return (
    <div style={{ maxWidth: 800, margin: "40px auto", padding: "24px" }}>
      <h1>{tournament ? tournament.name : "Loading..."}</h1>
      <div style={{ marginBottom: "24px", display: "flex", gap: "16px" }}>
        <button onClick={() => setActiveTab("general")} disabled={activeTab === "general"}>General</button>
        <button onClick={() => setActiveTab("events")} disabled={activeTab === "events"}>Events</button>
        <button onClick={() => setActiveTab("matches")} disabled={activeTab === "matches"}>Matches</button>
        <button onClick={() => setActiveTab("players")} disabled={activeTab === "players"}>Players</button>
      </div>

      {activeTab === "general" && tournament && (
        <div>
          <p><strong>Location:</strong> {tournament.location || "N/A"}</p>
          <p><strong>Host:</strong> {tournament.host || "N/A"}</p>
          <p><strong>Date:</strong> {tournament.date || "N/A"}</p>
        </div>
      )}

      {activeTab === "events" && (
        <div>
          <h2>Events</h2>
          {events.length === 0 ? (
            <p>No events found.</p>
          ) : (
            <ul>
              {events.map(event => (
                <div>
                    <Link to={`/event/${event.id}`}>{event.name}</Link>
                    <br/>
                </div>
              ))}
            </ul>
          )}
        </div>
      )}

      {activeTab === "matches" && (
        <div>
          <h2>Matches</h2>
          {matches.length === 0 ? (
            <p>No matches found.</p>
          ) : (
            <ul>
              {matches.map(match => (
                <li key={match.id}>
                  {match.playerA?.username || "?"} vs {match.playerB?.username || "?"}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      {activeTab === "players" && (
        <div>
          <h2>Players</h2>
          {players.length === 0 ? (
            <p>No players found.</p>
          ) : (
            <ul>
              {players.map(player => (
                <li key={player.id}>{player.username}</li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}

export default TournamentIndividualPage;