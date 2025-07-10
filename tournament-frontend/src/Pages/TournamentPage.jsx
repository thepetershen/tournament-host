import React, { useEffect, useState } from "react";
import SingleElimBracket from "../Components/SingleElimEvent/SingleElimBracket.jsx";

function TournamentPage() {
    const [draw, setDraw] = useState([]);
    const tournamentId = 1; // Change this to your actual tournament ID

    useEffect(() => {
        fetch(`http://localhost:8080/api/tournaments/${tournamentId}/draw`)
            .then(res => {
                if (!res.ok) throw new Error("Failed to fetch draw");
                return res.json();
            })
            .then(data => setDraw(data))
            .catch(err => {
                console.error("Error fetching draw:", err);
                setDraw([]); // fallback or show error
            });
    }, [tournamentId]);

    return (
        <>
            <h1>Tournament {tournamentId}</h1>
            <SingleElimBracket draw={draw} />
        </>
    );
}

export default TournamentPage;