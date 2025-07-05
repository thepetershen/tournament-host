import SingleElimEventMatch from "./SingleElimEventMatch";
import styles from "./SingleElimBracketStyle.module.css";
import React, { useRef, useEffect, useState, createContext } from "react";

export const SpacingContext = createContext();

const MATCH_HEIGHT =50;
const SPACER_HEIGHT = 10;
const TRUE_MATCH_HEIGHT = MATCH_HEIGHT + 13; //to account for padding and spacing

function SingleElimBracket ({draw}) {
    
    const spacing = calculatePadding(TRUE_MATCH_HEIGHT, SPACER_HEIGHT, draw.length);
    const spacingLines = calculatePaddingLines(TRUE_MATCH_HEIGHT, SPACER_HEIGHT, draw.length);
    const drawInXML = createAllRounds(draw, spacing, spacingLines);

    return (
        <div className={styles.main}>
            {drawInXML}
        </div>
    )
}


function createMatchesForRound(roundDraw, isFirstRound,curSpacing) {
    const toDisplay = [];

    // Add spacer at the beginning
    toDisplay.push(<div key="spacer-start" className={styles.gameSpacer} style = {{height: (curSpacing/2)+"px"}} ></div>);

    // Add matches and spacers between them
    roundDraw.forEach((match, index) => {
        toDisplay.push(
            <SpacingContext.Provider value = {MATCH_HEIGHT}>
                <SingleElimEventMatch
                    key={`match-${index}`}
                    playerTop={match.player1}
                    playerBottom={match.player2}
                    winner={match.winner}
                    arrOfScore={match.score}
                    nextMatch={roundDraw.length !== 1}
                    prevMatch={!isFirstRound}
                />
            </SpacingContext.Provider>
            
        );

        if (index < roundDraw.length - 1) {
            toDisplay.push(
                <div key={`spacer-${index}`} className={styles.gameSpacer} style = {{height: (curSpacing)+"px"}}></div>
            );
        }
    });

    // Add spacer at the end
    toDisplay.push(<div key="spacer-end" className={styles.gameSpacer} style = {{height: (curSpacing/2)+"px"}} ></div>);

    return toDisplay;
}

function createConnectingLinesForMatches(num, spacing){
    console.log(spacing)
    const answer = []
    answer.push(<div className={styles.connectingLines} style = {{height: (spacing/2)+"px"}}></div>)
    for(let i = 0; i < num-1; i++){
        answer.push(<div className={styles.connectingLines} style = {{height: (spacing)+"px"}}></div>)
    }
    answer.push(<div className={styles.connectingLines} style = {{height: (spacing/2)+"px"}}></div>)
    return answer
}

function createAllRounds (draw, spacing, spacingLines) {
    const drawInXML = draw.map((round, index) => (
        <div className={styles.roundContainer}>
            <div className = {styles.round} key = {"round" + index}>
                {createMatchesForRound(round, (index === 0),spacing[index])}
            </div>
            {index < draw.length - 1 && (
                <div className={styles.connectingLinesBlock}>
                    {createConnectingLinesForMatches(round.length, spacingLines[index])}
                </div>
            )}
        </div>
        
    )
    );
    return drawInXML
}
/*
    returns a array representing in order of the padding between matches. The initially padding is half of the returned padding
*/
function calculatePadding(TRUE_MATCH_HEIGHT, SPACER_HEIGHT, drawLength) {
    const answer = [];
    answer.push(SPACER_HEIGHT);
    for (let i = 1; i < drawLength; i++) {
        answer.push((TRUE_MATCH_HEIGHT * (Math.pow(2, i) - 1)) + (SPACER_HEIGHT * (Math.pow(2, i))));
    }
    return answer;
}

function calculatePaddingLines(TRUE_MATCH_HEIGHT, SPACER_HEIGHT, drawLength) {
    const answer = [];
    for (let i = 0; i < drawLength-1; i++) {
        answer.push((TRUE_MATCH_HEIGHT * (Math.pow(2, i))) + (SPACER_HEIGHT * (Math.pow(2, i))));
    }
    return answer;
}

export default SingleElimBracket;