import React, { useEffect, useState } from "react";
import SingleElimBracket from "../../Components/SingleElimEvent/SingleElimBracket.jsx";
import styles from "./TournamentPage.module.css";
import axios from "axios";

function TournamentPage() {
    const [tournaments, setTournaments] = useState([]);

    useEffect(() => {
        fetchTournaments();
    }, []);

    const fetchTournaments = async () => {
        try {
            const response = await axios.get("http://localhost:8080/api/tournaments");
            setTournaments(response.data)
        } catch (error) {
            console.error('Error fetching tournaments:', error);
        }
    }

    return (
        <div className={styles.pageContainer}>
            <h2 className={styles.heading}>Tournaments</h2>
            <div className={styles.tournamentGrid}>
                {tournaments.map((tournament) => (
                    <div key={tournament.id} className={styles.tournamentCard}>
                        <div className={styles.tournamentName}>{tournament.name}</div>
                        <div className={styles.tournamentDetails}>
                            {/* Placeholder for future details (date, players, etc.) */}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );

}

export default TournamentPage;