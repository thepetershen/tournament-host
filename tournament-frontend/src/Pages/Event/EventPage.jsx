import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import SingleElimBracket from '../../Components/SingleElimEvent/SingleElimBracket';
import authAxios from '../../utils/authAxios';

function EventPage() {
    const { tournamentId, eventIndex } = useParams();
    const [draw, setDraw] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchDraw = async () => {
            try {
                const response = await authAxios.get(`/api/tournaments/${tournamentId}/event/${eventIndex}/draw`);
                setDraw(response.data);
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

    return (
        <div style={{ maxWidth: 900, margin: "40px auto", padding: "24px" }}>
            <SingleElimBracket draw={draw} />
        </div>
    );
}

export default EventPage;