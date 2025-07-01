import styles from "./SingleElimEventMatchStyle.module.css";

function SingleElimEventMatch({ playerTop = "BYE", playerBottom = "BYE", winner, arrOfScore }) {

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
        <div className={styles.container}>
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
    );
}

export default SingleElimEventMatch;