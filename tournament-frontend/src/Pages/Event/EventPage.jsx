import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import SingleElimBracket from '../../Components/SingleElimEvent/SingleElimBracket';
import RoundRobinBracket from '../../Components/RoundRobinEvent/RoundRobinBracket';
import DoubleElimBracket from '../../Components/DoubleElimEvent/DoubleElimBracket';
import authAxios from '../../utils/authAxios';

function EventPage() {
    const { tournamentId, eventIndex } = useParams();
    const [eventType, setEventType] = useState(null);
    const [draw, setDraw] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchDraw = async () => {
            try {
                const response = await authAxios.get(`/api/tournaments/${tournamentId}/event/${eventIndex}/draw`);
                const { eventType, draw } = response.data;
                setEventType(eventType);
                setDraw(draw);
            } catch (err) {
                setError('Failed to load event draw.');
            } finally {
                setLoading(false);
            }
        };
        if (tournamentId && eventIndex !== undefined) fetchDraw();
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
        <div style={{ padding: "24px", width: "100%" }}>
            {renderBracket()}
        </div>
    );
}

export default EventPage;