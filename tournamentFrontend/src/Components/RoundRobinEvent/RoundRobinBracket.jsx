import styles from './RoundRobinBracket.module.css';
import React from 'react';
import { Link } from 'react-router-dom';

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

    // Helper functions to support both players and teams
    const getParticipantId = (participant) => {
        return participant?.id;
    };

    const getParticipant = (match, side) => {
        if (side === 'A') {
            return match.teamA || match.playerA;
        }
        return match.teamB || match.playerB;
    };

    const getWinner = (match) => {
        return match.winnerTeam || match.winner;
    };

    // Extract players/teams from draw (first element of each row)
    const participants = draw.map(row => row[0]);

    // Build a lookup map for matches
    // Key: "playerId1-playerId2" (sorted to handle both directions)
    const matchMap = new Map();

    draw.forEach(row => {
        const participant = row[0];
        const matches = row.slice(1); // Everything after the participant is a match

        matches.forEach(match => {
            const participantA = getParticipant(match, 'A');
            const participantB = getParticipant(match, 'B');

            if (match && participantA && participantB) {
                const participantAId = getParticipantId(participantA);
                const participantBId = getParticipantId(participantB);
                const participantId = getParticipantId(participant);

                // Create consistent key regardless of participant order
                const key1 = `${participantId}-${participantAId === participantId ? participantBId : participantAId}`;
                const key2 = `${participantAId === participantId ? participantBId : participantAId}-${participantId}`;

                matchMap.set(key1, match);
                matchMap.set(key2, match);
            }
        });
    });

    const getMatch = (participant1, participant2) => {
        const key = `${participant1.id}-${participant2.id}`;
        return matchMap.get(key);
    };

    const getParticipantName = (participant) => {
        return participant.teamName || participant.name || participant.username || 'Participant';
    };

    const renderParticipantName = (participant) => {
        // For teams
        if (participant.teamName) {
            return participant.teamName;
        }

        // For individual players/users
        if (participant.id && participant.username) {
            return (
                <Link
                    to={`/player/${participant.id}`}
                    style={{ color: 'inherit', textDecoration: 'none' }}
                >
                    {participant.name || participant.username}
                </Link>
            );
        }

        return 'Participant';
    };

    const renderMatchCell = (rowParticipant, colParticipant, rowIndex, colIndex) => {
        // Diagonal cells (participant vs themselves)
        if (rowIndex === colIndex) {
            return (
                <div key={`${rowIndex}-${colIndex}`} className={styles.disabledCell} />
            );
        }

        const match = getMatch(rowParticipant, colParticipant);

        // No match found
        if (!match) {
            return (
                <div key={`${rowIndex}-${colIndex}`} className={styles.matchCell}>
                    <span className={styles.pendingMatch}>Not scheduled</span>
                </div>
            );
        }

        // Determine scores for this specific match cell perspective
        // Row participant is the perspective we're showing from
        const participantA = getParticipant(match, 'A');
        const participantB = getParticipant(match, 'B');
        const isRowParticipantA = participantA && getParticipantId(participantA) === getParticipantId(rowParticipant);
        const winner = getWinner(match);
        const didRowParticipantWin = winner && getParticipantId(winner) === getParticipantId(rowParticipant);

        // Check if match has scores (completed or in progress)
        // Backend returns score as [scoreA, scoreB] for Round Robin
        const hasScores = match.score && match.score.length > 0;

        // Determine background color based on win/loss
        let cellClassName = styles.matchCell;
        if (winner) {
            cellClassName = didRowParticipantWin ? `${styles.matchCell} ${styles.winCell}` : `${styles.matchCell} ${styles.lossCell}`;
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
                                {isRowParticipantA ? match.score[0] : match.score[1]}
                            </span>
                            <span className={styles.scoreSeparator}>-</span>
                            <span className={styles.scoreNumber}>
                                {isRowParticipantA ? match.score[1] : match.score[0]}
                            </span>
                        </div>
                        <div className={`${styles.matchStatus} ${winner ? styles.statusComplete : styles.statusInProgress}`}>
                            {winner ? (didRowParticipantWin ? 'W' : 'L') : 'In Progress'}
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
    const gridTemplateColumns = `180px repeat(${participants.length}, 180px)`;
    const gridTemplateRows = `60px repeat(${participants.length}, 60px)`;

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
                    Participants
                </div>

                {/* Top header row - participant names */}
                {participants.map((participant, index) => (
                    <div key={`header-col-${index}`} className={styles.headerCell}>
                        {renderParticipantName(participant)}
                    </div>
                ))}

                {/* Each row */}
                {participants.map((rowParticipant, rowIndex) => (
                    <React.Fragment key={`row-${rowIndex}`}>
                        {/* Left header column - participant name */}
                        <div className={styles.headerCell}>
                            {renderParticipantName(rowParticipant)}
                        </div>

                        {/* Match cells */}
                        {participants.map((colParticipant, colIndex) =>
                            renderMatchCell(rowParticipant, colParticipant, rowIndex, colIndex)
                        )}
                    </React.Fragment>
                ))}
            </div>
        </div>
    );
}

export default RoundRobinBracket;
