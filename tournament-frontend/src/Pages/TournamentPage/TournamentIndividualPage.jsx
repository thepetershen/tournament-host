import { useEffect, useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import authAxios from "../../utils/authAxios";
import styles from "./TournamentIndividualPage.module.css";

function TournamentIndividualPage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const [tournament, setTournament] = useState(null);
  const [activeTab, setActiveTab] = useState("general");
  const [events, setEvents] = useState([]);
  const [matches, setMatches] = useState([]);
  const [players, setPlayers] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);
  const [isAuthorized, setIsAuthorized] = useState(false);

  useEffect(() => {
    // Fetch current user if authenticated
    const token = localStorage.getItem('token');
    if (token) {
      authAxios.get('/api/users/me')
        .then(res => setCurrentUser(res.data))
        .catch(err => console.error(err));
    }

    // Fetch tournament general info
    authAxios.get(`/api/tournaments/${tournamentId}`)
      .then(res => setTournament(res.data))
      .catch(err => console.error(err));

    // Fetch events
    authAxios.get(`/api/tournaments/${tournamentId}/events`)
      .then(res => setEvents(res.data))
      .catch(err => console.error(err));

    // Fetch matches
    authAxios.get(`/api/tournaments/${tournamentId}/matches`)
      .then(res => setMatches(res.data))
      .catch(err => console.error(err));

    // Fetch players
    authAxios.get(`/api/tournaments/${tournamentId}/users`)
      .then(res => setPlayers(res.data))
      .catch(err => console.error(err));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tournamentId]);

  // Check if current user is authorized (owner or editor)
  useEffect(() => {
    if (currentUser && tournament) {
      const isOwner = tournament.owner && tournament.owner.id === currentUser.id;
      const isEditor = tournament.authorizedEditors &&
        tournament.authorizedEditors.some(editor => editor.id === currentUser.id);
      setIsAuthorized(isOwner || isEditor);
    } else {
      setIsAuthorized(false);
    }
  }, [currentUser, tournament]);

  const handleSignUpClick = () => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }

    // Verify token is valid by making a quick API call
    authAxios.get('/api/users/me')
      .then(() => {
        navigate(`/tournament/${tournamentId}/signup`);
      })
      .catch(() => {
        // Token is invalid or expired
        localStorage.removeItem('token');
        navigate('/login');
      });
  };

  return (
    <div className={styles.pageContainer}>
      {/* Tournament Header Section with colored background */}
      <div className={styles.tournamentHeader}>
        <div>
          <h1 className={styles.tournamentTitle}>
            {tournament ? tournament.name : "Loading..."}
          </h1>
          {tournament && (
            <div className={styles.tournamentMeta}>
              {tournament.owner && (
                <div className={styles.metaItem}>
                  <span className={styles.metaLabel}>Owner:</span>
                  <span>{tournament.owner.username || tournament.owner.name}</span>
                </div>
              )}
              <div className={styles.metaItem}>
                <span className={styles.metaLabel}>Events:</span>
                <span>{events.length}</span>
              </div>
              <div className={styles.metaItem}>
                <span className={styles.metaLabel}>Players:</span>
                <span>{players.length}</span>
              </div>
            </div>
          )}
        </div>
        <div className={styles.headerButtons}>
          {isAuthorized && (
            <button
              onClick={() => navigate('/tournamentControl')}
              className={styles.controlButton}
            >
              Control Panel
            </button>
          )}
          <button
            onClick={handleSignUpClick}
            className={styles.signUpButton}
          >
            Sign Up
          </button>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className={styles.tabNavigation}>
        <button
          className={`${styles.tabButton} ${activeTab === "general" ? styles.active : ""}`}
          onClick={() => setActiveTab("general")}
        >
          General Information
        </button>
        <button
          className={`${styles.tabButton} ${activeTab === "events" ? styles.active : ""}`}
          onClick={() => setActiveTab("events")}
        >
          Events
        </button>
        <button
          className={`${styles.tabButton} ${activeTab === "players" ? styles.active : ""}`}
          onClick={() => setActiveTab("players")}
        >
          Players
        </button>
      </div>

      {/* Content Area */}
      <div className={styles.contentArea}>
        {activeTab === "general" && tournament && (
          <div className={styles.generalInfo}>
            <h2 className={styles.sectionTitle}>Tournament Information</h2>
            <div className={styles.infoGrid}>
              <div className={styles.infoItem}>
                <div className={styles.infoLabel}>Tournament Name</div>
                <div className={styles.infoValue}>{tournament.name}</div>
              </div>
              {tournament.owner && (
                <div className={styles.infoItem}>
                  <div className={styles.infoLabel}>Owner</div>
                  <div className={styles.infoValue}>
                    {tournament.owner.username || tournament.owner.name}
                  </div>
                </div>
              )}
              <div className={styles.infoItem}>
                <div className={styles.infoLabel}>Total Events</div>
                <div className={styles.infoValue}>{events.length}</div>
              </div>
              <div className={styles.infoItem}>
                <div className={styles.infoLabel}>Total Players</div>
                <div className={styles.infoValue}>{players.length}</div>
              </div>
              <div className={styles.infoItem}>
                <div className={styles.infoLabel}>Total Matches</div>
                <div className={styles.infoValue}>{matches.length}</div>
              </div>
            </div>
          </div>
        )}

        {activeTab === "events" && (
          <div>
            <h2 className={styles.sectionTitle}>Events</h2>
            {events.length === 0 ? (
              <div className={styles.emptyState}>No events found.</div>
            ) : (
              <ul className={styles.eventsList}>
                {events.map(event => (
                  <li key={event.id} className={styles.eventCard}>
                    <Link
                      to={`/tournament/${tournamentId}/event/${event.id}/draw`}
                      className={styles.eventLink}
                    >
                      {event.name}
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}

        {activeTab === "players" && (
          <div className={styles.playersList}>
            <h2 className={styles.sectionTitle}>Players</h2>
            {players.length === 0 ? (
              <div className={styles.emptyState}>No players found.</div>
            ) : (
              <ul className={styles.playersGrid}>
                {players.map(player => (
                  <li key={player.id} className={styles.playerItem}>
                    {player.username}
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default TournamentIndividualPage;
