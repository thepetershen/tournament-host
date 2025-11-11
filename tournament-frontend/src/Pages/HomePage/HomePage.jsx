import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import publicAxios from '../../utils/publicAxios';
import PlayerLink from '../../Components/PlayerLink/PlayerLink';
import styles from './HomePage.module.css';

function HomePage() {
    const [tournaments, setTournaments] = useState([]);
    const [leagues, setLeagues] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [tournamentsRes, leaguesRes] = await Promise.all([
                publicAxios.get('/api/tournaments?limit=20'),
                publicAxios.get('/api/leagues')
            ]);
            setTournaments(tournamentsRes.data);
            setLeagues(leaguesRes.data);
        } catch (error) {
            console.error('Error fetching data:', error);
        } finally {
            setLoading(false);
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'TBA';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    };

    const formatDateRange = (begin, end) => {
        if (!begin && !end) return 'Dates TBA';
        if (!begin) return `Until ${formatDate(end)}`;
        if (!end) return `From ${formatDate(begin)}`;

        const beginDate = formatDate(begin);
        const endDate = formatDate(end);

        if (beginDate === endDate) return beginDate;
        return `${beginDate} - ${endDate}`;
    };

    if (loading) {
        return (
            <div className={styles.homePage}>
                <div className={styles.loading}>Loading...</div>
            </div>
        );
    }

    return (
        <div className={styles.homePage}>
            {/* Tournaments Section */}
            <div className={styles.section}>
                <div className={styles.sectionHeader}>
                    <h2 className={styles.sectionTitle}>Tournaments</h2>
                    <div className={styles.headerActions}>
                        <Link to="/create-tournament" className={styles.createButton}>
                            Create Tournament
                        </Link>
                        <Link to="/tournaments" className={styles.viewAllLink}>
                            View All →
                        </Link>
                    </div>
                </div>

                {tournaments.length === 0 ? (
                    <div className={styles.emptyState}>No tournaments available</div>
                ) : (
                    <div className={styles.tournamentGrid}>
                        {tournaments.map((tournament) => (
                            <div key={tournament.id} className={styles.tournamentCard}>
                                <div className={`${styles.statusBadge} ${styles[tournament.status?.toLowerCase()]}`}>
                                    {tournament.status || 'UNKNOWN'}
                                </div>

                                <Link to={`/tournament/${tournament.id}`} className={styles.tournamentName}>
                                    {tournament.name}
                                </Link>

                                <div className={styles.tournamentMeta}>
                                    <div className={styles.metaItem}>
                                        <span className={styles.metaIcon}>📅</span>
                                        <span className={styles.metaText}>
                                            {formatDateRange(tournament.begin, tournament.end)}
                                        </span>
                                    </div>

                                    {tournament.location && (
                                        <div className={styles.metaItem}>
                                            <span className={styles.metaIcon}>📍</span>
                                            <span className={styles.metaText}>{tournament.location}</span>
                                        </div>
                                    )}

                                    <div className={styles.stats}>
                                        <div className={styles.statItem}>
                                            <span className={styles.statValue}>{tournament.eventCount || 0}</span>
                                            <span className={styles.statLabel}>Events</span>
                                        </div>
                                        <div className={styles.statItem}>
                                            <span className={styles.statValue}>{tournament.participantCount || 0}</span>
                                            <span className={styles.statLabel}>Players</span>
                                        </div>
                                    </div>

                                    {tournament.owner && (
                                        <div className={styles.organizer}>
                                            Organized by{' '}
                                            <span className={styles.organizerName}>
                                                <PlayerLink player={tournament.owner} />
                                            </span>
                                        </div>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Leagues Section */}
            <div className={styles.section}>
                <div className={styles.sectionHeader}>
                    <h2 className={styles.sectionTitle}>Leagues</h2>
                    <Link to="/leagues" className={styles.viewAllLink}>
                        View All →
                    </Link>
                </div>

                {leagues.length === 0 ? (
                    <div className={styles.emptyState}>No leagues available</div>
                ) : (
                    <div className={styles.leagueGrid}>
                        {leagues.map((league) => (
                            <Link
                                key={league.id}
                                to={`/league/${league.id}`}
                                className={styles.leagueCard}
                            >
                                <h3 className={styles.leagueName}>{league.name}</h3>
                                {league.description && (
                                    <p className={styles.leagueDescription}>{league.description}</p>
                                )}
                                <div className={styles.leagueStats}>
                                    <span>{league.tournaments?.length || 0} Tournaments</span>
                                </div>
                            </Link>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

export default HomePage;
