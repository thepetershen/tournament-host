import styles from "./SingleElimEventMatchStyle.module.css";
import {useContext} from "react";
import {SpacingContext} from './SingleElimBracket.jsx';
import { Link } from 'react-router-dom';


function SingleElimEventMatch({ playerTop = "BYE", playerBottom = "BYE", winner, arrOfScore = [], nextMatch, prevMatch, matchId, isCompleted, matchType = "SINGLES"}) {


    const MATCH_SPACING = useContext(SpacingContext);

    // Helper function to format team/player names
    const formatTeamName = (team) => {
        if (!team || team === "BYE") return "BYE";
        if (typeof team === 'string') return team;
        if (team.teamName) return team.teamName; // Use pre-formatted team name from backend
        if (team.player1 && team.player2) {
            return `${team.player1.name || team.player1.username} / ${team.player2.name || team.player2.username}`;
        }
        if (team.player1) return team.player1.name || team.player1.username;
        if (team.username) return team.name || team.username; // Fallback for User objects
        return "TBD";
    };

    // Helper function to render team/player names with links
    const renderPlayerName = (team, styles = {}) => {
        if (!team || team === "BYE") return "BYE";
        if (typeof team === 'string') return team;

        // Handle team (doubles)
        if (team.player1 && team.player2) {
            return (
                <>
                    <Link to={`/player/${team.player1.id}`} style={{ color: 'inherit', textDecoration: 'none' }}>
                        {team.player1.name || team.player1.username}
                    </Link>
                    {' / '}
                    <Link to={`/player/${team.player2.id}`} style={{ color: 'inherit', textDecoration: 'none' }}>
                        {team.player2.name || team.player2.username}
                    </Link>
                </>
            );
        }

        // Handle single player
        if (team.player1) {
            return (
                <Link to={`/player/${team.player1.id}`} style={{ color: 'inherit', textDecoration: 'none' }}>
                    {team.player1.name || team.player1.username}
                </Link>
            );
        }

        // Handle user object directly
        if (team.id && team.username) {
            return (
                <Link to={`/player/${team.id}`} style={{ color: 'inherit', textDecoration: 'none' }}>
                    {team.name || team.username}
                </Link>
            );
        }

        return "TBD";
    };

    const topDisplayName = formatTeamName(playerTop);
    const bottomDisplayName = formatTeamName(playerBottom);
    const winnerDisplayName = formatTeamName(winner);

    // Determine if players are winners or losers
    const isTopWinner = winner && winnerDisplayName === topDisplayName;
    const isBottomWinner = winner && winnerDisplayName === bottomDisplayName;
    const isTopLoser = winner && winnerDisplayName !== topDisplayName && topDisplayName !== "BYE";
    const isBottomLoser = winner && winnerDisplayName !== bottomDisplayName && bottomDisplayName !== "BYE";

    // Convert flat array [6, 4, 7, 5] to pairs [[6, 4], [7, 5]]
    const scorePairs = [];
    if (arrOfScore && arrOfScore.length > 0) {
        for (let i = 0; i < arrOfScore.length; i += 2) {
            if (i + 1 < arrOfScore.length) {
                scorePairs.push([arrOfScore[i], arrOfScore[i + 1]]);
            }
        }
    }

    const scoreListTop = scorePairs.map((score, index) =>
        <div className = {styles.scoreTop}
            key = {playerTop+playerBottom+index}
            style={{
                ...(score[0] > score[1] ? { fontWeight: "bold" } : {}),
                paddingRight: "5px"
            }}>
            {score[0]}
        </div>

    )

    const scoreListBottom = scorePairs.map((score, index) =>
        <div className = {styles.scoreBottom}
            key = {playerBottom+playerTop+index}
            style={{
                ...(score[1] > score[0] ? { fontWeight: "bold" } : {}),
                paddingRight: "5px"
            }}>
            {score[1]}
        </div>

    )

    return (
        <div style = {{display: "flex", alignItems: "center"}}>

            {prevMatch && (
                <div className={styles.matchRightConnector}></div>
            )}

             <div className={styles.container} style = {{height: MATCH_SPACING+"px", width: matchType === "DOUBLES" ? "380px" : "300px"}}>
                <div
                    className={styles.matchTop}
                    style={{
                        fontWeight: isTopWinner ? "bold" : "normal",
                        color: isTopWinner ? "#28a745" : (isTopLoser ? "#dc3545" : "#333"),
                        backgroundColor: isTopWinner ? "rgba(40, 167, 69, 0.1)" : (isTopLoser ? "rgba(220, 53, 69, 0.05)" : "transparent"),
                        transition: "all 0.3s ease"
                    }}
                >
                    {renderPlayerName(playerTop)}
                </div>
                <div
                    className={styles.matchBottom}
                    style={{
                        fontWeight: isBottomWinner ? "bold" : "normal",
                        color: isBottomWinner ? "#28a745" : (isBottomLoser ? "#dc3545" : "#333"),
                        backgroundColor: isBottomWinner ? "rgba(40, 167, 69, 0.1)" : (isBottomLoser ? "rgba(220, 53, 69, 0.05)" : "transparent"),
                        transition: "all 0.3s ease"
                    }}
                >
                    {renderPlayerName(playerBottom)}
                </div>
                <div className={styles.scoreTopContainer}>
                    {scoreListTop}
                </div>
                <div className={styles.scoreBottomContainer}>
                    {scoreListBottom}
                </div>
            </div>

            {nextMatch && (
                <div className={styles.matchRightConnector}></div>
            )}
        </div>

    );
}

export default SingleElimEventMatch;