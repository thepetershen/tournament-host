import styles from "./SingleElimEventMatchStyle.module.css";
import {useContext} from "react";
import {SpacingContext} from './SingleElimBracket.jsx';


function SingleElimEventMatch({ playerTop = "BYE", playerBottom = "BYE", winner, arrOfScore = [], nextMatch, prevMatch, highlightLoser = false}) {


    const MATCH_SPACING = useContext(SpacingContext);

    // Determine if players are winners or losers
    const isTopWinner = winner && winner === playerTop;
    const isBottomWinner = winner && winner === playerBottom;
    const isTopLoser = winner && winner !== playerTop && playerTop !== "BYE";
    const isBottomLoser = winner && winner !== playerBottom && playerBottom !== "BYE";

    const scoreListTop = arrOfScore.map((score, index) =>
        <div className = {styles.scoreTop}
            key = {playerTop+playerBottom+index}
            style={{
                ...(score[0] > score[1] ? { fontWeight: "bold" } : {}),
                paddingRight: "5px"
            }}>
            {score[0]}
        </div>

    )

    const scoreListBottom = arrOfScore.map((score, index) =>
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
                        color: highlightLoser && isTopLoser ? "#dc3545" : (isTopLoser ? "#999" : "#333")
                    }}
                >
                    {playerTop}
                </div>
                <div
                    className={styles.matchBottom}
                    style={{
                        fontWeight: isBottomWinner ? "bold" : "normal",
                        color: highlightLoser && isBottomLoser ? "#dc3545" : (isBottomLoser ? "#999" : "#333")
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