import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import SingleElimBracket from '../../Components/SingleElimEvent/SingleElimBracket';
import RoundRobinBracket from '../../Components/RoundRobinEvent/RoundRobinBracket';
import DoubleElimBracket from '../../Components/DoubleElimEvent/DoubleElimBracket';
import authAxios from '../../utils/authAxios';
import publicAxios from '../../utils/publicAxios';
import { useAuth } from '../../contexts/AuthContext';
import PlayerLink from '../../Components/PlayerLink/PlayerLink';
import styles from './EventPage.module.css';

function EventPage() {
    const { tournamentId, eventIndex } = useParams();
    const navigate = useNavigate();
    const { isLoggedIn } = useAuth();
    const [eventType, setEventType] = useState(null);
    const [draw, setDraw] = useState(null);
    const [tournament, setTournament] = useState(null);
    const [event, setEvent] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentUser, setCurrentUser] = useState(null);
    const [isAuthorized, setIsAuthorized] = useState(false);
    const [players, setPlayers] = useState([]);
    const [teams, setTeams] = useState([]);

    useEffect(() => {
        const fetchData = async () => {
            try {
                // Fetch current user if authenticated
                if (isLoggedIn) {
                    try {
                        const userResponse = await authAxios.get('/api/users/me');
                        setCurrentUser(userResponse.data);
                    } catch (err) {
                        console.error(err);
                    }
                }

                // Fetch tournament general info (public data)
                const tournamentResponse = await publicAxios.get(`/api/tournaments/${tournamentId}`);
                setTournament(tournamentResponse.data);

                // Fetch event info from events list to check if initialized
                const eventsResponse = await publicAxios.get(`/api/tournaments/${tournamentId}/events`);
                const eventData = eventsResponse.data[parseInt(eventIndex)];
                setEvent(eventData);

                // If event is initialized, fetch the draw
                if (eventData?.initialized) {
                    const drawResponse = await publicAxios.get(`/api/tournaments/${tournamentId}/event/${eventIndex}/draw`);
                    const { eventType, draw } = drawResponse.data;
                    setEventType(eventType);
                    setDraw(draw);
                } else {
                    // If not initialized, fetch players and teams
                    const playersResponse = await publicAxios.get(`/api/tournaments/${tournamentId}/event/${eventIndex}/players`);
                    setPlayers(playersResponse.data || []);

                    // Fetch teams for doubles events
                    if (eventData?.matchType === 'DOUBLES') {
                        try {
                            const teamsResponse = await publicAxios.get(`/api/tournaments/${tournamentId}/event/${eventIndex}/teams`);
                            setTeams(teamsResponse.data || []);
                        } catch (err) {
                            // Teams endpoint might not exist or fail, that's okay
                            setTeams([]);
                        }
                    }
                }
            } catch (err) {
                setError('Failed to load event data.');
            } finally {
                setLoading(false);
            }
        };
        if (tournamentId && eventIndex !== undefined) fetchData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [tournamentId, eventIndex, isLoggedIn]);

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
        if (!isLoggedIn) {
            navigate('/register');
            return;
        }

        // Verify token is valid by making a quick API call
        authAxios.get('/api/users/me')
            .then(() => {
                navigate(`/tournament/${tournamentId}/signup`);
            })
            .catch(() => {
                // Token is invalid or expired - auth context will handle clearing
                navigate('/register');
            });
    };

    if (loading) return <div>Loading bracket...</div>;
    if (error) return <div>{error}</div>;

    const renderPlayerList = () => {
        // Get list of player IDs who are in teams
        const pairedPlayerIds = new Set();
        teams.forEach(team => {
            if (team.player1?.id) pairedPlayerIds.add(team.player1.id);
            if (team.player2?.id) pairedPlayerIds.add(team.player2.id);
        });

        // Filter unpaired players
        const unpairedPlayers = players.filter(p => !pairedPlayerIds.has(p.id));

        return (
            <div className={styles.playerListContainer}>
                <h2 className={styles.sectionTitle}>Current Registrations</h2>
                <p className={styles.subtitle}>The event has not started yet. Participants will be listed here.</p>

                {event?.matchType === 'DOUBLES' && teams.length > 0 && (
                    <div className={styles.section}>
                        <h3 className={styles.subsectionTitle}>Teams ({teams.length})</h3>
                        <table className={styles.playerTable}>
                            <thead>
                                <tr>
                                    <th>Team</th>
                                    <th>Player 1</th>
                                    <th>Player 2</th>
                                </tr>
                            </thead>
                            <tbody>
                                {teams.map((team, index) => (
                                    <tr key={team.id}>
                                        <td>Team {index + 1}</td>
                                        <td>
                                            {team.player1 ? <PlayerLink player={team.player1} /> : '-'}
                                        </td>
                                        <td>
                                            {team.player2 ? <PlayerLink player={team.player2} /> : '-'}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}

                {event?.matchType === 'DOUBLES' && unpairedPlayers.length > 0 && (
                    <div className={styles.section}>
                        <h3 className={styles.subsectionTitle}>Players Without Team ({unpairedPlayers.length})</h3>
                        <table className={styles.playerTable}>
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Player</th>
                                </tr>
                            </thead>
                            <tbody>
                                {unpairedPlayers.map((player, index) => (
                                    <tr key={player.id}>
                                        <td>{index + 1}</td>
                                        <td><PlayerLink player={player} /></td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}

                {event?.matchType !== 'DOUBLES' && players.length > 0 && (
                    <div className={styles.section}>
                        <h3 className={styles.subsectionTitle}>Players ({players.length})</h3>
                        <table className={styles.playerTable}>
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Player</th>
                                </tr>
                            </thead>
                            <tbody>
                                {players.map((player, index) => (
                                    <tr key={player.id}>
                                        <td>{index + 1}</td>
                                        <td><PlayerLink player={player} /></td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}

                {players.length === 0 && teams.length === 0 && (
                    <p className={styles.emptyMessage}>No players have registered for this event yet.</p>
                )}
            </div>
        );
    };

    const renderBracket = () => {
        switch (eventType) {
            case 'SINGLE_ELIM':
                return <SingleElimBracket draw={draw} />;
            case 'ROUND_ROBIN':
                return <RoundRobinBracket draw={draw} />;
            case 'DOUBLE_ELIM':
                return <DoubleElimBracket draw={draw} />;
            default:
                return <div>Unsupported event type: {eventType}</div>;
        }
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
                                    <span><PlayerLink player={tournament.owner} /></span>
                                </div>
                            )}
                            <div className={styles.metaItem}>
                                <span className={styles.metaLabel}>Event:</span>
                                <span>{eventIndex}</span>
                            </div>
                        </div>
                    )}
                </div>
                <div className={styles.headerButtons}>
                    {isAuthorized && (
                        <button
                            onClick={() => navigate(`/tournament/${tournamentId}/control`)}
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

            {/* Bracket Content Area */}
            <div className={styles.bracketContainer}>
                {event?.initialized ? renderBracket() : renderPlayerList()}
            </div>
        </div>
    );
}

export default EventPage;