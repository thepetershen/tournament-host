import SingleElimBracket from "../SingleElimEvent/SingleElimBracket";
import SingleElimEventMatch from "../SingleElimEvent/SingleElimEventMatch";
import styles from "./DoubleElimBracketStyle.module.css";
import React, { act, createContext } from "react";

export const SpacingContext = createContext();

const MATCH_HEIGHT = 50;
const SPACER_HEIGHT = 10;
const TRUE_MATCH_HEIGHT = MATCH_HEIGHT + 13; // to account for padding and spacing

/**
 * DoubleElimBracket component
 *
 * @param {Object} draw - Object with two brackets from backend
 *   draw = {
 *     winners: [[MatchDTO, ...], [MatchDTO, ...], ...],  // Winners bracket rounds
 *     losers: [[MatchDTO, ...], [MatchDTO, ...], ...]    // Losers bracket rounds
 *   }
 *   where MatchDTO = { playerA: {username}, playerB: {username}, winner: {username}, score: [[a,b], ...] }
 */
function DoubleElimBracket({ draw }) {

    if (!draw || !draw.winners || !draw.losers) {
        return (
            <div className={styles.main}>
                <p>No bracket data available.</p>
            </div>
        );
    }

    const winnersBracket = draw.winners;
    const losersBracket = draw.losers;
    const bronzeMatch = draw.bronze;


    // Losers bracket has custom spacing because match counts don't always halve
    const losersSpacing = calculatePadding(TRUE_MATCH_HEIGHT, SPACER_HEIGHT, losersBracket);
    const losersSpacingLines = calculatePaddingLines(TRUE_MATCH_HEIGHT, SPACER_HEIGHT, losersBracket);
    const loserDrawXML = createAllRounds(losersBracket, losersSpacing, losersSpacingLines);
    
    const losersDrawInXML = createAllRounds(losersBracket, losersSpacing, losersSpacingLines, true);

    return (
        <div className={styles.doubleElimContainer}>
            {/* Winners Bracket */}
            <div className={styles.bracketSection}>
                <div className = {styles.bracketLabel}>
                    <h3>Winner Bracket</h3>
                </div>
                <SingleElimBracket draw = {winnersBracket}/>
            </div>
            

            {/* Losers Bracket */}
            <div className={styles.bracketSection}>
                <div className={styles.bracketLabel}>
                    <h3>Losers Bracket</h3>
                </div>
                <SingleElimBracket draw = {losersBracket}/>
            </div>

            {/* Bronze Match (3rd/4th place) */}
            {bronzeMatch && (
                <div className={styles.bracketSection}>
                    <div className={styles.bracketLabel}>
                        <h3>Bronze Match (3rd/4th Place)</h3>
                    </div>
                    <div className={styles.main}>
                        <SpacingContext.Provider value={MATCH_HEIGHT}>
                            <SingleElimEventMatch
                                playerTop={bronzeMatch.teamA || bronzeMatch.playerA || "TBD"}
                                playerBottom={bronzeMatch.teamB || bronzeMatch.playerB || "TBD"}
                                winner={bronzeMatch.winnerTeam || bronzeMatch.winner}
                                arrOfScore={bronzeMatch.score || []}
                                nextMatch={false}
                                prevMatch={false}
                                matchId={bronzeMatch.id}
                                isCompleted={bronzeMatch.completed}
                                matchType={bronzeMatch.matchType || 'SINGLES'}
                            />
                        </SpacingContext.Provider>
                    </div>
                </div>
            )}
        </div>
    );
}

function createMatchesForRound(roundDraw, isFirstRound, curSpacing, isLosersBracket = false) {
    const toDisplay = [];

    // Add spacer at the beginning
    toDisplay.push(<div key="spacer-start" className={styles.gameSpacer} style={{ height: (curSpacing / 2) + "px" }}></div>);

    // Add matches and spacers between them
    roundDraw.forEach((match, index) => {
        toDisplay.push(
            <SpacingContext.Provider value={MATCH_HEIGHT} key={`match-${index}`}>
                <SingleElimEventMatch
                    playerTop={match.teamA || match.playerA || "TBD"}
                    playerBottom={match.teamB || match.playerB || "TBD"}
                    winner={match.winnerTeam || match.winner}
                    arrOfScore={match.score || []}
                    nextMatch={roundDraw.length !== 1 || !isFirstRound}
                    prevMatch={!isFirstRound}
                    highlightLoser={isLosersBracket}
                    matchType={match.matchType || 'SINGLES'}
                />
            </SpacingContext.Provider>
        );

        if (index < roundDraw.length - 1) {
            toDisplay.push(
                <div key={`spacer-${index}`} className={styles.gameSpacer} style={{ height: (curSpacing) + "px" }}></div>
            );
        }
    });

    // Add spacer at the end
    toDisplay.push(<div key="spacer-end" className={styles.gameSpacer} style={{ height: (curSpacing / 2) + "px" }}></div>);

    return toDisplay;
}

function createConnectingLinesForMatches(num, spacing) {
    const answer = [];
    answer.push(<div key="line-start" className={styles.connectingLines} style={{ height: (spacing / 2) + "px" }}></div>);
    for (let i = 0; i < num - 1; i++) {
        answer.push(<div key={`line-${i}`} className={styles.connectingLines} style={{ height: (spacing) + "px" }}></div>);
    }
    answer.push(<div key="line-end" className={styles.connectingLines} style={{ height: (spacing / 2) + "px" }}></div>);
    return answer;
}

function createAllRounds(draw, spacing, spacingLines, isLosersBracket = false) {
    const drawInXML = draw.map((round, index) => (
        <div className={styles.roundContainer} key={"round" + index}>
            <div className={styles.round}>
                {createMatchesForRound(round, (index === 0), spacing[index], isLosersBracket)}
            </div>
            {index < draw.length - 1 && (
                <div className={styles.connectingLinesBlock}>
                    {createConnectingLinesForMatches(round.length, spacingLines[index])}
                </div>
            )}
        </div>
    ));
    return drawInXML;
}

/*
    Returns an array representing in order of the padding between matches. The initial padding is half of the returned padding
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



export default DoubleElimBracket;
