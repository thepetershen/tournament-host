import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import SingleElimBracket from '../../Components/SingleElimEvent/SingleElimBracket';
import RoundRobinBracket from '../../Components/RoundRobinEvent/RoundRobinBracket';
import DoubleElimBracket from '../../Components/DoubleElimEvent/DoubleElimBracket';
import authAxios from '../../utils/authAxios';
import styles from './EventPage.module.css';

function EventPage() {
    const { tournamentId, eventIndex } = useParams();
    const [eventType, setEventType] = useState(null);
    const [draw, setDraw] = useState(null);
    const [tournament, setTournament] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                // Fetch tournament general info
                const tournamentResponse = await authAxios.get(`/api/tournaments/${tournamentId}`);
                setTournament(tournamentResponse.data);

                // Fetch event draw
                const drawResponse = await authAxios.get(`/api/tournaments/${tournamentId}/event/${eventIndex}/draw`);
                const { eventType, draw } = drawResponse.data;
                setEventType(eventType);
                setDraw(draw);
            } catch (err) {
                setError('Failed to load event data.');
            } finally {
                setLoading(false);
            }
        };
        if (tournamentId && eventIndex !== undefined) fetchData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [tournamentId, eventIndex]);

    if (loading) return <div>Loading bracket...</div>;
    if (error) return <div>{error}</div>;

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
                <h1 className={styles.tournamentTitle}>
                    {tournament ? tournament.name : "Loading..."}
                </h1>
                {tournament && (
                    <div className={styles.tournamentMeta}>
                        {tournament.owner && (
                            <div className={styles.metaItem}>
                                <span className={styles.metaLabel}>Owner:</span>
                                <span>{tournament.owner.username || tournament.owner.name}</span>
                            </div>
                        )}
                        <div className={styles.metaItem}>
                            <span className={styles.metaLabel}>Event:</span>
                            <span>{eventIndex}</span>
                        </div>
                    </div>
                )}
            </div>

            {/* Bracket Content Area */}
            <div className={styles.bracketContainer}>
                {renderBracket()}
            </div>
        </div>
    );
}

export default EventPage;