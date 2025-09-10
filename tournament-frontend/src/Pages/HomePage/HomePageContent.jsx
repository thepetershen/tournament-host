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
            const response = await authAxios.get("/api/tournaments");
            setTournaments(response.data)
        } catch (error) {
            console.error('Error fetching tournaments:', error);
        }
    }

    return (
        <div className={styles.pageContainer}>
        <h1 className={styles.heading}>Current Tournaments</h1>
        {(!tournaments || tournaments.length === 0) ? (
            <div className={styles.empty}>No tournaments available.</div>
        ) : (
            tournaments.map(t => (
            <div key={t.id} className={styles.tournamentCard}>
                <Link to={`/tournament/${t.id}`} className={styles.tournamentName}>
                {t.name}
                </Link>
                <div className={styles.tournamentDetails}>
                <span><strong>Location:</strong> {t.location}</span>
                <span><strong>Host:</strong> {t.host}</span>
                <span><strong>Date:</strong> {t.date}</span>
                </div>
            </div>
            ))
        )}
        </div>
    );


}

export default HomePageContent;
// Already uses /api/tournaments endpoint from tournament controller