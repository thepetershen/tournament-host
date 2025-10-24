import SingleElimEventMatch from "../SingleElimEvent/SingleElimEventMatch";
import styles from "./DoubleElimBracketStyle.module.css";
import React, { createContext } from "react";

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

    // Calculate spacing for each bracket
    // Winners bracket follows standard single-elim pattern (each round halves)
    const winnersSpacing = calculatePadding(TRUE_MATCH_HEIGHT, SPACER_HEIGHT, winnersBracket.length);
    const winnersSpacingLines = calculatePaddingLines(TRUE_MATCH_HEIGHT, SPACER_HEIGHT, winnersBracket.length);

    // Losers bracket has custom spacing because match counts don't always halve
    const losersSpacing = calculateLosersBracketPadding(TRUE_MATCH_HEIGHT, SPACER_HEIGHT, losersBracket);
    const losersSpacingLines = calculateLosersBracketPaddingLines(TRUE_MATCH_HEIGHT, SPACER_HEIGHT, losersBracket);

    // Create bracket JSX
    const winnersDrawInXML = createAllRounds(winnersBracket, winnersSpacing, winnersSpacingLines, false);
    const losersDrawInXML = createAllRounds(losersBracket, losersSpacing, losersSpacingLines, true);

    return (
        <div className={styles.doubleElimContainer}>
            {/* Winners Bracket */}
            <div className={styles.bracketSection}>
                <div className={styles.bracketLabel}>
                    <h3>Winners Bracket</h3>
                </div>
                <div className={styles.main}>
                    {winnersDrawInXML}
                </div>
            </div>

            {/* Losers Bracket */}
            <div className={styles.bracketSection}>
                <div className={styles.bracketLabel}>
                    <h3>Losers Bracket</h3>
                </div>
                <div className={styles.main}>
                    {losersDrawInXML}
                </div>
            </div>
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
                    playerTop={match.playerA?.username || "TBD"}
                    playerBottom={match.playerB?.username || "TBD"}
                    winner={match.winner?.username}
                    arrOfScore={match.score || []}
                    nextMatch={roundDraw.length !== 1 || !isFirstRound}
                    prevMatch={!isFirstRound}
                    highlightLoser={isLosersBracket}
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
    for (let i = 0; i < drawLength - 1; i++) {
        answer.push((TRUE_MATCH_HEIGHT * (Math.pow(2, i))) + (SPACER_HEIGHT * (Math.pow(2, i))));
    }
    return answer;
}

/**
 * Calculate spacing for losers bracket based on actual match counts
 * Losers bracket doesn't follow power-of-2 pattern due to feed-in rounds
 */
function calculateLosersBracketPadding(TRUE_MATCH_HEIGHT, SPACER_HEIGHT, losersBracket) {
    const answer = [];

    for (let i = 0; i < losersBracket.length; i++) {
        const currentRound = losersBracket[i];
        const matchCount = currentRound.length;

        if (i === 0) {
            // First round uses base spacing
            answer.push(SPACER_HEIGHT);
        } else {
            const prevRound = losersBracket[i - 1];
            const prevMatchCount = prevRound.length;

            // If match count doubled (feed-in round), reduce spacing by half
            // If match count stayed same or halved (progression round), calculate normally
            if (matchCount > prevMatchCount) {
                // Feed-in: more matches, so reduce spacing
                const prevSpacing = answer[i - 1];
                answer.push(Math.max(SPACER_HEIGHT, prevSpacing / 2));
            } else if (matchCount === prevMatchCount) {
                // Same count: keep spacing the same
                answer.push(answer[i - 1]);
            } else {
                // Halved: double the spacing (standard single-elim behavior)
                answer.push((answer[i - 1] * 2) + TRUE_MATCH_HEIGHT);
            }
        }
    }

    return answer;
}

/**
 * Calculate connecting line spacing for losers bracket based on actual match counts
 */
function calculateLosersBracketPaddingLines(TRUE_MATCH_HEIGHT, SPACER_HEIGHT, losersBracket) {
    const answer = [];

    for (let i = 0; i < losersBracket.length - 1; i++) {
        const currentRound = losersBracket[i];
        const nextRound = losersBracket[i + 1];
        const currentMatchCount = currentRound.length;
        const nextMatchCount = nextRound.length;

        if (nextMatchCount > currentMatchCount) {
            // Feed-in round coming: connecting lines need to account for new players
            // Lines should be shorter/tighter
            answer.push(TRUE_MATCH_HEIGHT + SPACER_HEIGHT);
        } else if (nextMatchCount === currentMatchCount) {
            // Same match count: standard spacing
            answer.push((TRUE_MATCH_HEIGHT * 2) + (SPACER_HEIGHT * 2));
        } else {
            // Next round has fewer matches: standard progression
            const ratio = currentMatchCount / nextMatchCount;
            answer.push((TRUE_MATCH_HEIGHT * ratio) + (SPACER_HEIGHT * ratio));
        }
    }

    return answer;
}

export default DoubleElimBracket;
