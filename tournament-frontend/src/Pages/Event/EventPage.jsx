import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import SingleElimBracket from '../../Components/SingleElimEvent/SingleElimBracket';
import RoundRobinBracket from '../../Components/RoundRobinEvent/RoundRobinBracket';
import DoubleElimBracket from '../../Components/DoubleElimEvent/DoubleElimBracket';
import authAxios from '../../utils/authAxios';
import styles from './EventPage.module.css';

function EventPage() {
    const { tournamentId, eventIndex } = useParams();
    const navigate = useNavigate();
    const [eventType, setEventType] = useState(null);
    const [draw, setDraw] = useState(null);
    const [tournament, setTournament] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentUser, setCurrentUser] = useState(null);
    const [isAuthorized, setIsAuthorized] = useState(false);

    useEffect(() => {
        const fetchData = async () => {
            try {
                // Fetch current user if authenticated
                const token = localStorage.getItem('token');
                if (token) {
                    try {
                        const userResponse = await authAxios.get('/api/users/me');
                        setCurrentUser(userResponse.data);
                    } catch (err) {
                        console.error(err);
                    }
                }

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

    // Check if current user is authorized (owner or editor)
    useEffect(() => {
        if (currentUser && tournament) {
            const isOwner = tournament.owner && tournament.owner.id === currentUser.id;
            const isEditor = tournament.authorizedEditors &&
                tournament.authorizedEditors.some(editor => editor.id === currentUser.id);
            setIsAuthorized(isOwner || isEditor);
        } else {
            setIsAuthorized(false);
        }
    }, [currentUser, tournament]);

    const handleSignUpClick = () => {
        const token = localStorage.getItem('token');
        if (!token) {
            navigate('/register');
            return;
        }

        // Verify token is valid by making a quick API call
        authAxios.get('/api/users/me')
            .then(() => {
                navigate(`/tournament/${tournamentId}/signup`);
            })
            .catch(() => {
                // Token is invalid or expired
                localStorage.removeItem('token');
                navigate('/register');
            });
    };

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
                <div>
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
                <div className={styles.headerButtons}>
                    {isAuthorized && (
                        <button
                            onClick={() => navigate('/tournamentControl')}
                            className={styles.controlButton}
                        >
                            Control Panel
                        </button>
                    )}
                    <button
                        onClick={handleSignUpClick}
                        className={styles.signUpButton}
                    >
                        Sign Up
                    </button>
                </div>
            </div>

            {/* Bracket Content Area */}
            <div className={styles.bracketContainer}>
                {renderBracket()}
            </div>
        </div>
    );
}

export default EventPage;