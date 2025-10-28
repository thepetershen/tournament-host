import styles from "./SingleElimEventMatchStyle.module.css";
import {useContext} from "react";
import {SpacingContext} from './SingleElimBracket.jsx';


function SingleElimEventMatch({ playerTop = "BYE", playerBottom = "BYE", winner, arrOfScore = [], nextMatch, prevMatch, matchId, isCompleted}) {


    const MATCH_SPACING = useContext(SpacingContext);

    // Determine if players are winners or losers
    const isTopWinner = winner && winner === playerTop;
    const isBottomWinner = winner && winner === playerBottom;
    const isTopLoser = winner && winner !== playerTop && playerTop !== "BYE";
    const isBottomLoser = winner && winner !== playerBottom && playerBottom !== "BYE";

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

             <div className={styles.container} style = {{height: MATCH_SPACING+"px"}}>
                <div
                    className={styles.matchTop}
                    style={{
                        fontWeight: isTopWinner ? "bold" : "normal",
                        color: isTopWinner ? "#28a745" : (isTopLoser ? "#dc3545" : "#333"),
                        backgroundColor: isTopWinner ? "rgba(40, 167, 69, 0.1)" : (isTopLoser ? "rgba(220, 53, 69, 0.05)" : "transparent"),
                        transition: "all 0.3s ease"
                    }}
                >
                    {playerTop}
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
                    {playerBottom}
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