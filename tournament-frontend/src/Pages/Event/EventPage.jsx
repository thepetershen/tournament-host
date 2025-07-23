import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import SingleElimBracket from '../../Components/SingleElimEvent/SingleElimBracket';
import axios from 'axios';

function EventPage() {
    const { eventId } = useParams(); // assuming your route is /events/:id
    const [draw, setDraw] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchDraw = async () => {
            try {
                const response = await axios.get(`http://localhost:8080/api/events/${eventId}/draw`);
                setDraw(response.data);
            } catch (err) {
                setError('Failed to load event draw.');
            } finally {
                setLoading(false);
            }
        };
        if (eventId) fetchDraw();
    }, [eventId]);

    if (loading) return <div>Loading bracket...</div>;
    if (error) return <div>{error}</div>;

    return (
        <div style={{ maxWidth: 900, margin: "40px auto", padding: "24px" }}>
            <SingleElimBracket draw={draw} />
        </div>
    );
}

export default EventPage;