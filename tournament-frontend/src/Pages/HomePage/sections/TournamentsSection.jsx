import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import publicAxios from '../../../utils/publicAxios';
import PlayerLink from '../../../Components/PlayerLink/PlayerLink';
import styles from './TournamentsSection.module.css';

function TournamentsSection({ isLoggedIn }) {
    const [tournaments, setTournaments] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchTournaments();
    }, []);

    const fetchTournaments = async () => {
        try {
            const response = await publicAxios.get('/api/tournaments?limit=6');
            setTournaments(response.data);
        } catch (error) {
            console.error('Error fetching tournaments:', error);
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

    const heading = isLoggedIn ? 'Browse Tournaments' : 'Featured Tournaments';
    const subtext = isLoggedIn
        ? 'Discover and join tournaments happening now'
        : 'Join thousands of competitors in exciting tournaments';

    if (loading) {
        return (
            <div className={styles.tournamentsContainer}>
                <div className={styles.tournamentsContent}>
                    <h2 className={styles.tournamentsHeading}>{heading}</h2>
                    <p className={styles.loading}>Loading tournaments...</p>
                </div>
            </div>
        );
    }

    if (tournaments.length === 0) {
        return (
            <div className={styles.tournamentsContainer}>
                <div className={styles.tournamentsContent}>
                    <h2 className={styles.tournamentsHeading}>{heading}</h2>
                    <p className={styles.emptyState}>No tournaments available at the moment</p>
                </div>
            </div>
        );
    }

    return (
        <div className={styles.tournamentsContainer}>
            <div className={styles.tournamentsContent}>
                <div className={styles.header}>
                    <div>
                        <h2 className={styles.tournamentsHeading}>{heading}</h2>
                        <p className={styles.tournamentsSubtext}>{subtext}</p>
                    </div>
                    <Link to="/tournaments" className={styles.viewAllButton}>
                        View All →
                    </Link>
                </div>

                <div className={styles.tournamentGrid}>
                    {tournaments.map((tournament) => (
                        <div key={tournament.id} className={styles.tournamentCard}>
                            <div className={`${styles.statusBadge} ${styles[tournament.status?.toLowerCase()]}`}>
                                {tournament.status || 'UNKNOWN'}
                            </div>

                            <Link
                                to={`/tournament/${tournament.id}`}
                                className={styles.tournamentName}
                            >
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
            </div>
        </div>
    );
}

export default TournamentsSection;
