import styles from './RoundRobinBracket.module.css';
import React from 'react';

/**
 * RoundRobinBracket component
 *
 * @param {Array} draw - Array of player rows from backend
 *   Each row is [UserDTO, MatchDTO, MatchDTO, ...]
 *   where UserDTO = { id, username, ... }
 *   and MatchDTO = { id, playerA: {id, username}, playerB: {id, username}, winner: {id, username}, score: [[a1,b1], [a2,b2], ...] }
 */
function RoundRobinBracket({ draw }) {

    if (!draw || draw.length === 0) {
        return (
            <div className={styles.roundRobinContainer}>
                <p>No players registered yet.</p>
            </div>
        );
    }

    // Extract players from draw (first element of each row)
    const players = draw.map(row => row[0]);

    // Build a lookup map for matches
    // Key: "playerId1-playerId2" (sorted to handle both directions)
    const matchMap = new Map();

    draw.forEach(row => {
        const player = row[0];
        const matches = row.slice(1); // Everything after the user is a match

        matches.forEach(match => {
            if (match && match.playerA && match.playerB) {
                // Create consistent key regardless of player order
                const key1 = `${player.id}-${match.playerA.id === player.id ? match.playerB.id : match.playerA.id}`;
                const key2 = `${match.playerA.id === player.id ? match.playerB.id : match.playerA.id}-${player.id}`;

                matchMap.set(key1, match);
                matchMap.set(key2, match);
            }
        });
    });

    const getMatch = (player1, player2) => {
        const key = `${player1.id}-${player2.id}`;
        return matchMap.get(key);
    };

    const renderMatchCell = (rowPlayer, colPlayer, rowIndex, colIndex) => {
        // Diagonal cells (player vs themselves)
        if (rowIndex === colIndex) {
            return (
                <div key={`${rowIndex}-${colIndex}`} className={styles.disabledCell} />
            );
        }

        const match = getMatch(rowPlayer, colPlayer);

        // No match found
        if (!match) {
            return (
                <div key={`${rowIndex}-${colIndex}`} className={styles.matchCell}>
                    <span className={styles.pendingMatch}>Not scheduled</span>
                </div>
            );
        }

        // Determine scores for this specific match cell perspective
        // Row player is the perspective we're showing from
        const isRowPlayerA = match.playerA.id === rowPlayer.id;
        const didRowPlayerWin = match.winner && match.winner.id === rowPlayer.id;

        // Check if match has scores (completed or in progress)
        // Backend returns score as [scoreA, scoreB] for Round Robin
        const hasScores = match.score && match.score.length > 0;

        // Determine background color based on win/loss
        let cellClassName = styles.matchCell;
        if (match.winner) {
            cellClassName = didRowPlayerWin ? `${styles.matchCell} ${styles.winCell}` : `${styles.matchCell} ${styles.lossCell}`;
        }

        return (
            <div
                key={`${rowIndex}-${colIndex}`}
                className={cellClassName}
            >
                {hasScores ? (
                    <>
                        <div className={styles.scoreDisplay}>
                            <span className={styles.scoreNumber}>
                                {isRowPlayerA ? match.score[0] : match.score[1]}
                            </span>
                            <span className={styles.scoreSeparator}>-</span>
                            <span className={styles.scoreNumber}>
                                {isRowPlayerA ? match.score[1] : match.score[0]}
                            </span>
                        </div>
                        <div className={`${styles.matchStatus} ${match.winner ? styles.statusComplete : styles.statusInProgress}`}>
                            {match.winner ? (didRowPlayerWin ? 'W' : 'L') : 'In Progress'}
                        </div>
                    </>
                ) : (
                    <>
                        <span className={styles.pendingMatch}>Pending</span>
                        <div className={`${styles.matchStatus} ${styles.statusPending}`}>
                            -
                        </div>
                    </>
                )}
            </div>
        );
    };

    // Set up grid template for CSS Grid
    const gridTemplateColumns = `180px repeat(${players.length}, 180px)`;
    const gridTemplateRows = `60px repeat(${players.length}, 60px)`;

    return (
        <div className={styles.roundRobinContainer}>
            <div
                className={styles.bracketGrid}
                style={{
                    gridTemplateColumns,
                    gridTemplateRows
                }}
            >
                {/* Top-left corner cell */}
                <div className={styles.cornerCell}>
                    Players
                </div>

                {/* Top header row - player names */}
                {players.map((player, index) => (
                    <div key={`header-col-${index}`} className={styles.headerCell}>
                        {player.username}
                    </div>
                ))}

                {/* Each row */}
                {players.map((rowPlayer, rowIndex) => (
                    <React.Fragment key={`row-${rowIndex}`}>
                        {/* Left header column - player name */}
                        <div className={styles.headerCell}>
                            {rowPlayer.username}
                        </div>

                        {/* Match cells */}
                        {players.map((colPlayer, colIndex) =>
                            renderMatchCell(rowPlayer, colPlayer, rowIndex, colIndex)
                        )}
                    </React.Fragment>
                ))}
            </div>
        </div>
    );
}

export default RoundRobinBracket;
