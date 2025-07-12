import { useEffect , useState} from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import SingleElimBracket from "../../Components/SingleElimEvent/SingleElimBracket";

function TournamentIndividualPage () {
    const params = useParams();
    const tournamentRequestedId = Number(params.tournamentId);

    const [tournamentDraw, setTournamentDraw] = useState([]);
    const [tournamentName, setTournamentName] = useState("");

    useEffect(() => {
        if (!isNaN(tournamentRequestedId)) {
            fetchDraw();
            fetchName();
        }
    }, [tournamentRequestedId]);

    const fetchDraw = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/api/tournaments/${tournamentRequestedId}/draw`);
            setTournamentDraw(response.data);
        } catch (error) {
            console.error('Error fetching tournament draw:', error);
        }
    }

    const fetchName = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/api/tournaments/${tournamentRequestedId}/name`);
            setTournamentName(response.data.name);
        } catch (error) {
            console.error('Error fetching tournament name:', error);
        }
    }

    return (
        <div>
            <h2>{tournamentName}</h2>
            <SingleElimBracket draw={tournamentDraw}/>
        </div>
    );
}

export default TournamentIndividualPage;