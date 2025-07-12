import React, { useState } from 'react';
import axios from 'axios';

const TournamentControl = () => {
    const [name, setName] = useState('');
    const [players, setPlayers] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setSuccess('');

        try {
            // 1. Create tournament
            const tournamentRes = await axios.post('http://localhost:8080/api/tournaments', { name });
            const tournamentId = tournamentRes.data.id;

            // 2. Add players (batch)
            const playerList = players.split(',').map(p => p.trim()).filter(p => p.length > 0);
            const playerRequests = playerList.map(name => ({ name }));

            await axios.post(`http://localhost:8080/api/tournaments/${tournamentId}/players/batch`, playerRequests);
            await axios.post(`http://localhost:8080/api/tournaments/${tournamentId}/initialize`)

            setSuccess('Tournament created successfully!');
            setName('');
            setPlayers('');
        } catch (err) {
            setError('Failed to create tournament.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ maxWidth: 400, margin: 'auto', padding: 20 }}>
            <h2>Create Tournament</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Tournament Name:</label>
                    <input
                        type="text"
                        value={name}
                        onChange={e => setName(e.target.value)}
                        required
                        style={{ width: '100%', marginBottom: 10 }}
                    />
                </div>
                <div>
                    <label>Players (comma separated):</label>
                    <textarea
                        value={players}
                        onChange={e => setPlayers(e.target.value)}
                        rows={4}
                        style={{ width: '100%', marginBottom: 10 }}
                        placeholder="e.g. Alice, Bob, Charlie"
                        required
                    />
                </div>
                <button type="submit" disabled={loading}>
                    {loading ? 'Creating...' : 'Create Tournament'}
                </button>
            </form>
            {error && <div style={{ color: 'red', marginTop: 10 }}>{error}</div>}
            {success && <div style={{ color: 'green', marginTop: 10 }}>{success}</div>}
        </div>
    );
};

export default TournamentControl;
