import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import authAxios from '../../utils/authAxios';
import styles from './PlayerPage.module.css';

function PlayerPage() {
    const { playerId } = useParams();
    const [player, setPlayer] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [activeTab, setActiveTab] = useState('overview');

    useEffect(() => {
        const fetchPlayerData = async () => {
            try {
                setLoading(true);
                const response = await authAxios.get(`/api/users/${playerId}`);
                setPlayer(response.data);
            } catch (err) {
                setError('Failed to load player data.');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        if (playerId) {
            fetchPlayerData();
        }
    }, [playerId]);

    if (loading) {
        return (
            <div className={styles.loadingContainer}>
                <div className={styles.spinner}></div>
                <p>Loading player profile...</p>
            </div>
        );
    }

    if (error || !player) {
        return (
            <div className={styles.errorContainer}>
                <h2>Error</h2>
                <p>{error || 'Player not found'}</p>
            </div>
        );
    }

    // Calculate total statistics
    const totalTournaments = player.tournaments?.length || 0;
    const totalMatches = player.tournaments?.reduce((total, tournament) => {
        if (!tournament.eventMatches) return total;
        const matchCount = Object.values(tournament.eventMatches).reduce(
            (sum, matches) => sum + matches.length,
            0
        );
        return total + matchCount;
    }, 0) || 0;

    const totalWins = player.tournaments?.reduce((total, tournament) => {
        if (!tournament.eventMatches) return total;
        const wins = Object.values(tournament.eventMatches).reduce((sum, matches) => {
            return sum + matches.filter(match =>
                match.completed && match.winner?.id === player.id
            ).length;
        }, 0);
        return total + wins;
    }, 0) || 0;

    const winRate = totalMatches > 0 ? ((totalWins / totalMatches) * 100).toFixed(1) : 0;

    return (
        <div className={styles.pageContainer}>
            {/* Player Header */}
            <div className={styles.playerHeader}>
                <div className={styles.playerAvatar}>
                    {player.name?.charAt(0).toUpperCase() || player.username?.charAt(0).toUpperCase() || 'P'}
                </div>
                <div className={styles.playerInfo}>
                    <h1 className={styles.playerName}>{player.name || player.username}</h1>
                    <p className={styles.playerUsername}>@{player.username}</p>
                </div>
            </div>

            {/* Statistics Cards */}
            <div className={styles.statsGrid}>
                <div className={styles.statCard}>
                    <div className={styles.statValue}>{totalTournaments}</div>
                    <div className={styles.statLabel}>Tournaments</div>
                </div>
                <div className={styles.statCard}>
                    <div className={styles.statValue}>{totalMatches}</div>
                    <div className={styles.statLabel}>Total Matches</div>
                </div>
                <div className={styles.statCard}>
                    <div className={styles.statValue}>{totalWins}</div>
                    <div className={styles.statLabel}>Wins</div>
                </div>
                <div className={styles.statCard}>
                    <div className={styles.statValue}>{winRate}%</div>
                    <div className={styles.statLabel}>Win Rate</div>
                </div>
            </div>

            {/* Tab Navigation */}
            <div className={styles.tabNavigation}>
                <button
                    className={`${styles.tabButton} ${activeTab === 'overview' ? styles.active : ''}`}
                    onClick={() => setActiveTab('overview')}
                >
                    Overview
                </button>
                <button
                    className={`${styles.tabButton} ${activeTab === 'tournaments' ? styles.active : ''}`}
                    onClick={() => setActiveTab('tournaments')}
                >
                    Tournaments
                </button>
                <button
                    className={`${styles.tabButton} ${activeTab === 'matches' ? styles.active : ''}`}
                    onClick={() => setActiveTab('matches')}
                >
                    Match History
                </button>
            </div>

            {/* Content Area */}
            <div className={styles.contentArea}>
                {activeTab === 'overview' && (
                    <div className={styles.overviewContent}>
                        <h2 className={styles.sectionTitle}>Recent Activity</h2>
                        {player.tournaments && player.tournaments.length > 0 ? (
                            <div className={styles.recentTournaments}>
                                {player.tournaments.slice(0, 3).map((tournament) => (
                                    <Link
                                        key={tournament.id}
                                        to={`/tournament/${tournament.id}`}
                                        className={styles.tournamentCard}
                                    >
                                        <h3>{tournament.name}</h3>
                                        <div className={styles.tournamentDetails}>
                                            <span>{tournament.location}</span>
                                            <span>{new Date(tournament.begin).toLocaleDateString()}</span>
                                        </div>
                                    </Link>
                                ))}
                            </div>
                        ) : (
                            <p className={styles.emptyState}>No tournament activity yet.</p>
                        )}
                    </div>
                )}

                {activeTab === 'tournaments' && (
                    <div className={styles.tournamentsContent}>
                        <h2 className={styles.sectionTitle}>All Tournaments</h2>
                        {player.tournaments && player.tournaments.length > 0 ? (
                            <div className={styles.tournamentsList}>
                                {player.tournaments.map((tournament) => (
                                    <div key={tournament.id} className={styles.tournamentItem}>
                                        <div className={styles.tournamentHeader}>
                                            <Link
                                                to={`/tournament/${tournament.id}`}
                                                className={styles.tournamentName}
                                            >
                                                {tournament.name}
                                            </Link>
                                            <span className={styles.tournamentDate}>
                                                {new Date(tournament.begin).toLocaleDateString()} - {new Date(tournament.end).toLocaleDateString()}
                                            </span>
                                        </div>
                                        <div className={styles.tournamentMeta}>
                                            <span>{tournament.location}</span>
                                            {tournament.eventMatches && (
                                                <span>
                                                    {Object.keys(tournament.eventMatches).length} event(s)
                                                </span>
                                            )}
                                        </div>
                                        {tournament.message && (
                                            <p className={styles.tournamentMessage}>{tournament.message}</p>
                                        )}
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <p className={styles.emptyState}>No tournaments found.</p>
                        )}
                    </div>
                )}

                {activeTab === 'matches' && (
                    <div className={styles.matchesContent}>
                        <h2 className={styles.sectionTitle}>Match History</h2>
                        {player.tournaments && player.tournaments.length > 0 ? (
                            <div className={styles.matchesList}>
                                {player.tournaments.map((tournament) => {
                                    if (!tournament.eventMatches) return null;

                                    // Convert eventMatches object to array of [event, matches] pairs
                                    const eventMatchesArray = Object.entries(tournament.eventMatches);

                                    return eventMatchesArray.map(([eventKey, matches], eventIdx) => {
                                        // The eventKey is actually an object (EventDTO), not a string
                                        // Try to parse it, or use it directly if it's already an object
                                        let event;
                                        try {
                                            event = typeof eventKey === 'string' ? JSON.parse(eventKey) : eventKey;
                                        } catch (e) {
                                            // If parsing fails, eventKey might already be an object
                                            event = eventKey;
                                        }

                                        return matches.map((match, matchIdx) => (
                                            <div key={`${tournament.id}-${eventIdx}-${matchIdx}`} className={styles.matchItem}>
                                                <div className={styles.matchHeader}>
                                                    <Link
                                                        to={`/tournament/${tournament.id}`}
                                                        className={styles.matchTournament}
                                                    >
                                                        {tournament.name}
                                                    </Link>
                                                    <span className={styles.matchEvent}>
                                                        Event: {event?.name || 'Unknown Event'}
                                                    </span>
                                                </div>
                                                <div className={styles.matchDetails}>
                                                    <div className={styles.matchPlayers}>
                                                        <span className={match.playerA?.id === player.id ? styles.currentPlayer : ''}>
                                                            {match.playerA?.name || match.playerA?.username || 'TBD'}
                                                        </span>
                                                        <span className={styles.vs}>vs</span>
                                                        <span className={match.playerB?.id === player.id ? styles.currentPlayer : ''}>
                                                            {match.playerB?.name || match.playerB?.username || 'TBD'}
                                                        </span>
                                                    </div>
                                                    {match.completed ? (
                                                        <div className={styles.matchResult}>
                                                            {match.winner?.id === player.id ? (
                                                                <span className={styles.win}>W</span>
                                                            ) : (
                                                                <span className={styles.loss}>L</span>
                                                            )}
                                                            {match.score && match.score.length === 2 && (
                                                                <span className={styles.score}>
                                                                    {match.score[0]} - {match.score[1]}
                                                                </span>
                                                            )}
                                                        </div>
                                                    ) : (
                                                        <span className={styles.pending}>Pending</span>
                                                    )}
                                                </div>
                                            </div>
                                        ));
                                    });
                                })}
                            </div>
                        ) : (
                            <p className={styles.emptyState}>No matches found.</p>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}

export default PlayerPage;
