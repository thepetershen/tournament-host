import styles from "./SingleElimEventMatchStyle.module.css";
import React, {useContext} from "react";
import {SpacingContext} from './SingleElimBracket.jsx';


function SingleElimEventMatch({ playerTop = "BYE", playerBottom = "BYE", winner, arrOfScore = [], nextMatch, prevMatch}) {


    const MATCH_SPACING = useContext(SpacingContext);
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
                    style={winner === playerTop ? { fontWeight: "bold" } : {}}
                >
                    {playerTop}
                </div>
                <div
                    className={styles.matchBottom}
                    style={winner === playerBottom ? { fontWeight: "bold" } : {}}
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