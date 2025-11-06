import { Link } from "react-router-dom";
import React, {useEffect, useState} from "react";
import styles from "./HomePageContent.module.css";
import authAxios from "../../utils/authAxios";

function HomePageContent() {
    const [tournaments, setTournaments] = useState([]);

    useEffect(() => {
        fetchTournaments();
    }, []);

    const fetchTournaments = async () => {
        try {
            const response = await authAxios.get("/api/tournaments?limit=20");
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

    return (
        <div className={styles.pageContainer}>
            <div className={styles.header}>
                <h1 className={styles.heading}>Tournaments</h1>
                <Link to="/tournaments" className={styles.viewAllButton}>
                    View All
                </Link>
            </div>

            {(!tournaments || tournaments.length === 0) ? (
                <div className={styles.empty}>No tournaments available.</div>
            ) : (
                <div className={styles.tournamentGrid}>
                    {tournaments.map(tournament => (
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
                                        Organized by <span className={styles.organizerName}>{tournament.owner.name || tournament.owner.username}</span>
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

export default HomePageContent;