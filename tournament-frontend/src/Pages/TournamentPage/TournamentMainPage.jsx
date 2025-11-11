import React, { useEffect, useState } from "react";
import styles from "./TournamentMainPage.module.css";
import publicAxios from "../../utils/publicAxios";
import { Link } from "react-router-dom";
import PlayerLink from "../../Components/PlayerLink/PlayerLink";

function TournamentPage() {
    const [tournaments, setTournaments] = useState([]);
    const [filter, setFilter] = useState('ALL');

    useEffect(() => {
        fetchTournaments();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const fetchTournaments = async () => {
        try {
            const response = await publicAxios.get("/api/tournaments?limit=20");
            setTournaments(response.data)
        } catch (error) {
            console.error('Error fetching tournaments:', error);
        }
    }

    const formatDate = (dateString) => {
        if (!dateString) return 'TBA';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    }

    const formatDateRange = (begin, end) => {
        if (!begin && !end) return 'Dates TBA';
        if (!begin) return `Until ${formatDate(end)}`;
        if (!end) return `From ${formatDate(begin)}`;

        const beginDate = formatDate(begin);
        const endDate = formatDate(end);

        if (beginDate === endDate) return beginDate;
        return `${beginDate} - ${endDate}`;
    }

    const filteredTournaments = tournaments.filter(tournament => {
        if (filter === 'ALL') return true;
        return tournament.status === filter;
    });

    return (
        <div className={styles.pageContainer}>
            <div className={styles.header}>
                <h2 className={styles.heading}>Tournaments</h2>
                <div className={styles.filters}>
                    <button
                        className={filter === 'ALL' ? styles.activeFilter : ''}
                        onClick={() => setFilter('ALL')}
                    >
                        All
                    </button>
                    <button
                        className={filter === 'ONGOING' ? styles.activeFilter : ''}
                        onClick={() => setFilter('ONGOING')}
                    >
                        Ongoing
                    </button>
                    <button
                        className={filter === 'UPCOMING' ? styles.activeFilter : ''}
                        onClick={() => setFilter('UPCOMING')}
                    >
                        Upcoming
                    </button>
                    <button
                        className={filter === 'COMPLETED' ? styles.activeFilter : ''}
                        onClick={() => setFilter('COMPLETED')}
                    >
                        Past
                    </button>
                </div>
            </div>

            {filteredTournaments.length === 0 ? (
                <div className={styles.emptyState}>
                    <p>No tournaments found</p>
                </div>
            ) : (
                <div className={styles.tournamentGrid}>
                    {filteredTournaments.map((tournament) => (
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
                                    <span className={styles.metaText}>{formatDateRange(tournament.begin, tournament.end)}</span>
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
                                        Organized by <span className={styles.organizerName}>
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
    );

}

export default TournamentPage;