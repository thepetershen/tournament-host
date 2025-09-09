import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import SingleElimBracket from '../../Components/SingleElimEvent/SingleElimBracket';
import axios from 'axios';

function EventPage() {
    const { eventId } = useParams();
    const [draw, setDraw] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Create an axios instance with Authorization header from localStorage
    const token = localStorage.getItem('token');
    const authAxios = axios.create({
        baseURL: 'http://localhost:8080',
        headers: token ? { Authorization: `Bearer ${token}` } : {},
    });

    useEffect(() => {
        const fetchDraw = async () => {
            try {
                const response = await authAxios.get(`/api/events/${eventId}/draw`);
                setDraw(response.data);
            } catch (err) {
                setError('Failed to load event draw.');
            } finally {
                setLoading(false);
            }
        };
        if (eventId) fetchDraw();
        // eslint-disable-next-line react-hooks/exhaustive-deps
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