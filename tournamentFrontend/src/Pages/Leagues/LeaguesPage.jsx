import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authAxios from '../../utils/authAxios';
import publicAxios from '../../utils/publicAxios';
import PlayerLink from '../../Components/PlayerLink/PlayerLink';
import styles from './LeaguesPage.module.css';

function LeaguesPage() {
  const navigate = useNavigate();
  const [leagues, setLeagues] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newLeagueName, setNewLeagueName] = useState('');
  const [message, setMessage] = useState({ type: '', text: '' });

  useEffect(() => {
    fetchLeagues();
  }, []);

  const fetchLeagues = async () => {
    try {
      const response = await publicAxios.get('/api/leagues');
      setLeagues(response.data);
      setLoading(false);
    } catch (err) {
      showMessage('error', 'Failed to load leagues');
      setLoading(false);
    }
  };

  const showMessage = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 5000);
  };

  const handleCreateLeague = async (e) => {
    e.preventDefault();
    if (!newLeagueName.trim()) {
      showMessage('error', 'League name cannot be empty');
      return;
    }

    try {
      await authAxios.post('/api/leagues', { name: newLeagueName });
      showMessage('success', 'League created successfully!');
      setShowCreateModal(false);
      setNewLeagueName('');
      fetchLeagues();
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to create league');
    }
  };

  if (loading) {
    return <div className={styles.loading}>Loading leagues...</div>;
  }

  return (
    <div className={styles.leaguesPage}>
      <div className={styles.header}>
        <h1 className={styles.title}>Leagues</h1>
        <button onClick={() => setShowCreateModal(true)} className={styles.createButton}>
          + Create League
        </button>
      </div>

      {message.text && (
        <div className={`${styles.message} ${styles[message.type]}`}>
          {message.text}
        </div>
      )}

      {leagues.length === 0 ? (
        <div className={styles.emptyState}>
          <p>No leagues found. Create your first league to get started!</p>
        </div>
      ) : (
        <div className={styles.leaguesGrid}>
          {leagues.map(league => (
            <div key={league.id} className={styles.leagueCard}>
              <div className={styles.leagueHeader}>
                <h2 className={styles.leagueName}>{league.name}</h2>
              </div>
              <div className={styles.leagueInfo}>
                <div className={styles.infoItem}>
                  <span className={styles.infoLabel}>Owner:</span>
                  <span className={styles.infoValue}>
                    <PlayerLink player={league.owner} />
                  </span>
                </div>
                <div className={styles.infoItem}>
                  <span className={styles.infoLabel}>Tournaments:</span>
                  <span className={styles.infoValue}>{league.tournaments?.length || 0}</span>
                </div>
              </div>
              <div className={styles.cardActions}>
                <button
                  onClick={() => navigate(`/league/${league.id}`)}
                  className={styles.viewButton}
                >
                  View League
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {showCreateModal && (
        <div className={styles.modal} onClick={() => setShowCreateModal(false)}>
          <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <h2 className={styles.modalTitle}>Create New League</h2>
            <form onSubmit={handleCreateLeague}>
              <div className={styles.formGroup}>
                <label htmlFor="leagueName">League Name</label>
                <input
                  id="leagueName"
                  type="text"
                  value={newLeagueName}
                  onChange={(e) => setNewLeagueName(e.target.value)}
                  placeholder="Enter league name"
                  className={styles.input}
                  autoFocus
                />
              </div>
              <div className={styles.modalActions}>
                <button type="button" onClick={() => setShowCreateModal(false)} className={styles.cancelButton}>
                  Cancel
                </button>
                <button type="submit" className={styles.submitButton}>
                  Create League
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default LeaguesPage;
