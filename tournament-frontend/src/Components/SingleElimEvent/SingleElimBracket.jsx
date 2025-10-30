import SingleElimEventMatch from "./SingleElimEventMatch";
import styles from "./SingleElimBracketStyle.module.css";
import React, { useRef, useEffect, useState, createContext } from "react";

export const SpacingContext = createContext();

const MATCH_HEIGHT =50;
const SPACER_HEIGHT = 10;
const TRUE_MATCH_HEIGHT = MATCH_HEIGHT + 13; //to account for padding and spacing

function SingleElimBracket ({draw}) {
    
    const spacing = calculatePadding(TRUE_MATCH_HEIGHT, SPACER_HEIGHT, draw);
    const spacingLines = calculatePaddingLines(TRUE_MATCH_HEIGHT, SPACER_HEIGHT, draw);
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
        // Handle new MatchDTO structure
        const playerAName = match.playerA?.username || match.playerA?.name || 'TBD';
        const playerBName = match.playerB?.username || match.playerB?.name || 'TBD';
        const winnerName = match.winner?.username || match.winner?.name || null;

        toDisplay.push(
            <SpacingContext.Provider value = {MATCH_HEIGHT} key={`match-${index}`}>
                <SingleElimEventMatch
                    playerTop={playerAName}
                    playerBottom={playerBName}
                    winner={winnerName}
                    arrOfScore={match.score || []}
                    nextMatch={roundDraw.length !== 1}
                    prevMatch={!isFirstRound}
                    matchId={match.id}
                    isCompleted={match.completed}
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
function calculatePadding(TRUE_MATCH_HEIGHT, SPACER_HEIGHT, draw) {
    const answer = [];
    answer.push(SPACER_HEIGHT);
    var actualHeight = 0;
    for (let i = 1; i < draw.length; i++) {
        if (draw[i].length != draw[i-1].length) {
            actualHeight++;
        }
        answer.push((TRUE_MATCH_HEIGHT * (Math.pow(2, actualHeight) - 1)) + (SPACER_HEIGHT * (Math.pow(2, actualHeight))));
    }
    return answer;
}

function calculatePaddingLines(TRUE_MATCH_HEIGHT, SPACER_HEIGHT, draw) {
    const answer = [];
    var actualHeight = 0;
    for (let i = 0; i < draw.length - 1; i++) {
        if (i > 0 && draw[i].length != draw[i-1].length) {
            actualHeight++;
        }
        answer.push((TRUE_MATCH_HEIGHT * (Math.pow(2, actualHeight))) + (SPACER_HEIGHT * (Math.pow(2, actualHeight))));
    }
    return answer;
}

export default SingleElimBracket;