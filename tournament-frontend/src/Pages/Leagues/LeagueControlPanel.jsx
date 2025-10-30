import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import authAxios from '../../utils/authAxios';
import styles from './LeagueControlPanel.module.css';

function LeagueControlPanel() {
  const { leagueId } = useParams();
  const navigate = useNavigate();

  const [league, setLeague] = useState(null);
  const [tournaments, setTournaments] = useState([]);
  const [allTournaments, setAllTournaments] = useState([]);
  const [editors, setEditors] = useState([]);
  const [activeSection, setActiveSection] = useState('tournaments');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ type: '', text: '' });

  // Modal states
  const [showAddTournamentModal, setShowAddTournamentModal] = useState(false);
  const [selectedTournamentId, setSelectedTournamentId] = useState('');
  const [showAddEditorModal, setShowAddEditorModal] = useState(false);
  const [editorSearchQuery, setEditorSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);

  useEffect(() => {
    fetchData();
    fetchCurrentUser();
  }, [leagueId]);

  const fetchCurrentUser = async () => {
    try {
      const response = await authAxios.get('/api/users/me');
      setCurrentUser(response.data);
    } catch (err) {
      console.error('Failed to fetch current user:', err);
    }
  };

  const fetchData = async () => {
    try {
      const [leagueRes, tournamentsRes, allTournamentsRes] = await Promise.all([
        authAxios.get(`/api/leagues/${leagueId}`),
        authAxios.get(`/api/leagues/${leagueId}/tournaments`),
        authAxios.get('/api/tournaments')
      ]);

      setLeague(leagueRes.data);
      setTournaments(tournamentsRes.data);
      setAllTournaments(allTournamentsRes.data);
      setEditors(leagueRes.data.authorizedEditors || []);
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

  const isOwner = () => {
    return currentUser && league && league.owner?.id === currentUser.id;
  };

  const handleAddTournament = async (e) => {
    e.preventDefault();
    if (!selectedTournamentId) {
      showMessage('error', 'Please select a tournament');
      return;
    }

    try {
      await authAxios.post(`/api/leagues/${leagueId}/tournaments/${selectedTournamentId}`);
      showMessage('success', 'Tournament added to league!');
      setShowAddTournamentModal(false);
      setSelectedTournamentId('');
      fetchData();
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to add tournament');
    }
  };

  const handleRemoveTournament = async (tournamentId) => {
    if (!window.confirm('Remove this tournament from the league? Rankings will be recalculated.')) return;

    try {
      await authAxios.delete(`/api/leagues/${leagueId}/tournaments/${tournamentId}`);
      showMessage('success', 'Tournament removed and rankings recalculated');
      fetchData();
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to remove tournament');
    }
  };

  const handleRecalculateRankings = async () => {
    try {
      await authAxios.post(`/api/leagues/${leagueId}/rankings/recalculate`);
      showMessage('success', 'Rankings recalculated successfully!');
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to recalculate rankings');
    }
  };

  const handleSearchEditors = async () => {
    if (!editorSearchQuery.trim()) {
      setSearchResults([]);
      return;
    }

    try {
      // Search all users - adjust this endpoint as needed
      const response = await authAxios.get('/api/users/search', {
        params: { query: editorSearchQuery }
      });
      setSearchResults(response.data);
    } catch (err) {
      showMessage('error', 'Failed to search users');
    }
  };

  const handleAddEditor = async (userId) => {
    try {
      await authAxios.post(`/api/leagues/${leagueId}/editors/${userId}`);
      showMessage('success', 'Editor added successfully!');
      setShowAddEditorModal(false);
      setEditorSearchQuery('');
      setSearchResults([]);
      fetchData();
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to add editor');
    }
  };

  const handleRemoveEditor = async (editorId) => {
    if (!window.confirm('Remove this editor?')) return;

    try {
      await authAxios.delete(`/api/leagues/${leagueId}/editors/${editorId}`);
      showMessage('success', 'Editor removed successfully!');
      fetchData();
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to remove editor');
    }
  };

  const availableTournaments = allTournaments.filter(
    t => !tournaments.some(lt => lt.id === t.id)
  );

  if (loading) {
    return <div className={styles.loading}>Loading control panel...</div>;
  }

  if (!league) {
    return <div className={styles.error}>League not found</div>;
  }

  return (
    <div className={styles.controlPanel}>
      <div className={styles.header}>
        <button onClick={() => navigate(`/league/${leagueId}`)} className={styles.backButton}>
          ← Back to League
        </button>
        <h1 className={styles.title}>League Control Panel</h1>
        <div className={styles.leagueName}>{league.name}</div>
      </div>

      {message.text && (
        <div className={`${styles.message} ${styles[message.type]}`}>
          {message.text}
        </div>
      )}

      <div className={styles.mainContent}>
        <div className={styles.sidebar}>
          <button
            className={`${styles.sidebarButton} ${activeSection === 'tournaments' ? styles.active : ''}`}
            onClick={() => setActiveSection('tournaments')}
          >
            Tournaments
          </button>
          {isOwner() && (
            <button
              className={`${styles.sidebarButton} ${activeSection === 'editors' ? styles.active : ''}`}
              onClick={() => setActiveSection('editors')}
            >
              Editors
            </button>
          )}
          <button
            className={`${styles.sidebarButton} ${activeSection === 'settings' ? styles.active : ''}`}
            onClick={() => setActiveSection('settings')}
          >
            Settings
          </button>
        </div>

        <div className={styles.content}>
          {activeSection === 'tournaments' && (
            <div className={styles.section}>
              <div className={styles.sectionHeader}>
                <h2 className={styles.sectionTitle}>Manage Tournaments</h2>
                <button onClick={() => setShowAddTournamentModal(true)} className={styles.primaryButton}>
                  + Add Tournament
                </button>
              </div>

              {tournaments.length === 0 ? (
                <div className={styles.emptyState}>
                  <p>No tournaments in this league yet. Add tournaments to get started!</p>
                </div>
              ) : (
                <div className={styles.tournamentsList}>
                  {tournaments.map(tournament => (
                    <div key={tournament.id} className={styles.tournamentItem}>
                      <div className={styles.tournamentInfo}>
                        <h3 className={styles.tournamentName}>{tournament.name}</h3>
                        <div className={styles.tournamentDetails}>
                          <span>{tournament.location || 'No location'}</span>
                          <span>{tournament.begin ? new Date(tournament.begin).toLocaleDateString() : 'No date'}</span>
                        </div>
                      </div>
                      <button
                        onClick={() => handleRemoveTournament(tournament.id)}
                        className={styles.dangerButton}
                      >
                        Remove
                      </button>
                    </div>
                  ))}
                </div>
              )}

              <div className={styles.actionSection}>
                <button onClick={handleRecalculateRankings} className={styles.secondaryButton}>
                  Recalculate Rankings
                </button>
              </div>
            </div>
          )}

          {activeSection === 'editors' && isOwner() && (
            <div className={styles.section}>
              <div className={styles.sectionHeader}>
                <h2 className={styles.sectionTitle}>Authorized Editors</h2>
                <button onClick={() => setShowAddEditorModal(true)} className={styles.primaryButton}>
                  + Add Editor
                </button>
              </div>

              {editors.length === 0 ? (
                <div className={styles.emptyState}>
                  <p>No editors assigned yet. Add editors to help manage this league!</p>
                </div>
              ) : (
                <div className={styles.editorsList}>
                  {editors.map(editor => (
                    <div key={editor.id} className={styles.editorItem}>
                      <div className={styles.editorInfo}>
                        <span className={styles.editorUsername}>{editor.username}</span>
                        {editor.name && <span className={styles.editorName}>({editor.name})</span>}
                      </div>
                      <button
                        onClick={() => handleRemoveEditor(editor.id)}
                        className={styles.dangerButton}
                      >
                        Remove
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {activeSection === 'settings' && (
            <div className={styles.section}>
              <h2 className={styles.sectionTitle}>League Settings</h2>
              <div className={styles.infoCard}>
                <p>Additional league settings coming soon...</p>
              </div>
            </div>
          )}
        </div>
      </div>

      {showAddTournamentModal && (
        <div className={styles.modal} onClick={() => setShowAddTournamentModal(false)}>
          <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <h2 className={styles.modalTitle}>Add Tournament to League</h2>
            <form onSubmit={handleAddTournament}>
              <div className={styles.formGroup}>
                <label htmlFor="tournament">Select Tournament</label>
                <select
                  id="tournament"
                  value={selectedTournamentId}
                  onChange={(e) => setSelectedTournamentId(e.target.value)}
                  className={styles.select}
                >
                  <option value="">-- Select Tournament --</option>
                  {availableTournaments.map(tournament => (
                    <option key={tournament.id} value={tournament.id}>
                      {tournament.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className={styles.modalActions}>
                <button type="button" onClick={() => setShowAddTournamentModal(false)} className={styles.cancelButton}>
                  Cancel
                </button>
                <button type="submit" className={styles.submitButton}>
                  Add Tournament
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showAddEditorModal && (
        <div className={styles.modal} onClick={() => setShowAddEditorModal(false)}>
          <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <h2 className={styles.modalTitle}>Add Editor</h2>
            <div className={styles.searchBox}>
              <input
                type="text"
                value={editorSearchQuery}
                onChange={(e) => setEditorSearchQuery(e.target.value)}
                placeholder="Search for users..."
                className={styles.input}
              />
              <button onClick={handleSearchEditors} className={styles.searchButton}>
                Search
              </button>
            </div>
            {searchResults.length > 0 && (
              <div className={styles.searchResults}>
                {searchResults.map(user => (
                  <div key={user.id} className={styles.searchResultItem}>
                    <div>
                      <div className={styles.resultUsername}>{user.username}</div>
                      {user.name && <div className={styles.resultName}>{user.name}</div>}
                    </div>
                    <button onClick={() => handleAddEditor(user.id)} className={styles.addButton}>
                      Add
                    </button>
                  </div>
                ))}
              </div>
            )}
            <div className={styles.modalActions}>
              <button onClick={() => setShowAddEditorModal(false)} className={styles.cancelButton}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default LeagueControlPanel;
