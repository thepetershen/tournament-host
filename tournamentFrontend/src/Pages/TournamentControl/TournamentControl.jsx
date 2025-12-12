import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import authAxios from '../../utils/authAxios';
import PlayerLink from '../../Components/PlayerLink/PlayerLink';
import BracketPreview from '../../Components/BracketPreview';
import { supportsPreview } from '../../utils/bracketGenerator';
import styles from './TournamentControl.module.css';

function TournamentControl() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  // State
  const [tournament, setTournament] = useState(null);
  const [events, setEvents] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [activeSection, setActiveSection] = useState('overview');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ type: '', text: '' });

  // Section-specific state
  const [pendingRegistrations, setPendingRegistrations] = useState([]);
  const [allRegistrations, setAllRegistrations] = useState([]); // All registrations including approved
  const [selectedRegistrations, setSelectedRegistrations] = useState([]);
  const [eventPlayers, setEventPlayers] = useState([]);
  const [teams, setTeams] = useState([]);
  const [matches, setMatches] = useState([]);
  const [seeds, setSeeds] = useState({});
  const [teamSeeds, setTeamSeeds] = useState({});
  const [editors, setEditors] = useState([]);
  const [manualSeeds, setManualSeeds] = useState({});
  const [manualTeamSeeds, setManualTeamSeeds] = useState({});
  const [pointsDistribution, setPointsDistribution] = useState({});
  const [matchConfig, setMatchConfig] = useState({
    matchType: 'SINGLES',
    gamesPerMatch: 1
  });
  const [previousEventId, setPreviousEventId] = useState(null); // Track previous event to detect switches

  // Refs to track editing state (avoid stale closures)
  const isEditingSeedsRef = useRef(false);
  const isEditingTeamSeedsRef = useRef(false);

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
  const [selectedPartners, setSelectedPartners] = useState({}); // For partner matching

  // Fetch tournament data
  useEffect(() => {
    const fetchData = async () => {
      try {
        // Fetch initial data in parallel for faster load
        const results = await Promise.allSettled([
          authAxios.get(`/api/tournaments/${tournamentId}`),
          authAxios.get(`/api/tournaments/${tournamentId}/events`),
          authAxios.get(`/api/tournaments/${tournamentId}/editors`),
          authAxios.get(`/api/tournaments/${tournamentId}/users`)
        ]);

        // Handle each result individually
        if (results[0].status === 'fulfilled') {
          setTournament(results[0].value.data);
        } else {
          console.error('Failed to fetch tournament:', results[0].reason);
          throw new Error('Tournament not found');
        }

        const eventsData = results[1].status === 'fulfilled' ? results[1].value.data : [];
        setEvents(eventsData);

        const editorsData = results[2].status === 'fulfilled' ? results[2].value.data : [];
        setEditors(editorsData);

        const playersData = results[3].status === 'fulfilled' ? results[3].value.data : [];
        setAllTournamentPlayers(playersData);

        // Fetch pending registrations across all events for overview (in parallel)
        const registrationPromises = eventsData.map(event =>
          authAxios.get(`/api/tournaments/${tournamentId}/event/${event.id}/registrations/pending`)
            .then(res => res.data)
            .catch(err => {
              console.error(`Error fetching registrations for event ${event.id}:`, err);
              return [];
            })
        );
        const registrationResults = await Promise.all(registrationPromises);
        const allPendingRegistrations = registrationResults.flat();
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
        setTeams([]);
        setMatches([]);
        setSeeds({});
        setTeamSeeds({});
        setManualSeeds({});
        setManualTeamSeeds({});
        setPreviousEventId(null);
        return;
      }

      // Check if we're switching to a different event
      const isEventSwitch = previousEventId !== null && previousEventId !== selectedEvent.id;

      // Clear stale data before fetching new event data
      setPendingRegistrations([]);
      setEventPlayers([]);
      setTeams([]);
      setMatches([]);
      setPointsDistribution({});

      // Only clear seeds when switching to a DIFFERENT event
      // This preserves manual input when the same event re-renders
      if (isEventSwitch) {
        setSeeds({});
        setTeamSeeds({});
        setManualSeeds({});
        setManualTeamSeeds({});
      }

      // Update previous event ID
      setPreviousEventId(selectedEvent.id);

      // Set match config from selectedEvent
      const isDoubles = selectedEvent.matchType === 'DOUBLES';
      setMatchConfig({
        matchType: selectedEvent.matchType || 'SINGLES',
        gamesPerMatch: selectedEvent.gamesPerMatch || 1
      });

      try {
        // Fetch all event-specific data in parallel for faster load
        const requests = [
          authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/registrations/pending`),
          authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/registrations/all`),
          authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/players`),
          authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/matches`),
          authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/seeds`),
          authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/points-distribution`)
        ];

        // Add team-specific requests for doubles events
        if (isDoubles) {
          requests.push(
            authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/teams`),
            authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/seeds/teams`)
          );
        }

        const results = await Promise.allSettled(requests);

        // Set data from successful requests
        if (results[0].status === 'fulfilled') setPendingRegistrations(results[0].value.data);
        if (results[1].status === 'fulfilled') setAllRegistrations(results[1].value.data);
        if (results[2].status === 'fulfilled') setEventPlayers(results[2].value.data);
        if (results[3].status === 'fulfilled') setMatches(results[3].value.data);

        // Only update seeds if there are no manual edits in progress
        if (results[4].status === 'fulfilled') {
          setSeeds(prevSeeds => {
            const serverSeeds = results[4].value.data || {};

            // Use ref instead of state - refs are ALWAYS current (no stale closure)
            if (isEditingSeedsRef.current) {
              return prevSeeds; // Preserve during editing
            }

            return serverSeeds;
          });
        }

        if (results[5].status === 'fulfilled' && results[5].value.data) {
          setPointsDistribution(results[5].value.data.pointsMap || {});
        }

        // Set team-specific data for doubles events
        if (isDoubles) {
          if (results[6] && results[6].status === 'fulfilled') setTeams(results[6].value.data || []);

          // Only update team seeds if there are no manual edits in progress
          if (results[7] && results[7].status === 'fulfilled') {
            setTeamSeeds(prevTeamSeeds => {
              const serverTeamSeeds = results[7].value.data || {};

              // Use ref instead of state - refs are ALWAYS current (no stale closure)
              if (isEditingTeamSeedsRef.current) {
                return prevTeamSeeds; // Preserve during editing
              }

              return serverTeamSeeds;
            });
          }
        }
      } catch (err) {
        console.error('Error fetching event data:', err);
      }
    };

    fetchEventData();
  }, [selectedEvent?.id, tournamentId]); // Use selectedEvent.id instead of whole object to prevent unnecessary re-fetches

  const showMessage = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 5000);
  };

  // Helper function to get expected placements based on event type and participant count
  const getExpectedPlacements = () => {
    if (!selectedEvent) return [];

    // Determine participant count based on match type
    const isDoubles = selectedEvent.matchType === 'DOUBLES';
    let participantCount;

    if (isDoubles) {
      if (!teams.length) return [];
      participantCount = teams.length; // Use team count for doubles
    } else {
      if (!eventPlayers.length) return [];
      participantCount = eventPlayers.length; // Use player count for singles
    }

    const eventType = selectedEvent.eventType;

    if (eventType === 'ROUND_ROBIN') {
      // Round Robin: every placement from 1 to participantCount
      return Array.from({ length: participantCount }, (_, i) => String(i + 1));
    } else if (eventType === 'SINGLE_ELIM') {
      // Single Elim: 1st, 2nd, 3rd (tied), 5th (tied), 9th (tied), etc.
      // Based on backend logic: finals loser = 2nd, semis losers = 3rd, quarters = 5th, etc.
      const placements = ['1', '2']; // Winner and finalist
      const totalRounds = Math.ceil(Math.log2(participantCount));

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
      let remaining = participantCount - 4;
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

  const handleSaveMatchConfig = async () => {
    try {
      await authAxios.post(
        `/api/tournaments/${tournamentId}/event/${selectedEvent.id}/match-config`,
        matchConfig
      );
      showMessage('success', 'Match configuration saved successfully!');

      // Refresh events to get updated config
      await refreshEvents();

      // Update selectedEvent with new config
      const updatedEvents = await authAxios.get(`/api/tournaments/${tournamentId}/events`);
      const updatedEvent = updatedEvents.data.find(e => e.id === selectedEvent.id);
      if (updatedEvent) {
        setSelectedEvent(updatedEvent);
      }
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to save match configuration');
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

      // Clear matches array since event is now uninitialized
      setMatches([]);

      // Refresh event list to update initialized status
      const eventsRes = await authAxios.get(`/api/tournaments/${tournamentId}/events`);
      setEvents(eventsRes.data);

      // Update selected event
      const updatedEvent = eventsRes.data.find(e => e.id === selectedEvent.id);
      if (updatedEvent) {
        setSelectedEvent(updatedEvent);
      }
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to deinitialize event');
    }
  };

  // Match result recording
  const [matchResult, setMatchResult] = useState({ winnerId: '', score: '' });

  const handleRecordResult = async (e) => {
    e.preventDefault();

    // Validate both participants exist
    if (!hasParticipants(selectedMatch)) {
      showMessage('error', 'Cannot record result for a match with a bye');
      return;
    }

    // Validate winner is selected and is a valid participant
    if (!matchResult.winnerId) {
      showMessage('error', 'Please select a winner');
      return;
    }

    const winnerIdNum = parseInt(matchResult.winnerId);
    const playerAId = getParticipantId(selectedMatch, 'A');
    const playerBId = getParticipantId(selectedMatch, 'B');

    if (winnerIdNum !== playerAId && winnerIdNum !== playerBId) {
      showMessage('error', 'Invalid winner selection');
      return;
    }

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
      // Search all users globally using the unified search endpoint
      const response = await authAxios.get(`/api/search?query=${encodeURIComponent(editorSearchQuery)}`);
      // Extract users from the response (search returns both players and tournaments)
      setSearchResults(response.data.players || []);
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
      (player.name && player.name.toLowerCase().includes(playerSearchQuery.toLowerCase()))
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
    isEditingSeedsRef.current = true; // Mark as editing
    setManualSeeds(prev => {
      const updated = { ...prev };
      if (seedValue === '' || seedValue === null) {
        delete updated[userId];
      } else {
        updated[userId] = parseInt(seedValue);
      }
      // If no more manual seeds, stop editing
      if (Object.keys(updated).length === 0) {
        isEditingSeedsRef.current = false;
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

      // Clear editing flag
      isEditingSeedsRef.current = false;

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

      // Clear editing flag
      isEditingSeedsRef.current = false;

      // Clear both seeds and manual seeds state immediately
      setSeeds({});
      setManualSeeds({});
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to clear seeds');
    }
  };

  const handleAutoSeed = async () => {
    const numberOfSeeds = prompt(
      `How many players should be seeded? (1-${eventPlayers.length})`,
      Math.min(4, eventPlayers.length).toString()
    );

    if (!numberOfSeeds) return; // User cancelled

    const seedCount = parseInt(numberOfSeeds);
    if (isNaN(seedCount) || seedCount < 1 || seedCount > eventPlayers.length) {
      showMessage('error', `Please enter a number between 1 and ${eventPlayers.length}`);
      return;
    }

    try {
      await authAxios.post(
        `/api/tournaments/${tournamentId}/event/${selectedEvent.id}/seeds/auto`,
        { numberOfSeeds: seedCount }
      );
      showMessage('success', `Auto-seeded ${seedCount} players successfully!`);

      // Refresh seeds from server
      const seedsRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/seeds`);
      setSeeds(seedsRes.data || {});
      setManualSeeds({});
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to auto-seed players');
    }
  };

  // Team creation for partner matching
  const handleCreateTeam = async (player1Id, player2Id) => {
    if (!player2Id) {
      showMessage('error', 'Please select a partner from the dropdown');
      return;
    }

    if (player1Id === player2Id) {
      showMessage('error', 'Cannot create a team with the same player twice');
      return;
    }

    try {
      await authAxios.post(
        `/api/tournaments/${tournamentId}/event/${selectedEvent.id}/create-team`,
        { player1Id, player2Id }
      );

      showMessage('success', 'Team created successfully!');

      // Refresh teams list
      const teamsRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/teams`);
      setTeams(teamsRes.data || []);

      // Clear the selected partner for this player
      setSelectedPartners(prev => {
        const updated = { ...prev };
        delete updated[player1Id];
        return updated;
      });
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to create team');
    }
  };

  const handleDeleteTeam = async (teamId) => {
    if (!window.confirm('Are you sure you want to delete this team?')) {
      return;
    }

    try {
      await authAxios.delete(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/teams/${teamId}`);

      showMessage('success', 'Team deleted successfully!');

      // Refresh teams list
      const teamsRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/teams`);
      setTeams(teamsRes.data || []);
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to delete team');
    }
  };

  const handleRemovePlayer = async (playerId) => {
    if (!window.confirm('Are you sure you want to remove this player from the event?')) {
      return;
    }

    try {
      await authAxios.delete(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/players/${playerId}`);

      showMessage('success', 'Player removed successfully!');

      // Refresh players list
      const playersRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/players`);
      setEventPlayers(playersRes.data || []);
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to remove player');
    }
  };

  // Team seeding management
  const handleTeamSeedChange = (teamId, seedValue) => {
    isEditingTeamSeedsRef.current = true; // Mark as editing
    setManualTeamSeeds(prev => {
      const updated = { ...prev };
      if (seedValue === '' || seedValue === null) {
        delete updated[teamId];
      } else {
        updated[teamId] = parseInt(seedValue);
      }
      // If no more manual seeds, stop editing
      if (Object.keys(updated).length === 0) {
        isEditingTeamSeedsRef.current = false;
      }
      return updated;
    });
  };

  const handleSaveTeamSeeds = async () => {
    try {
      // Validate seeds
      const seedValues = Object.values(manualTeamSeeds);
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
        `/api/tournaments/${tournamentId}/event/${selectedEvent.id}/seeds/teams/manual`,
        { teamSeeds: manualTeamSeeds }
      );

      showMessage('success', 'Team seeds saved successfully!');

      // Clear editing flag
      isEditingTeamSeedsRef.current = false;

      // Refresh seeds from server
      const seedsRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/seeds/teams`);
      setTeamSeeds(seedsRes.data || {});
      setManualTeamSeeds({});
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to save team seeds');
    }
  };

  const handleClearTeamSeeds = async () => {
    if (!window.confirm('Clear all team seeds? This cannot be undone.')) return;

    try {
      await authAxios.delete(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/seeds`);
      showMessage('success', 'Team seeds cleared successfully!');

      // Clear editing flag
      isEditingTeamSeedsRef.current = false;

      // Clear both team seeds and manual team seeds state immediately
      setTeamSeeds({});
      setManualTeamSeeds({});
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to clear team seeds');
    }
  };

  const handleAutoSeedTeams = async () => {
    const numberOfSeeds = prompt(
      `How many teams should be seeded? (1-${teams.length})`,
      Math.min(4, teams.length).toString()
    );

    if (!numberOfSeeds) return; // User cancelled

    const seedCount = parseInt(numberOfSeeds);
    if (isNaN(seedCount) || seedCount < 1 || seedCount > teams.length) {
      showMessage('error', `Please enter a number between 1 and ${teams.length}`);
      return;
    }

    try {
      await authAxios.post(
        `/api/tournaments/${tournamentId}/event/${selectedEvent.id}/seeds/teams/auto`,
        { numberOfSeeds: seedCount }
      );
      showMessage('success', `Auto-seeded ${seedCount} teams successfully!`);

      // Refresh seeds from server
      const seedsRes = await authAxios.get(`/api/tournaments/${tournamentId}/event/${selectedEvent.id}/seeds/teams`);
      setTeamSeeds(seedsRes.data || {});
      setManualTeamSeeds({});
    } catch (err) {
      showMessage('error', err.response?.data || 'Failed to auto-seed teams');
    }
  };

  // Helper functions to get participant names and IDs (supports both players and teams)
  const getParticipantName = (match, side) => {
    // For doubles/team-based matches
    if (side === 'A' && match.teamA) {
      return match.teamA.teamName || 'Team A';
    }
    if (side === 'B' && match.teamB) {
      return match.teamB.teamName || 'Team B';
    }

    // For singles/player-based matches
    if (side === 'A' && match.playerA) {
      return match.playerA.name || match.playerA.username || 'Player A';
    }
    if (side === 'B' && match.playerB) {
      return match.playerB.name || match.playerB.username || 'Player B';
    }

    return 'TBD';
  };

  const getParticipantId = (match, side) => {
    if (side === 'A') {
      return match.teamA?.id || match.playerA?.id;
    }
    return match.teamB?.id || match.playerB?.id;
  };

  const getWinnerName = (match) => {
    if (match.winnerTeam) {
      return match.winnerTeam.teamName || 'Winner Team';
    }
    if (match.winner) {
      return match.winner.name || match.winner.username || 'Winner';
    }
    return 'Unknown';
  };

  const hasParticipants = (match) => {
    // Check if participant exists AND has a valid ID (not an empty DTO)
    const hasValidA = (match.teamA?.id || match.playerA?.id);
    const hasValidB = (match.teamB?.id || match.playerB?.id);
    return !!(hasValidA && hasValidB);
  };

  // Helper to get list of player IDs that are already in teams
  const getPairedPlayerIds = () => {
    const pairedIds = new Set();
    teams.forEach(team => {
      if (team.player1?.id) pairedIds.add(team.player1.id);
      if (team.player2?.id) pairedIds.add(team.player2.id);
    });
    return pairedIds;
  };

  // Helper to filter out players who are already paired
  const getAvailablePlayers = () => {
    const pairedIds = getPairedPlayerIds();
    return eventPlayers.filter(player => !pairedIds.has(player.id));
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
              disabled={!selectedEvent || selectedEvent.initialized}
              title={selectedEvent?.initialized ? 'Cannot modify registrations after event is initialized' : ''}
            >
              Registrations {pendingRegistrations.length > 0 && (
                <span className={styles.badge}>{pendingRegistrations.length}</span>
              )}
            </button>
            <button
              className={`${styles.sidebarButton} ${activeSection === 'players' ? styles.active : ''}`}
              onClick={() => setActiveSection('players')}
              disabled={!selectedEvent || selectedEvent.initialized}
              title={selectedEvent?.initialized ? 'Cannot add players after event is initialized' : ''}
            >
              Players
            </button>
            {selectedEvent?.matchType === 'DOUBLES' && (
              <button
                className={`${styles.sidebarButton} ${activeSection === 'partnerMatching' ? styles.active : ''}`}
                onClick={() => setActiveSection('partnerMatching')}
                disabled={!selectedEvent || selectedEvent.initialized}
                title={selectedEvent?.initialized ? 'Cannot modify teams after event is initialized' : ''}
              >
                Partner Matching
              </button>
            )}
            <button
              className={`${styles.sidebarButton} ${activeSection === 'matchConfig' ? styles.active : ''}`}
              onClick={() => setActiveSection('matchConfig')}
              disabled={!selectedEvent || selectedEvent.initialized}
              title={selectedEvent?.initialized ? 'Cannot change match settings after event is initialized' : ''}
            >
              Match Configuration
            </button>
            <button
              className={`${styles.sidebarButton} ${activeSection === 'seeding' ? styles.active : ''}`}
              onClick={() => setActiveSection('seeding')}
              disabled={!selectedEvent || selectedEvent.initialized}
              title={selectedEvent?.initialized ? 'Cannot modify seeds after event is initialized' : ''}
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
                    <span className={styles.detailValue}>
                      <PlayerLink player={tournament.owner} />
                    </span>
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
              ) : selectedEvent.initialized ? (
                <div className={styles.infoCard} style={{ background: '#fff3cd', borderLeft: '4px solid #ffc107' }}>
                  <h3 style={{ marginTop: 0, color: '#856404' }}>Section Locked</h3>
                  <p style={{ color: '#856404' }}>
                    Registrations cannot be modified after the event has been initialized.
                    The bracket has already been generated with the current participants.
                  </p>
                  <p style={{ color: '#856404', marginBottom: 0 }}>
                    To make changes, you must first de-initialize the event from the Events section.
                  </p>
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
                        <th>Desired Partner</th>
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
                          <td><PlayerLink player={reg.user} /></td>
                          <td>{reg.user?.name || '-'}</td>
                          <td>{reg.desiredPartner || '-'}</td>
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
              {selectedEvent.initialized ? (
                <div className={styles.infoCard} style={{ background: '#fff3cd', borderLeft: '4px solid #ffc107' }}>
                  <h3 style={{ marginTop: 0, color: '#856404' }}>Section Locked</h3>
                  <p style={{ color: '#856404' }}>
                    Players cannot be added or removed after the event has been initialized.
                    The bracket has already been generated with the current participants.
                  </p>
                  <p style={{ color: '#856404', marginBottom: 0 }}>
                    To make changes, you must first de-initialize the event from the Events section.
                  </p>
                </div>
              ) : (
                <>
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

                  {Object.keys(seeds).length > 0 && (
                    <div className={styles.infoCard} style={{ background: '#fff3cd', borderLeft: '4px solid #ffc107', marginBottom: '20px' }}>
                      <h3 style={{ marginTop: 0, color: '#856404', fontSize: '1rem' }}>⚠️ Seeding Impact</h3>
                      <p style={{ color: '#856404', marginBottom: '8px', fontSize: '0.9rem' }}>
                        <strong>Removing a seeded player will automatically adjust remaining seeds:</strong>
                      </p>
                      <ul style={{ color: '#856404', marginLeft: '20px', marginBottom: '8px', fontSize: '0.9rem', lineHeight: '1.5' }}>
                        <li>If you remove Seed #3, all higher seeds (4, 5, 6...) will be decremented by 1 to maintain sequential numbering (1, 2, 3...)</li>
                        <li>Seeds lower than the removed seed remain unchanged</li>
                        <li>This ensures your bracket stays valid for initialization</li>
                      </ul>
                      <p style={{ color: '#856404', marginBottom: 0, fontSize: '0.85rem', fontStyle: 'italic' }}>
                        Example: Seeds [1, 2, 3, 4] → Remove Seed #2 → Result: [1, 2, 3] (old seed 3 becomes 2, old seed 4 becomes 3)
                      </p>
                    </div>
                  )}

                  {eventPlayers.length === 0 ? (
                    <div className={styles.emptyState}>
                      <p>No players in this event yet. Add players to get started!</p>
                    </div>
                  ) : (
                    <div className={styles.playersGrid}>
                      {eventPlayers.map(player => (
                        <div key={player.id} className={styles.playerCard}>
                          <div>
                            <div className={styles.playerName}>
                              <PlayerLink player={player} />
                            </div>
                            <div className={styles.playerDetail}>@{player.username}</div>
                          </div>
                          <button
                            className={styles.deleteButton}
                            onClick={() => handleRemovePlayer(player.id)}
                            title="Remove player from event"
                          >
                            ×
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                </>
              )}
            </div>
          )}

          {/* Partner Matching Section */}
          {activeSection === 'partnerMatching' && selectedEvent && (
            <div className={styles.section}>
              <h2 className={styles.sectionTitle}>Partner Matching - {selectedEvent.name}</h2>

              {selectedEvent.initialized ? (
                <div className={styles.infoCard} style={{ background: '#fff3cd', borderLeft: '4px solid #ffc107' }}>
                  <h3 style={{ marginTop: 0, color: '#856404' }}>Section Locked</h3>
                  <p style={{ color: '#856404' }}>
                    This section cannot be modified once the event has been initialized. All partner matching and team creation must be completed before initialization.
                  </p>
                  <p style={{ color: '#856404', marginBottom: 0 }}>
                    To make changes, you must first de-initialize the event from the Events section.
                  </p>
                </div>
              ) : (
                <>
                  {/* Players with desired partners - show all who specified a partner */}
                  {(() => {
                    // Filter all registrations (pending + approved) for those with desired partners
                    const pairedIds = getPairedPlayerIds();
                    const registrationsWithPartners = allRegistrations.filter(r =>
                      r.desiredPartner &&
                      r.desiredPartner.trim() !== '' &&
                      (r.status === 'PENDING' || r.status === 'APPROVED') &&
                      !pairedIds.has(r.user?.id) // Filter out players who are already paired
                    );

                    return registrationsWithPartners.length > 0 && (
                      <div className={styles.infoCard}>
                        <h3>Players with Desired Partners ({registrationsWithPartners.length})</h3>
                        <p style={{ marginBottom: '20px' }}>
                          Below are all players who specified a desired partner during signup. Match them with actual users to create teams.
                          This allows you to handle typos and resolve any partner mismatches.
                        </p>

                        <div className={styles.table}>
                          <table>
                            <thead>
                              <tr>
                                <th>Player</th>
                                <th>Desired Partner (as typed)</th>
                                <th>Match with User</th>
                                <th>Actions</th>
                              </tr>
                            </thead>
                            <tbody>
                              {registrationsWithPartners.map(reg => (
                                <tr key={reg.user?.id || reg.id}>
                                  <td>
                                    <div>
                                      <strong><PlayerLink player={reg.user} /></strong>
                                      <div style={{ fontSize: '0.85rem', color: '#666', marginTop: '2px' }}>
                                        @{reg.user?.username}
                                      </div>
                                    </div>
                                  </td>
                                  <td>
                                    <span style={{
                                      padding: '4px 10px',
                                      backgroundColor: '#f0f0f0',
                                      borderRadius: '4px',
                                      fontFamily: 'monospace',
                                      fontSize: '0.95rem',
                                      fontWeight: '600'
                                    }}>
                                      {reg.desiredPartner}
                                    </span>
                                  </td>
                                  <td>
                                    <select
                                      className={styles.select}
                                      value={selectedPartners[reg.user.id] || ''}
                                      onChange={(e) => setSelectedPartners({
                                        ...selectedPartners,
                                        [reg.user.id]: e.target.value
                                      })}
                                    >
                                      <option value="">Select actual partner...</option>
                                      {getAvailablePlayers()
                                        .filter(p => p.id !== reg.user.id)
                                        .map(player => (
                                          <option key={player.id} value={player.id}>
                                            {player.name || player.username}
                                          </option>
                                        ))}
                                    </select>
                                  </td>
                                  <td>
                                    <button
                                      className={styles.primaryButtonSmall}
                                      onClick={() => handleCreateTeam(reg.user.id, parseInt(selectedPartners[reg.user.id]))}
                                      disabled={!selectedPartners[reg.user.id]}
                                    >
                                      Create Team
                                    </button>
                                  </td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                      </div>
                    );
                  })()}

                  {/* Manual team creation for any players */}
                  <div className={styles.infoCard} style={{ marginTop: '20px' }}>
                    <h3>Manual Team Creation</h3>
                    <p style={{ marginBottom: '20px' }}>
                      Create teams manually by selecting any two players from the event.
                    </p>

                    {getAvailablePlayers().length < 2 ? (
                      <p style={{ color: '#999' }}>
                        {eventPlayers.length < 2
                          ? 'Need at least 2 players to create a team. Approve registrations first.'
                          : 'All available players have been paired into teams.'}
                      </p>
                    ) : (
                      <div style={{ display: 'flex', gap: '16px', alignItems: 'flex-end', flexWrap: 'wrap' }}>
                        <div style={{ flex: '1', minWidth: '200px' }}>
                          <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#555' }}>
                            Player 1:
                          </label>
                          <select
                            className={styles.select}
                            value={selectedPartners['manual1'] || ''}
                            onChange={(e) => setSelectedPartners({
                              ...selectedPartners,
                              manual1: e.target.value
                            })}
                          >
                            <option value="">Select first player...</option>
                            {getAvailablePlayers().map(player => (
                              <option key={player.id} value={player.id}>
                                {player.name || player.username}
                              </option>
                            ))}
                          </select>
                        </div>

                        <div style={{ flex: '1', minWidth: '200px' }}>
                          <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#555' }}>
                            Player 2:
                          </label>
                          <select
                            className={styles.select}
                            value={selectedPartners['manual2'] || ''}
                            onChange={(e) => setSelectedPartners({
                              ...selectedPartners,
                              manual2: e.target.value
                            })}
                          >
                            <option value="">Select second player...</option>
                            {getAvailablePlayers()
                              .filter(p => p.id.toString() !== selectedPartners['manual1'])
                              .map(player => (
                                <option key={player.id} value={player.id}>
                                  {player.name || player.username}
                                </option>
                              ))}
                          </select>
                        </div>

                        <button
                          className={styles.primaryButton}
                          onClick={() => {
                            handleCreateTeam(parseInt(selectedPartners['manual1']), parseInt(selectedPartners['manual2']));
                            setSelectedPartners(prev => {
                              const updated = { ...prev };
                              delete updated.manual1;
                              delete updated.manual2;
                              return updated;
                            });
                          }}
                          disabled={!selectedPartners['manual1'] || !selectedPartners['manual2']}
                        >
                          Create Team
                        </button>
                      </div>
                    )}
                  </div>

                  {/* Display already created teams */}
                  {teams.length > 0 && (
                    <div className={styles.infoCard} style={{ marginTop: '20px' }}>
                      <h3>Created Teams ({teams.length}):</h3>
                      <div className={styles.teamsList}>
                        {teams.map(team => (
                          <div key={team.id} className={styles.teamCard}>
                            <span className={styles.teamName}>{team.teamName}</span>
                            <button
                              className={styles.deleteButton}
                              onClick={() => handleDeleteTeam(team.id)}
                              title="Delete team"
                            >
                              ✕
                            </button>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </>
              )}
            </div>
          )}

          {/* Seeding Section */}
          {activeSection === 'seeding' && selectedEvent && (
            <div className={styles.section}>
              {selectedEvent.initialized ? (
                <div className={styles.infoCard} style={{ background: '#fff3cd', borderLeft: '4px solid #ffc107' }}>
                  <h3 style={{ marginTop: 0, color: '#856404' }}>Section Locked</h3>
                  <p style={{ color: '#856404' }}>
                    Seeding cannot be modified after the event has been initialized.
                    The bracket has already been generated with the current seeds.
                  </p>
                  <p style={{ color: '#856404', marginBottom: 0 }}>
                    To make changes, you must first de-initialize the event from the Events section.
                  </p>
                </div>
              ) : (
                <>
                  <h2 className={styles.sectionTitle}>
                    Manual Seeding - {selectedEvent.name} {selectedEvent.matchType === 'DOUBLES' && '(Team Seeding)'}
                  </h2>
                  <div className={styles.infoCard} style={{ marginBottom: '20px' }}>
                    <h3 style={{ marginTop: 0, marginBottom: '12px', fontSize: '1.1rem' }}>How Seeding Works</h3>

                    <p style={{ marginBottom: '10px' }}>
                      <strong>Seeding controls bracket positions, not byes:</strong>
                    </p>

                    <ul style={{ marginLeft: '20px', marginBottom: '12px', lineHeight: '1.6' }}>
                      <li><strong>Seeded {selectedEvent.matchType === 'DOUBLES' ? 'teams' : 'players'}:</strong> Placed at specific bracket positions using standard tournament seeding (Seed 1 at top, Seed 2 opposite side, etc.). This ensures top seeds don't meet until later rounds.</li>
                      <li><strong>Unseeded {selectedEvent.matchType === 'DOUBLES' ? 'teams' : 'players'}:</strong> Randomly assigned to all remaining bracket positions</li>
                      <li><strong>Byes:</strong> Only occur when total participants is less than bracket size (4, 8, 16, 32, etc.). Byes fill empty positions AFTER all participants are placed. <em>Seeded players are NOT guaranteed byes.</em></li>
                    </ul>

                    <p style={{ marginBottom: '10px' }}>
                      <strong>Example:</strong> 6 players, Seeds 1-2 set in an 8-player bracket:
                      <br />• Seed 1 → Position 0, Seed 2 → Position 4
                      <br />• 4 unseeded players randomly fill positions 1, 2, 3, 5, 6, 7
                      <br />• Result: All 6 players have first-round matches (no byes)
                    </p>

                    <p style={{ marginTop: '12px', padding: '8px', background: '#fff3cd', borderLeft: '3px solid #ffc107', fontSize: '0.9rem' }}>
                      <strong>Key Point:</strong> Seeding determines <em>where</em> participants are placed in the bracket to avoid early matchups between top seeds. It does NOT give seeded players free passes.
                    </p>

                    <p style={{ marginTop: '12px', padding: '8px', background: '#f0f9ff', borderLeft: '3px solid #3b82f6', fontSize: '0.9rem' }}>
                      <strong>Requirements:</strong> Seeds must be sequential starting from 1 (e.g., 1, 2, 3, 4).
                      Leave blank for unseeded {selectedEvent.matchType === 'DOUBLES' ? 'teams' : 'players'}.
                    </p>

                    <p style={{ marginTop: '12px' }}>
                      <strong>
                        Current saved seeds: {selectedEvent.matchType === 'DOUBLES' ? Object.keys(teamSeeds).length : Object.keys(seeds).length}
                      </strong>
                    </p>
                  </div>

                  {/* Two-column layout: Seeding inputs on left, Bracket preview on right */}
                  <div className={styles.seedingContainer}>
                    {/* Left column: Seeding inputs */}
                    <div className={styles.seedingInputsColumn}>
                      {selectedEvent.matchType === 'DOUBLES' ? (
                        // Team seeding UI
                        <>
                          <div className={styles.seedingControls}>
                            <button onClick={handleAutoSeedTeams} className={styles.secondaryButton}>
                              Auto-Seed Teams
                            </button>
                            <button onClick={handleSaveTeamSeeds} className={styles.primaryButton}>
                              Save Team Seeds
                            </button>
                            {Object.keys(teamSeeds).length > 0 && (
                              <button onClick={handleClearTeamSeeds} className={styles.dangerButton}>
                                Clear All Team Seeds
                              </button>
                            )}
                          </div>

                          <div className={styles.playerSeedList}>
                            {teams.length === 0 ? (
                              <p>No teams created for this event yet. Create teams from the Partner Matching section.</p>
                            ) : (
                              teams.map(team => (
                                <div key={team.id} className={styles.playerSeedRow}>
                                  <div className={styles.playerInfo}>
                                    <span className={styles.playerUsername}>{team.teamName}</span>
                                  </div>
                                  <div className={styles.seedInputContainer}>
                                    <label htmlFor={`seed-${team.id}`}>Seed:</label>
                                    <input
                                      id={`seed-${team.id}`}
                                      type="number"
                                      min="1"
                                      max={teams.length}
                                      value={manualTeamSeeds[team.id] || ''}
                                      onChange={(e) => handleTeamSeedChange(team.id, e.target.value)}
                                      placeholder={teamSeeds[team.id] ? `Current: ${teamSeeds[team.id]}` : 'None'}
                                      className={styles.seedInput}
                                    />
                                  </div>
                                </div>
                              ))
                            )}
                          </div>
                        </>
                      ) : (
                        // Player seeding UI
                        <>
                          <div className={styles.seedingControls}>
                            <button onClick={handleAutoSeed} className={styles.secondaryButton}>
                              Auto-Seed Players
                            </button>
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
                                    <span className={styles.playerUsername}>
                                      <PlayerLink player={player} />
                                    </span>
                                    <span className={styles.playerRealName}>@{player.username}</span>
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

                    {/* Right column: Bracket preview (only for single/double elim) */}
                    {supportsPreview(selectedEvent.eventType) && (
                      <div className={styles.bracketPreviewColumn}>
                        <BracketPreview
                          players={eventPlayers}
                          teams={teams}
                          seeds={manualSeeds}
                          teamSeeds={manualTeamSeeds}
                          matchType={selectedEvent.matchType}
                          eventType={selectedEvent.eventType}
                        />
                      </div>
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
                        <div className={styles.player}>{getParticipantName(match, 'A')}</div>
                        <div className={styles.vs}>vs</div>
                        <div className={styles.player}>{getParticipantName(match, 'B')}</div>
                      </div>
                      {match.completed ? (
                        <div className={styles.matchResult}>
                          <div className={styles.winner}>Winner: {getWinnerName(match)}</div>
                          <div className={styles.score}>Score: {match.score}</div>
                        </div>
                      ) : (
                        <button
                          className={styles.primaryButton}
                          onClick={() => {
                            setSelectedMatch(match);
                            setShowRecordResultModal(true);
                          }}
                          disabled={!hasParticipants(match)}
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

          {/* Match Configuration Section */}
          {activeSection === 'matchConfig' && selectedEvent && (
            <div className={styles.section}>
              {selectedEvent.initialized ? (
                <div className={styles.infoCard} style={{ background: '#fff3cd', borderLeft: '4px solid #ffc107' }}>
                  <h3 style={{ marginTop: 0, color: '#856404' }}>Section Locked</h3>
                  <p style={{ color: '#856404' }}>
                    Match configuration cannot be changed after the event has been initialized.
                    The bracket has already been generated with these settings.
                  </p>
                  <p style={{ color: '#856404', marginBottom: 0 }}>
                    To make changes, you must first de-initialize the event from the Events section.
                  </p>
                </div>
              ) : (
                <>
                  <div className={styles.sectionHeader}>
                    <h2 className={styles.sectionTitle}>Match Configuration - {selectedEvent.name}</h2>
                    <button
                      className={styles.primaryButton}
                      onClick={handleSaveMatchConfig}
                    >
                      Save Configuration
                    </button>
                  </div>

              <div className={styles.configForm}>
                <div className={styles.formGroup}>
                  <label className={styles.label}>Match Type</label>
                  <select
                    className={styles.select}
                    value={matchConfig.matchType}
                    onChange={(e) => setMatchConfig(prev => ({ ...prev, matchType: e.target.value }))}
                    disabled={selectedEvent.initialized}
                  >
                    <option value="SINGLES">Singles (1v1)</option>
                    <option value="DOUBLES">Doubles (2v2)</option>
                  </select>
                  <p className={styles.helpText}>
                    {matchConfig.matchType === 'SINGLES'
                      ? 'Each match will have one player per side.'
                      : 'Each match will have two players per side (teams).'}
                  </p>
                </div>

                <div className={styles.formGroup}>
                  <label className={styles.label}>Games Per Match</label>
                  <select
                    className={styles.select}
                    value={matchConfig.gamesPerMatch}
                    onChange={(e) => setMatchConfig(prev => ({ ...prev, gamesPerMatch: parseInt(e.target.value) }))}
                    disabled={selectedEvent.initialized}
                  >
                    <option value={1}>1 Game (Single Game)</option>
                    <option value={3}>3 Games (Best of 3)</option>
                    <option value={5}>5 Games (Best of 5)</option>
                    <option value={7}>7 Games (Best of 7)</option>
                    <option value={9}>9 Games (Best of 9)</option>
                  </select>
                  <p className={styles.helpText}>
                    {matchConfig.gamesPerMatch === 1
                      ? 'Single game decides the match.'
                      : `First to win ${Math.floor(matchConfig.gamesPerMatch / 2) + 1} games wins the match.`}
                  </p>
                </div>

                <div className={styles.infoBox}>
                  <h3>Current Configuration</h3>
                  <ul>
                    <li><strong>Type:</strong> {matchConfig.matchType === 'SINGLES' ? 'Singles (1v1)' : 'Doubles (2v2)'}</li>
                    <li><strong>Games:</strong> {matchConfig.gamesPerMatch === 1 ? 'Single game' : `Best of ${matchConfig.gamesPerMatch}`}</li>
                    <li><strong>Games to win:</strong> {Math.floor(matchConfig.gamesPerMatch / 2) + 1}</li>
                  </ul>
                </div>
              </div>
                </>
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
                          <td>
                            <PlayerLink player={editor} />
                            {editor.id === tournament?.owner?.id && (
                              <span className={styles.ownerBadge}> (Owner)</span>
                            )}
                          </td>
                          <td>@{editor.username}</td>
                          <td>
                            {/* Only show Remove button if current user is owner AND editor is not the owner */}
                            {user && tournament?.owner?.id === user.id && editor.id !== tournament?.owner?.id ? (
                              <button
                                className={styles.dangerButtonSmall}
                                onClick={() => handleRemoveEditor(editor.id)}
                              >
                                Remove
                              </button>
                            ) : (
                              <span style={{color: '#999'}}>—</span>
                            )}
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
                  {/* <option value="DOUBLE_ELIM">Double Elimination</option> */}
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
              <p><strong>{getParticipantName(selectedMatch, 'A')}</strong> vs <strong>{getParticipantName(selectedMatch, 'B')}</strong></p>
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
                  {getParticipantId(selectedMatch, 'A') && (
                    <option value={getParticipantId(selectedMatch, 'A')}>{getParticipantName(selectedMatch, 'A')}</option>
                  )}
                  {getParticipantId(selectedMatch, 'B') && (
                    <option value={getParticipantId(selectedMatch, 'B')}>{getParticipantName(selectedMatch, 'B')}</option>
                  )}
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
                        <div style={{ fontWeight: 600, color: '#2d3748' }}>
                          <PlayerLink player={user} />
                        </div>
                        <div style={{ fontSize: '0.9rem', color: '#718096' }}>@{user.username}</div>
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
                          <div style={{ fontWeight: 600, color: '#2d3748' }}>
                            <PlayerLink player={player} />
                          </div>
                          <div style={{ fontSize: '0.9rem', color: '#718096' }}>@{player.username}</div>
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
