import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import authAxios from '../../utils/authAxios';
import publicAxios from '../../utils/publicAxios';
import { useAuth } from '../../contexts/AuthContext';
import PlayerLink from '../../Components/PlayerLink/PlayerLink';
import styles from './IndividualLeaguePage.module.css';

function IndividualLeaguePage() {
  const { leagueId } = useParams();
  const navigate = useNavigate();
  const { isLoggedIn } = useAuth();

  const [league, setLeague] = useState(null);
  const [tournaments, setTournaments] = useState([]);
  const [rankings, setRankings] = useState([]);
  const [activeTab, setActiveTab] = useState('tournaments');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [currentUser, setCurrentUser] = useState(null);
  const [recalculating, setRecalculating] = useState(false);

  useEffect(() => {
    fetchLeagueData();
    if (isLoggedIn) {
      fetchCurrentUser();
    }
  }, [leagueId, isLoggedIn]);

  const fetchCurrentUser = async () => {
    try {
      const response = await authAxios.get('/api/users/me');
      setCurrentUser(response.data);
    } catch (err) {
      console.error('Failed to fetch current user:', err);
    }
  };

  const fetchLeagueData = async () => {
    try {
      const [leagueRes, tournamentsRes, rankingsRes] = await Promise.all([
        publicAxios.get(`/api/leagues/${leagueId}`),
        publicAxios.get(`/api/leagues/${leagueId}/tournaments`),
        publicAxios.get(`/api/leagues/${leagueId}/rankings`)
      ]);

      setLeague(leagueRes.data);
      setTournaments(tournamentsRes.data);
      setRankings(rankingsRes.data);
      setLoading(false);
    } catch (err) {
      showMessage('error', 'Failed to load league data');
      setLoading(false);
    }
  };

  const showMessage = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 5000);
  };

  const handleRecalculateRankings = async () => {
    setRecalculating(true);
    try {
      const response = await authAxios.post(`/api/leagues/${leagueId}/rankings/recalculate`);
      setRankings(response.data);
      showMessage('success', 'Rankings recalculated successfully!');
    } catch (err) {
      showMessage('error', 'Failed to recalculate rankings');
      console.error('Failed to recalculate rankings:', err);
    } finally {
      setRecalculating(false);
    }
  };

  const canEdit = () => {
    if (!currentUser || !league) return false;
    return league.owner?.id === currentUser.id ||
           league.authorizedEditors?.some(editor => editor.id === currentUser.id);
  };

  if (loading) {
    return <div className={styles.loading}>Loading league...</div>;
  }

  if (!league) {
    return <div className={styles.error}>League not found</div>;
  }

  return (
    <div className={styles.leaguePage}>
      <div className={styles.header}>
        <button onClick={() => navigate('/leagues')} className={styles.backButton}>
          ← Back to Leagues
        </button>
        <div className={styles.headerContent}>
          <h1 className={styles.title}>{league.name}</h1>
          <div className={styles.leagueInfo}>
            <span className={styles.infoItem}>
              Owner: <PlayerLink player={league.owner} />
            </span>
            <span className={styles.infoItem}>{tournaments.length} Tournaments</span>
            <span className={styles.infoItem}>{rankings.length} Players</span>
          </div>
        </div>
        {canEdit() && (
          <button
            onClick={() => navigate(`/league/${leagueId}/control`)}
            className={styles.controlButton}
          >
            Manage League
          </button>
        )}
      </div>

      {message.text && (
        <div className={`${styles.message} ${styles[message.type]}`}>
          {message.text}
        </div>
      )}

      <div className={styles.tabs}>
        <button
          className={`${styles.tab} ${activeTab === 'tournaments' ? styles.activeTab : ''}`}
          onClick={() => setActiveTab('tournaments')}
        >
          Tournaments
        </button>
        <button
          className={`${styles.tab} ${activeTab === 'players' ? styles.activeTab : ''}`}
          onClick={() => setActiveTab('players')}
        >
          Players & Rankings
        </button>
      </div>

      <div className={styles.content}>
        {activeTab === 'tournaments' && (
          <div className={styles.tournamentsSection}>
            {tournaments.length === 0 ? (
              <div className={styles.emptyState}>
                <p>No tournaments in this league yet.</p>
                {canEdit() && <p>Go to Manage League to add tournaments!</p>}
              </div>
            ) : (
              <div className={styles.tournamentsGrid}>
                {tournaments.map(tournament => (
                  <div key={tournament.id} className={styles.tournamentCard}>
                    <h3 className={styles.tournamentName}>{tournament.name}</h3>
                    <div className={styles.tournamentDetails}>
                      <div className={styles.detailItem}>
                        <span className={styles.detailLabel}>Location:</span>
                        <span className={styles.detailValue}>{tournament.location || 'TBD'}</span>
                      </div>
                      <div className={styles.detailItem}>
                        <span className={styles.detailLabel}>Start Date:</span>
                        <span className={styles.detailValue}>
                          {tournament.begin ? new Date(tournament.begin).toLocaleDateString() : 'TBD'}
                        </span>
                      </div>
                      <div className={styles.detailItem}>
                        <span className={styles.detailLabel}>End Date:</span>
                        <span className={styles.detailValue}>
                          {tournament.end ? new Date(tournament.end).toLocaleDateString() : 'TBD'}
                        </span>
                      </div>
                    </div>
                    <button
                      onClick={() => navigate(`/tournament/${tournament.id}`)}
                      className={styles.viewTournamentButton}
                    >
                      View Tournament
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {activeTab === 'players' && (
          <div className={styles.playersSection}>
            <div className={styles.rankingsHeader}>
              <h2 className={styles.sectionTitle}>Player Rankings</h2>
              <button
                onClick={handleRecalculateRankings}
                disabled={recalculating}
                className={styles.recalculateButton}
              >
                {recalculating ? 'Recalculating...' : 'Recalculate Rankings'}
              </button>
            </div>
            {rankings.length === 0 ? (
              <div className={styles.emptyState}>
                <p>No player rankings yet. Rankings are calculated from tournament results.</p>
                <p>Click "Recalculate Rankings" to generate standings.</p>
              </div>
            ) : (
              <div className={styles.rankingsTable}>
                <div className={styles.tableHeader}>
                  <div className={styles.rankCol}>Rank</div>
                  <div className={styles.playerCol}>Player</div>
                  <div className={styles.pointsCol}>Points</div>
                  <div className={styles.matchesCol}>Matches</div>
                  <div className={styles.recordCol}>W-L</div>
                </div>
                {rankings.map(ranking => (
                  <div key={ranking.id} className={styles.tableRow}>
                    <div className={styles.rankCol}>
                      <span className={styles.rankBadge}>{ranking.rank}</span>
                    </div>
                    <div className={styles.playerCol}>
                      <div className={styles.playerInfo}>
                        <span className={styles.playerUsername}>
                          <PlayerLink player={ranking.player} />
                        </span>
                        <span className={styles.playerName}>@{ranking.player?.username}</span>
                      </div>
                    </div>
                    <div className={styles.pointsCol}>
                      <span className={styles.points}>{ranking.points}</span>
                    </div>
                    <div className={styles.matchesCol}>
                      {ranking.matchesPlayed}
                    </div>
                    <div className={styles.recordCol}>
                      <span className={styles.wins}>{ranking.matchesWon}</span>-
                      <span className={styles.losses}>{ranking.matchesLost}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default IndividualLeaguePage;
