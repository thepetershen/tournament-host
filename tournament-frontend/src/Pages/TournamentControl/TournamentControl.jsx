import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import authAxios from '../../utils/authAxios';
import styles from './TournamentControl.module.css';

function TournamentControl() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();

  // State
  const [tournament, setTournament] = useState(null);
  const [events, setEvents] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [activeSection, setActiveSection] = useState('overview');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ type: '', text: '' });

  // Section-specific state
  const [pendingRegistrations, setPendingRegistrations] = useState([]);
  const [selectedRegistrations, setSelectedRegistrations] = useState([]);
  const [eventPlayers, setEventPlayers] = useState([]);
  const [matches, setMatches] = useState([]);
  const [seeds, setSeeds] = useState({});
  const [editors, setEditors] = useState([]);
  const [manualSeeds, setManualSeeds] = useState({});
  const [pointsDistribution, setPointsDistribution] = useState({});

  // Modal state
  const [showCreateEventModal, setShowCreateEventModal] = useState(false);
  const [showRecordResultModal, setShowRecordResultModal] = useState(false);
  const [selectedMatch, setSelectedMatch] = useState(null);
  const [showAddEditorModal, setShowAddEditorModal] = useState(false);
  const [editorSearchQuery, setEditorSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [showAddPlayersModal, setShowAddPlayersModal] = useState(false);
  const [playerSearchQuery, setPlayerSearchQuery] = useState('');
  const [playerSearchResults, setPlayerSearchResults] = useState([]);
  const [allTournamentPlayers, setAllTournamentPlayers] = useState([]);
  const [selectedPlayersToAdd, setSelectedPlayersToAdd] = useState([]);

  // Fetch tournament data
  useEffect(() => {
    const fetchData = async () => {
      try {
        const tournamentRes = await authAxios.get(`/api/tournaments/${tournamentId}`);
        setTournament(tournamentRes.data);

        const eventsRes = await authAxios.get(`/api/tournaments/${tournamentId}/events`);
        const eventsData = eventsRes.data;
        setEvents(eventsData);

        const editorsRes = await authAxios.get(`/api/tournaments/${tournamentId}/editors`);
        setEditors(editorsRes.data);

        // Fetch all tournament players for adding to events
        const playersRes = await authAxios.get(`/api/tournaments/${tournamentId}/users`);
        setAllTournamentPlayers(playersRes.data);

        // Fetch pending registrations across all events for overview
        let allPendingRegistrations = [];
        for (const event of eventsData) {
          try {
            const regRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${event.id}/registrations/pending`);
            allPendingRegistrations = [...allPendingRegistrations, ...regRes.data];
          } catch (err) {
            console.error(`Error fetching registrations for event ${event.id}:`, err);
          }
        }
        // Only set if no event is selected (for overview display)
        if (!selectedEvent) {
          setPendingRegistrations(allPendingRegistrations);
        }

        setLoading(false);
      } catch (err) {
        showMessage('error', 'Failed to load tournament data');
        setLoading(false);
      }
    };

    if (tournamentId) {
      fetchData();
    }
  }, [tournamentId]);

  // Fetch event-specific data when event is selected
  useEffect(() => {
    const fetchEventData = async () => {
      if (!selectedEvent) {
        // Clear event-specific state when no event is selected
        setPendingRegistrations([]);
        setEventPlayers([]);
        setMatches([]);
        setSeeds({});
        setManualSeeds({});
        return;
      }

      // Clear stale data before fetching new event data
      setPendingRegistrations([]);
      setEventPlayers([]);
      setMatches([]);
      setSeeds({});
      setManualSeeds({});
      setPointsDistribution({});

      try {
        // Fetch pending registrations
        const regRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/registrations/pending`);
        setPendingRegistrations(regRes.data);

        // Fetch event players
        const playersRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/players`);
        setEventPlayers(playersRes.data);

        // Fetch matches
        const matchesRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/matches`);
        setMatches(matchesRes.data);

        // Fetch seeds
        const seedsRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/seeds`);
        setSeeds(seedsRes.data || {});

        // Fetch points distribution
        try {
          const pointsRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/points-distribution`);
          if (pointsRes.data) {
            setPointsDistribution(pointsRes.data.pointsMap || {});
          }
        } catch (err) {
          // Points distribution might not exist yet, which is fine
          setPointsDistribution({});
        }
      } catch (err) {
        console.error('Error fetching event data:', err);
      }
    };

    fetchEventData();
  }, [selectedEvent, tournamentId]);

  const showMessage = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 5000);
  };

  // Helper function to get expected placements based on event type and player count
  const getExpectedPlacements = () => {
    if (!selectedEvent || !eventPlayers.length) return [];

    const playerCount = eventPlayers.length;
    const eventType = selectedEvent.eventType;

    if (eventType === 'ROUND_ROBIN') {
      // Round Robin: every placement from 1 to playerCount
      return Array.from({ length: playerCount }, (_, i) => String(i + 1));
    } else if (eventType === 'SINGLE_ELIM') {
      // Single Elim: 1st, 2nd, 3rd (tied), 5th (tied), 9th (tied), etc.
      // Based on backend logic: finals loser = 2nd, semis losers = 3rd, quarters = 5th, etc.
      const placements = ['1', '2']; // Winner and finalist
      const totalRounds = Math.ceil(Math.log2(playerCount));

      // Add placements for rounds before finals (semifinals and earlier)
      for (let roundIndex = totalRounds - 2; roundIndex >= 0; roundIndex--) {
        if (roundIndex === totalRounds - 2) {
          // Semifinals losers get 3rd place
          placements.push('3');
        } else {
          // Earlier rounds: quarterfinalists = 5th, round of 16 = 9th, etc.
          // Formula: 2^(totalRounds - roundIndex - 1) + 1
          const placement = String(Math.pow(2, totalRounds - roundIndex - 1) + 1);
          placements.push(placement);
        }
      }

      return placements;
    } else if (eventType === 'DOUBLE_ELIM') {
      // Double Elim: Similar to single elim but more granular placements
      const placements = ['1', '2', '3', '4'];
      let remaining = playerCount - 4;
      let nextPlacement = 5;

      while (remaining > 0) {
        placements.push(String(nextPlacement));
        const eliminated = Math.max(1, Math.ceil(remaining / 2));
        remaining -= eliminated;
        nextPlacement += eliminated;
      }

      return placements;
    }

    return [];
  };

  const handlePointsDistributionChange = (placement, value) => {
    setPointsDistribution(prev => ({
      ...prev,
      [placement]: value === '' ? undefined : parseInt(value)
    }));
  };

  const handleSavePointsDistribution = async () => {
    try {
      // Filter out undefined values
      const filteredPoints = Object.fromEntries(
        Object.entries(pointsDistribution).filter(([_, v]) => v !== undefined)
      );

      await authAxios.post(
        `/api/tournaments/${tournamentId}/event/${selectedEvent.id}/points-distribution`,
        { pointsMap: filteredPoints }
      );
      showMessage('success', 'Points distribution saved successfully!');
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to save points distribution');
    }
  };

  const refreshEvents = async () => {
    const eventsRes = await authAxios.get(`/api/tournaments/${tournamentId}/events`);
    setEvents(eventsRes.data);
  };

  // Event creation
  const [newEvent, setNewEvent] = useState({ name: '', eventType: 'SINGLE_ELIM' });

  const handleCreateEvent = async (e) => {
    e.preventDefault();
    try {
      await authAxios.post(`/api/tournaments/${tournamentId}/event`, newEvent);
      showMessage('success', 'Event created successfully!');
      setShowCreateEventModal(false);
      setNewEvent({ name: '', eventType: 'SINGLE_ELIM' });
      await refreshEvents();
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to create event');
    }
  };

  // Registration approval
  const handleApproveRegistrations = async () => {
    if (selectedRegistrations.length === 0) {
      showMessage('error', 'Please select registrations to approve');
      return;
    }

    try {
      await authAxios.post(
        `/api/tournaments/${tournamentId}/event/${selectedEvent.id}/registrations/approve`,
        { registrationIds: selectedRegistrations }
      );
      showMessage('success', `Approved ${selectedRegistrations.length} registration(s)`);
      setSelectedRegistrations([]);

      // Refresh data
      const regRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/registrations/pending`);
      setPendingRegistrations(regRes.data);

      const playersRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/players`);
      setEventPlayers(playersRes.data);
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to approve registrations');
    }
  };

  const handleRejectRegistration = async (registrationId) => {
    try {
      await authAxios.post(
        `/api/tournaments/${tournamentId}/event/${selectedEvent.id}/registrations/${registrationId}/reject`
      );
      showMessage('success', 'Registration rejected');

      const regRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/registrations/pending`);
      setPendingRegistrations(regRes.data);
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to reject registration');
    }
  };

  // Event initialization
  const handleInitializeEvent = async () => {
    if (!window.confirm('Initialize this event? This will generate the bracket/matches.')) return;

    try {
      await authAxios.post(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/initialize`);
      showMessage('success', 'Event initialized successfully!');

      const matchesRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/matches`);
      setMatches(matchesRes.data);
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to initialize event');
    }
  };

  const handleDeinitializeEvent = async () => {
    if (!window.confirm('Deinitialize this event? This will remove all matches but keep players.')) return;

    try {
      await authAxios.post(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/deinitialize`);
      showMessage('success', 'Event deinitialized successfully!');

      const matchesRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/matches`);
      setMatches(matchesRes.data);
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to deinitialize event');
    }
  };

  // Match result recording
  const [matchResult, setMatchResult] = useState({ winnerId: '', score: '' });

  const handleRecordResult = async (e) => {
    e.preventDefault();
    try {
      // Parse score string to array of integers
      let scoreArray = null;
      if (matchResult.score && matchResult.score.trim()) {
        // Parse formats like "6-4" or "6-4, 7-5" into [6, 4, 7, 5]
        const scoreStr = matchResult.score.replace(/\s/g, ''); // Remove spaces
        const numbers = scoreStr.split(/[-,]/).map(num => parseInt(num.trim())).filter(num => !isNaN(num));
        if (numbers.length > 0) {
          scoreArray = numbers;
        }
      }

      await authAxios.post(
        `/api/tournaments/${tournamentId}/matches/${selectedMatch.id}/result`,
        {
          winnerId: matchResult.winnerId,
          score: scoreArray
        }
      );
      showMessage('success', 'Match result recorded!');
      setShowRecordResultModal(false);
      setMatchResult({ winnerId: '', score: '' });

      const matchesRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/matches`);
      setMatches(matchesRes.data);
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to record result');
    }
  };

  // Editor management
  const handleSearchEditors = async () => {
    if (!editorSearchQuery.trim()) {
      setSearchResults([]);
      return;
    }

    try {
      // Search all users - you may need to implement a search endpoint
      // For now, we'll use the tournament users endpoint as a proxy
      const response = await authAxios.get(`/api/tournaments/${tournamentId}/users`);
      const filtered = response.data.filter(user =>
        user.username.toLowerCase().includes(editorSearchQuery.toLowerCase()) ||
        user.name.toLowerCase().includes(editorSearchQuery.toLowerCase())
      );
      setSearchResults(filtered);
    } catch (err) {
      showMessage('error', 'Failed to search users');
    }
  };

  const handleAddEditor = async (userId) => {
    try {
      await authAxios.post(`/api/tournaments/${tournamentId}/editors`, { id: userId });
      showMessage('success', 'Editor added successfully!');
      setShowAddEditorModal(false);
      setEditorSearchQuery('');
      setSearchResults([]);

      // Refresh editors list
      const editorsRes = await authAxios.get(`/api/tournaments/${tournamentId}/editors`);
      setEditors(editorsRes.data);
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to add editor');
    }
  };

  const handleRemoveEditor = async (editorId) => {
    if (!window.confirm('Remove this editor? They will lose access to manage this tournament.')) return;

    try {
      await authAxios.delete(`/api/tournaments/${tournamentId}/editors/${editorId}`);
      showMessage('success', 'Editor removed successfully!');

      // Refresh editors list
      const editorsRes = await authAxios.get(`/api/tournaments/${tournamentId}/editors`);
      setEditors(editorsRes.data);
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to remove editor');
    }
  };

  // Player management
  const handleSearchPlayers = () => {
    if (!playerSearchQuery.trim()) {
      setPlayerSearchResults(allTournamentPlayers);
      return;
    }

    const filtered = allTournamentPlayers.filter(player =>
      player.username.toLowerCase().includes(playerSearchQuery.toLowerCase()) ||
      player.name.toLowerCase().includes(playerSearchQuery.toLowerCase())
    );
    setPlayerSearchResults(filtered);
  };

  const handleTogglePlayerSelection = (playerId) => {
    if (selectedPlayersToAdd.includes(playerId)) {
      setSelectedPlayersToAdd(selectedPlayersToAdd.filter(id => id !== playerId));
    } else {
      setSelectedPlayersToAdd([...selectedPlayersToAdd, playerId]);
    }
  };

  const handleAddPlayersToEvent = async () => {
    if (selectedPlayersToAdd.length === 0) {
      showMessage('error', 'Please select at least one player to add');
      return;
    }

    try {
      const playerRequests = selectedPlayersToAdd.map(id => ({ id }));
      await authAxios.post(
        `/api/tournaments/${tournamentId}/event/${selectedEvent.id}/players`,
        playerRequests
      );
      showMessage('success', `Added ${selectedPlayersToAdd.length} player(s) to event!`);
      setShowAddPlayersModal(false);
      setPlayerSearchQuery('');
      setPlayerSearchResults([]);
      setSelectedPlayersToAdd([]);

      // Refresh event players
      const playersRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/players`);
      setEventPlayers(playersRes.data);
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to add players');
    }
  };

  // Seeding management
  const handleSeedChange = (userId, seedValue) => {
    setManualSeeds(prev => {
      const updated = { ...prev };
      if (seedValue === '' || seedValue === null) {
        delete updated[userId];
      } else {
        updated[userId] = parseInt(seedValue);
      }
      return updated;
    });
  };

  const handleSaveSeeds = async () => {
    try {
      // Validate seeds
      const seedValues = Object.values(manualSeeds);
      const uniqueSeeds = new Set(seedValues);

      if (seedValues.length !== uniqueSeeds.size) {
        showMessage('error', 'Duplicate seed numbers are not allowed');
        return;
      }

      // Check for sequential seeds starting from 1
      const sortedSeeds = [...seedValues].sort((a, b) => a - b);
      for (let i = 0; i < sortedSeeds.length; i++) {
        if (sortedSeeds[i] !== i + 1) {
          showMessage('error', 'Seeds must be sequential starting from 1 (e.g., 1, 2, 3, 4)');
          return;
        }
      }

      await authAxios.post(
        `/api/tournaments/${tournamentId}/event/${selectedEvent.id}/seeds/manual`,
        { playerSeeds: manualSeeds }
      );

      showMessage('success', 'Seeds saved successfully!');

      // Refresh seeds from server
      const seedsRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/seeds`);
      setSeeds(seedsRes.data || {});
      setManualSeeds({});
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to save seeds');
    }
  };

  const handleClearSeeds = async () => {
    if (!window.confirm('Clear all seeds? This cannot be undone.')) return;

    try {
      await authAxios.delete(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/seeds`);
      showMessage('success', 'Seeds cleared successfully!');

      // Refresh seeds from server
      const seedsRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/seeds`);
      setSeeds(seedsRes.data || {});
      setManualSeeds({});
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to clear seeds');
    }
  };

  if (loading) {
    return <div className={styles.loading}>Loading tournament control panel...</div>;
  }

  if (!tournament) {
    return <div className={styles.error}>Tournament not found</div>;
  }

  return (
    <div className={styles.controlPanel}>
      {/* Header */}
      <div className={styles.header}>
        <div className={styles.headerContent}>
          <button onClick={() => navigate(`/tournament/${tournamentId}`)} className={styles.backButton}>
            ← Back to Tournament
          </button>
          <h1 className={styles.title}>Tournament Control Panel</h1>
          <div className={styles.tournamentName}>{tournament.name}</div>
        </div>
      </div>

      {/* Message Banner */}
      {message.text && (
        <div className={`${styles.message} ${styles[message.type]}`}>
          {message.text}
        </div>
      )}

      {/* Main Content */}
      <div className={styles.mainContent}>
        {/* Sidebar Navigation */}
        <div className={styles.sidebar}>
          <div className={styles.sidebarSection}>
            <h3 className={styles.sidebarTitle}>Navigation</h3>
            <button
              className={`${styles.sidebarButton} ${activeSection === 'overview' ? styles.active : ''}`}
              onClick={() => setActiveSection('overview')}
            >
              Overview
            </button>
            <button
              className={`${styles.sidebarButton} ${activeSection === 'events' ? styles.active : ''}`}
              onClick={() => setActiveSection('events')}
            >
              Events
            </button>
            <button
              className={`${styles.sidebarButton} ${activeSection === 'registrations' ? styles.active : ''}`}
              onClick={() => setActiveSection('registrations')}
              disabled={!selectedEvent}
            >
              Registrations {pendingRegistrations.length > 0 && (
                <span className={styles.badge}>{pendingRegistrations.length}</span>
              )}
            </button>
            <button
              className={`${styles.sidebarButton} ${activeSection === 'players' ? styles.active : ''}`}
              onClick={() => setActiveSection('players')}
              disabled={!selectedEvent}
            >
              Players
            </button>
            <button
              className={`${styles.sidebarButton} ${activeSection === 'seeding' ? styles.active : ''}`}
              onClick={() => setActiveSection('seeding')}
              disabled={!selectedEvent}
            >
              Seeding
            </button>
            <button
              className={`${styles.sidebarButton} ${activeSection === 'matches' ? styles.active : ''}`}
              onClick={() => setActiveSection('matches')}
              disabled={!selectedEvent}
            >
              Matches
            </button>
            <button
              className={`${styles.sidebarButton} ${activeSection === 'points' ? styles.active : ''}`}
              onClick={() => setActiveSection('points')}
              disabled={!selectedEvent}
            >
              Points Distribution
            </button>
            <button
              className={`${styles.sidebarButton} ${activeSection === 'editors' ? styles.active : ''}`}
              onClick={() => setActiveSection('editors')}
            >
              Editors
            </button>
          </div>

          {/* Event Selector */}
          {events.length > 0 && (
            <div className={styles.sidebarSection}>
              <h3 className={styles.sidebarTitle}>Select Event</h3>
              <select
                className={styles.eventSelector}
                value={selectedEvent?.id || ''}
                onChange={(e) => {
                  const event = events.find(ev => ev.id === parseInt(e.target.value));
                  setSelectedEvent(event);
                }}
              >
                <option value="">Choose an event...</option>
                {events.map(event => (
                  <option key={event.id} value={event.id}>
                    {event.name}
                  </option>
                ))}
              </select>
            </div>
          )}
        </div>

        {/* Content Area */}
        <div className={styles.content}>
          {/* Overview Section */}
          {activeSection === 'overview' && (
            <div className={styles.section}>
              <h2 className={styles.sectionTitle}>Tournament Overview</h2>
              <div className={styles.statsGrid}>
                <div className={styles.statCard}>
                  <div className={styles.statNumber}>{events.length}</div>
                  <div className={styles.statLabel}>Total Events</div>
                </div>
                <div className={styles.statCard}>
                  <div className={styles.statNumber}>{editors.length}</div>
                  <div className={styles.statLabel}>Authorized Editors</div>
                </div>
                <div className={styles.statCard}>
                  <div className={styles.statNumber}>{pendingRegistrations.length}</div>
                  <div className={styles.statLabel}>Pending Registrations</div>
                </div>
              </div>

              <div className={styles.infoCard}>
                <h3>Tournament Details</h3>
                <div className={styles.detailsGrid}>
                  <div className={styles.detailItem}>
                    <span className={styles.detailLabel}>Owner:</span>
                    <span className={styles.detailValue}>{tournament.owner?.username || 'N/A'}</span>
                  </div>
                  {tournament.location && (
                    <div className={styles.detailItem}>
                      <span className={styles.detailLabel}>Location:</span>
                      <span className={styles.detailValue}>{tournament.location}</span>
                    </div>
                  )}
                  {tournament.begin && (
                    <div className={styles.detailItem}>
                      <span className={styles.detailLabel}>Start Date:</span>
                      <span className={styles.detailValue}>{new Date(tournament.begin).toLocaleDateString()}</span>
                    </div>
                  )}
                  {tournament.end && (
                    <div className={styles.detailItem}>
                      <span className={styles.detailLabel}>End Date:</span>
                      <span className={styles.detailValue}>{new Date(tournament.end).toLocaleDateString()}</span>
                    </div>
                  )}
                </div>
                {tournament.message && (
                  <div className={styles.messageBox}>
                    <strong>Message:</strong>
                    <p>{tournament.message}</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Events Section */}
          {activeSection === 'events' && (
            <div className={styles.section}>
              <div className={styles.sectionHeader}>
                <h2 className={styles.sectionTitle}>Manage Events</h2>
                <button
                  className={styles.primaryButton}
                  onClick={() => setShowCreateEventModal(true)}
                >
                  + Create Event
                </button>
              </div>

              {events.length === 0 ? (
                <div className={styles.emptyState}>
                  <p>No events yet. Create your first event to get started!</p>
                </div>
              ) : (
                <div className={styles.eventsList}>
                  {events.map(event => (
                    <div key={event.id} className={styles.eventCard}>
                      <div className={styles.eventHeader}>
                        <h3>{event.name}</h3>
                        <span className={styles.eventType}>{event.eventType || 'N/A'}</span>
                      </div>
                      <div className={styles.eventActions}>
                        <button
                          className={styles.secondaryButton}
                          onClick={() => setSelectedEvent(event)}
                        >
                          Select
                        </button>
                        {selectedEvent?.id === event.id && (
                          <>
                            <button
                              className={styles.successButton}
                              onClick={handleInitializeEvent}
                            >
                              Initialize
                            </button>
                            <button
                              className={styles.dangerButton}
                              onClick={handleDeinitializeEvent}
                            >
                              Deinitialize
                            </button>
                          </>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Registrations Section */}
          {activeSection === 'registrations' && (
            <div className={styles.section}>
              {!selectedEvent ? (
                <div className={styles.emptyState}>
                  <p>Please select an event to view registrations</p>
                </div>
              ) : (
                <>
                  <div className={styles.sectionHeader}>
                    <h2 className={styles.sectionTitle}>Pending Registrations - {selectedEvent.name}</h2>
                    {selectedRegistrations.length > 0 && (
                      <button
                        className={styles.successButton}
                        onClick={handleApproveRegistrations}
                      >
                        Approve Selected ({selectedRegistrations.length})
                      </button>
                    )}
                  </div>

                  {pendingRegistrations.length === 0 ? (
                    <div className={styles.emptyState}>
                      <p>No pending registrations for this event</p>
                    </div>
                  ) : (
                <div className={styles.table}>
                  <table>
                    <thead>
                      <tr>
                        <th>
                          <input
                            type="checkbox"
                            onChange={(e) => {
                              if (e.target.checked) {
                                setSelectedRegistrations(pendingRegistrations.map(r => r.id));
                              } else {
                                setSelectedRegistrations([]);
                              }
                            }}
                            checked={selectedRegistrations.length === pendingRegistrations.length}
                          />
                        </th>
                        <th>Username</th>
                        <th>Name</th>
                        <th>Registered At</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {pendingRegistrations.map(reg => (
                        <tr key={reg.id}>
                          <td>
                            <input
                              type="checkbox"
                              checked={selectedRegistrations.includes(reg.id)}
                              onChange={(e) => {
                                if (e.target.checked) {
                                  setSelectedRegistrations([...selectedRegistrations, reg.id]);
                                } else {
                                  setSelectedRegistrations(selectedRegistrations.filter(id => id !== reg.id));
                                }
                              }}
                            />
                          </td>
                          <td>{reg.user?.username}</td>
                          <td>{reg.user?.name}</td>
                          <td>{new Date(reg.registeredAt).toLocaleString()}</td>
                          <td>
                            <button
                              className={styles.dangerButtonSmall}
                              onClick={() => handleRejectRegistration(reg.id)}
                            >
                              Reject
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                  )}
                </>
              )}
            </div>
          )}

          {/* Players Section */}
          {activeSection === 'players' && selectedEvent && (
            <div className={styles.section}>
              <div className={styles.sectionHeader}>
                <h2 className={styles.sectionTitle}>Players - {selectedEvent.name}</h2>
                <button
                  className={styles.primaryButton}
                  onClick={() => {
                    setPlayerSearchResults(allTournamentPlayers);
                    setShowAddPlayersModal(true);
                  }}
                >
                  + Add Players
                </button>
              </div>

              {eventPlayers.length === 0 ? (
                <div className={styles.emptyState}>
                  <p>No players in this event yet. Add players to get started!</p>
                </div>
              ) : (
                <div className={styles.playersGrid}>
                  {eventPlayers.map(player => (
                    <div key={player.id} className={styles.playerCard}>
                      <div className={styles.playerName}>{player.username}</div>
                      <div className={styles.playerDetail}>{player.name}</div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Seeding Section */}
          {activeSection === 'seeding' && selectedEvent && (
            <div className={styles.section}>
              <h2 className={styles.sectionTitle}>Manual Seeding - {selectedEvent.name}</h2>

              {selectedEvent.initialized ? (
                <div className={styles.infoCard}>
                  <p style={{ color: '#f39c12', marginBottom: '10px' }}>
                    WARNING: Event is initialized. Seeds cannot be changed after initialization.
                  </p>
                  <h3>Current Seeds:</h3>
                  {Object.keys(seeds).length > 0 ? (
                    <div className={styles.seedList}>
                      {eventPlayers
                        .filter(player => seeds[player.id])
                        .sort((a, b) => seeds[a.id] - seeds[b.id])
                        .map(player => (
                          <div key={player.id} className={styles.seedItem}>
                            <span className={styles.seedNumber}>Seed {seeds[player.id]}</span>
                            <span className={styles.playerName}>{player.username}</span>
                          </div>
                        ))}
                    </div>
                  ) : (
                    <p>No seeds set for this event.</p>
                  )}
                </div>
              ) : (
                <>
                  <div className={styles.infoCard} style={{ marginBottom: '20px' }}>
                    <p>
                      Assign seed numbers to players. Seeds must be sequential starting from 1 (e.g., 1, 2, 3, 4).
                      Leave blank for unseeded players.
                    </p>
                    <p style={{ marginTop: '10px' }}>
                      <strong>Current saved seeds: {Object.keys(seeds).length}</strong>
                    </p>
                  </div>

                  <div className={styles.seedingControls}>
                    <button onClick={handleSaveSeeds} className={styles.primaryButton}>
                      Save Seeds
                    </button>
                    {Object.keys(seeds).length > 0 && (
                      <button onClick={handleClearSeeds} className={styles.dangerButton}>
                        Clear All Seeds
                      </button>
                    )}
                  </div>

                  <div className={styles.playerSeedList}>
                    {eventPlayers.length === 0 ? (
                      <p>No players registered for this event yet.</p>
                    ) : (
                      eventPlayers.map(player => (
                          <div key={player.id} className={styles.playerSeedRow}>
                            <div className={styles.playerInfo}>
                              <span className={styles.playerUsername}>{player.username}</span>
                              {player.name && (
                                <span className={styles.playerRealName}>({player.name})</span>
                              )}
                            </div>
                            <div className={styles.seedInputContainer}>
                              <label htmlFor={`seed-${player.id}`}>Seed:</label>
                              <input
                                id={`seed-${player.id}`}
                                type="number"
                                min="1"
                                max={eventPlayers.length}
                                value={manualSeeds[player.id] || ''}
                                onChange={(e) => handleSeedChange(player.id, e.target.value)}
                                placeholder={seeds[player.id] ? `Current: ${seeds[player.id]}` : 'None'}
                                className={styles.seedInput}
                              />
                            </div>
                          </div>
                        ))
                    )}
                  </div>
                </>
              )}
            </div>
          )}

          {/* Matches Section */}
          {activeSection === 'matches' && selectedEvent && (
            <div className={styles.section}>
              <h2 className={styles.sectionTitle}>Matches - {selectedEvent.name}</h2>

              {matches.length === 0 ? (
                <div className={styles.emptyState}>
                  <p>No matches yet. Initialize the event to generate matches.</p>
                </div>
              ) : (
                <div className={styles.matchesList}>
                  {matches.map(match => (
                    <div key={match.id} className={`${styles.matchCard} ${match.completed ? styles.completed : ''}`}>
                      <div className={styles.matchPlayers}>
                        <div className={styles.player}>{match.playerA?.username || 'TBD'}</div>
                        <div className={styles.vs}>vs</div>
                        <div className={styles.player}>{match.playerB?.username || 'TBD'}</div>
                      </div>
                      {match.completed ? (
                        <div className={styles.matchResult}>
                          <div className={styles.winner}>Winner: {match.winner?.username}</div>
                          <div className={styles.score}>Score: {match.score}</div>
                        </div>
                      ) : (
                        <button
                          className={styles.primaryButton}
                          onClick={() => {
                            setSelectedMatch(match);
                            setShowRecordResultModal(true);
                          }}
                          disabled={!match.playerA || !match.playerB}
                        >
                          Record Result
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Points Distribution Section */}
          {activeSection === 'points' && selectedEvent && (
            <div className={styles.section}>
              <div className={styles.sectionHeader}>
                <h2 className={styles.sectionTitle}>Points Distribution for {selectedEvent.name}</h2>
                <button
                  className={styles.primaryButton}
                  onClick={handleSavePointsDistribution}
                >
                  Save Points Distribution
                </button>
              </div>

              <div className={styles.infoBox}>
                <h3>Event Type: {selectedEvent.eventType}</h3>
                <p>
                  {selectedEvent.eventType === 'SINGLE_ELIM' &&
                    'Single Elimination: Set points for 1st, 2nd, 3rd (semifinalists), 5th (quarterfinalists), etc.'}
                  {selectedEvent.eventType === 'DOUBLE_ELIM' &&
                    'Double Elimination: Set points for each unique finish position (1st, 2nd, 3rd, 4th, 5th, etc.)'}
                  {selectedEvent.eventType === 'ROUND_ROBIN' &&
                    'Round Robin: Set points for each placement from 1st to last place.'}
                </p>
                <p><strong>Note:</strong> Points are only awarded when the event is complete.</p>
              </div>

              {eventPlayers.length === 0 ? (
                <div className={styles.emptyState}>
                  <p>No players in this event yet. Add players to configure points distribution.</p>
                </div>
              ) : (
                <div className={styles.pointsGrid}>
                  {getExpectedPlacements().map(placement => (
                    <div key={placement} className={styles.pointsInputGroup}>
                      <label className={styles.placementLabel}>
                        {placement === '1' ? '1st Place' :
                         placement === '2' ? '2nd Place' :
                         placement === '3' ? '3rd Place' :
                         `${placement}th Place`}
                        {selectedEvent.eventType !== 'ROUND_ROBIN' && parseInt(placement) > 2 && (
                          <span className={styles.tiedNote}>(tied)</span>
                        )}
                      </label>
                      <input
                        type="number"
                        className={styles.pointsInput}
                        value={pointsDistribution[placement] || ''}
                        onChange={(e) => handlePointsDistributionChange(placement, e.target.value)}
                        placeholder="Enter points"
                        min="0"
                      />
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Editors Section */}
          {activeSection === 'editors' && (
            <div className={styles.section}>
              <div className={styles.sectionHeader}>
                <h2 className={styles.sectionTitle}>Authorized Editors</h2>
                <button
                  className={styles.primaryButton}
                  onClick={() => setShowAddEditorModal(true)}
                >
                  + Add Editor
                </button>
              </div>

              {editors.length === 0 ? (
                <div className={styles.emptyState}>
                  <p>No additional editors</p>
                </div>
              ) : (
                <div className={styles.table}>
                  <table>
                    <thead>
                      <tr>
                        <th>Username</th>
                        <th>Name</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {editors.map(editor => (
                        <tr key={editor.id}>
                          <td>{editor.username}</td>
                          <td>{editor.name}</td>
                          <td>
                            <button
                              className={styles.dangerButtonSmall}
                              onClick={() => handleRemoveEditor(editor.id)}
                            >
                              Remove
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Create Event Modal */}
      {showCreateEventModal && (
        <div className={styles.modalOverlay} onClick={() => setShowCreateEventModal(false)}>
          <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
            <h2>Create New Event</h2>
            <form onSubmit={handleCreateEvent}>
              <div className={styles.formGroup}>
                <label>Event Name</label>
                <input
                  type="text"
                  value={newEvent.name}
                  onChange={(e) => setNewEvent({ ...newEvent, name: e.target.value })}
                  required
                />
              </div>
              <div className={styles.formGroup}>
                <label>Event Type</label>
                <select
                  value={newEvent.eventType}
                  onChange={(e) => setNewEvent({ ...newEvent, eventType: e.target.value })}
                >
                  <option value="SINGLE_ELIM">Single Elimination</option>
                  <option value="DOUBLE_ELIM">Double Elimination</option>
                  <option value="ROUND_ROBIN">Round Robin</option>
                </select>
              </div>
              <div className={styles.modalActions}>
                <button type="button" className={styles.secondaryButton} onClick={() => setShowCreateEventModal(false)}>
                  Cancel
                </button>
                <button type="submit" className={styles.primaryButton}>
                  Create Event
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Record Result Modal */}
      {showRecordResultModal && selectedMatch && (
        <div className={styles.modalOverlay} onClick={() => setShowRecordResultModal(false)}>
          <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
            <h2>Record Match Result</h2>
            <div className={styles.matchInfo}>
              <p><strong>{selectedMatch.playerA?.username}</strong> vs <strong>{selectedMatch.playerB?.username}</strong></p>
            </div>
            <form onSubmit={handleRecordResult}>
              <div className={styles.formGroup}>
                <label>Winner</label>
                <select
                  value={matchResult.winnerId}
                  onChange={(e) => setMatchResult({ ...matchResult, winnerId: e.target.value })}
                  required
                >
                  <option value="">Select winner...</option>
                  <option value={selectedMatch.playerA?.id}>{selectedMatch.playerA?.username}</option>
                  <option value={selectedMatch.playerB?.id}>{selectedMatch.playerB?.username}</option>
                </select>
              </div>
              <div className={styles.formGroup}>
                <label>Score (optional)</label>
                <input
                  type="text"
                  value={matchResult.score}
                  onChange={(e) => setMatchResult({ ...matchResult, score: e.target.value })}
                  placeholder="Format: 6-4 or 6-4, 7-5 or 21-19"
                />
                <small style={{ color: '#718096', fontSize: '0.85rem', marginTop: '4px', display: 'block' }}>
                  Enter scores separated by hyphens or commas (e.g., "6-4, 6-3" for tennis sets)
                </small>
              </div>
              <div className={styles.modalActions}>
                <button type="button" className={styles.secondaryButton} onClick={() => setShowRecordResultModal(false)}>
                  Cancel
                </button>
                <button type="submit" className={styles.primaryButton}>
                  Record Result
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Add Editor Modal */}
      {showAddEditorModal && (
        <div className={styles.modalOverlay} onClick={() => setShowAddEditorModal(false)}>
          <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
            <h2>Add Editor</h2>
            <div className={styles.formGroup}>
              <label>Search Users</label>
              <div style={{ display: 'flex', gap: '8px' }}>
                <input
                  type="text"
                  value={editorSearchQuery}
                  onChange={(e) => setEditorSearchQuery(e.target.value)}
                  placeholder="Enter username or name..."
                  style={{ flex: 1 }}
                />
                <button
                  type="button"
                  className={styles.secondaryButton}
                  onClick={handleSearchEditors}
                >
                  Search
                </button>
              </div>
            </div>

            {searchResults.length > 0 && (
              <div className={styles.searchResults}>
                <h3 style={{ fontSize: '1rem', marginBottom: '12px', color: '#4a5568' }}>Search Results:</h3>
                <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                  {searchResults.map(user => (
                    <div key={user.id} className={styles.searchResultItem}>
                      <div>
                        <div style={{ fontWeight: 600, color: '#2d3748' }}>{user.username}</div>
                        <div style={{ fontSize: '0.9rem', color: '#718096' }}>{user.name}</div>
                      </div>
                      <button
                        className={styles.successButton}
                        onClick={() => handleAddEditor(user.id)}
                        disabled={editors.some(e => e.id === user.id)}
                      >
                        {editors.some(e => e.id === user.id) ? 'Already Editor' : 'Add'}
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div className={styles.modalActions}>
              <button type="button" className={styles.secondaryButton} onClick={() => {
                setShowAddEditorModal(false);
                setEditorSearchQuery('');
                setSearchResults([]);
              }}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Add Players Modal */}
      {showAddPlayersModal && (
        <div className={styles.modalOverlay} onClick={() => setShowAddPlayersModal(false)}>
          <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
            <h2>Add Players to Event</h2>
            <p style={{ color: '#718096', marginBottom: '20px' }}>
              Select tournament players to add to <strong>{selectedEvent?.name}</strong>
            </p>

            <div className={styles.formGroup}>
              <label>Search Players</label>
              <div style={{ display: 'flex', gap: '8px' }}>
                <input
                  type="text"
                  value={playerSearchQuery}
                  onChange={(e) => setPlayerSearchQuery(e.target.value)}
                  placeholder="Search by username or name..."
                  style={{ flex: 1 }}
                />
                <button
                  type="button"
                  className={styles.secondaryButton}
                  onClick={handleSearchPlayers}
                >
                  Search
                </button>
              </div>
            </div>

            {playerSearchResults.length > 0 && (
              <div className={styles.searchResults}>
                <h3 style={{ fontSize: '1rem', marginBottom: '12px', color: '#4a5568' }}>
                  Available Players ({playerSearchResults.length}):
                </h3>
                <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                  {playerSearchResults.map(player => {
                    const isInEvent = eventPlayers.some(p => p.id === player.id);
                    const isSelected = selectedPlayersToAdd.includes(player.id);

                    return (
                      <div
                        key={player.id}
                        className={styles.searchResultItem}
                        style={{
                          opacity: isInEvent ? 0.5 : 1,
                          borderColor: isSelected ? '#667eea' : '#e9ecef',
                          backgroundColor: isSelected ? '#f0f4ff' : '#f8f9fa'
                        }}
                      >
                        <div>
                          <div style={{ fontWeight: 600, color: '#2d3748' }}>{player.username}</div>
                          <div style={{ fontSize: '0.9rem', color: '#718096' }}>{player.name}</div>
                        </div>
                        {isInEvent ? (
                          <span style={{ color: '#718096', fontSize: '0.9rem', fontWeight: 600 }}>
                            Already in event
                          </span>
                        ) : (
                          <button
                            className={isSelected ? styles.secondaryButton : styles.successButton}
                            onClick={() => handleTogglePlayerSelection(player.id)}
                          >
                            {isSelected ? 'Deselect' : 'Select'}
                          </button>
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            <div className={styles.modalActions}>
              <button type="button" className={styles.secondaryButton} onClick={() => {
                setShowAddPlayersModal(false);
                setPlayerSearchQuery('');
                setPlayerSearchResults([]);
                setSelectedPlayersToAdd([]);
              }}>
                Cancel
              </button>
              <button
                type="button"
                className={styles.primaryButton}
                onClick={handleAddPlayersToEvent}
                disabled={selectedPlayersToAdd.length === 0}
              >
                Add {selectedPlayersToAdd.length > 0 ? `(${selectedPlayersToAdd.length})` : ''} Players
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default TournamentControl;
