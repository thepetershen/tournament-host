import React, { useMemo } from 'react';
import {
  generateDrawUsingSeeding,
  generateDrawUsingTeamSeeding,
  getPlayerDisplayName,
  getTeamDisplayName
} from '../utils/bracketGenerator';
import '../styles/BracketPreview.css';

/**
 * BracketPreview Component
 *
 * Displays a live preview of how the tournament bracket will look
 * based on current seeding, without requiring backend calls.
 *
 * Shows only first-round matches to give users a clear view of
 * how their seeding decisions affect matchups.
 */
const BracketPreview = ({
  players = [],
  teams = [],
  seeds = {},
  teamSeeds = {},
  matchType = 'SINGLES',
  eventType = 'SINGLE_ELIM'
}) => {
  // Generate bracket structure based on current seeds
  const bracketData = useMemo(() => {
    if (matchType === 'DOUBLES' && teams && teams.length > 0) {
      return generateDrawUsingTeamSeeding(teams, teamSeeds, true); // preview mode
    } else if (players && players.length > 0) {
      return generateDrawUsingSeeding(players, seeds, true); // preview mode
    }
    return { matches: [], bracketSize: 0, hasSeeds: false };
  }, [players, teams, seeds, teamSeeds, matchType]);

  // Show all first round matches in preview
  const relevantMatches = useMemo(() => {
    return bracketData.matches;
  }, [bracketData]);

  if (!players || players.length === 0) {
    return (
      <div className="bracket-preview">
        <div className="bracket-preview-header">
          <h3>Bracket Preview</h3>
          <p className="preview-subtitle">First Round Matchups</p>
        </div>
        <div className="preview-empty">
          <p>No players registered yet</p>
        </div>
      </div>
    );
  }

  if (relevantMatches.length === 0) {
    return (
      <div className="bracket-preview">
        <div className="bracket-preview-header">
          <h3>Bracket Preview</h3>
          <p className="preview-subtitle">First Round Matchups</p>
        </div>
        <div className="preview-empty">
          <p>No matches to preview</p>
        </div>
      </div>
    );
  }

  const isDoubles = matchType === 'DOUBLES';

  return (
    <div className="bracket-preview">
      <div className="bracket-preview-header">
        <h3>Bracket Preview</h3>
        <p className="preview-subtitle">
          First Round Matchups
          {bracketData.hasSeeds && (
            <span className="seeded-count">
              ({Object.keys(isDoubles ? teamSeeds : seeds).length} seeded)
            </span>
          )}
        </p>
      </div>

      <div className="preview-info">
        <div className="info-item">
          <span className="info-label">Bracket Size:</span>
          <span className="info-value">{bracketData.bracketSize}</span>
        </div>
        <div className="info-item">
          <span className="info-label">Participants:</span>
          <span className="info-value">{bracketData.participantCount}</span>
        </div>
        <div className="info-item">
          <span className="info-label">First Round Matches:</span>
          <span className="info-value">{bracketData.matchAmount}</span>
        </div>
      </div>

      <div className="matches-container">
        {relevantMatches.map((match) => (
          <MatchCard
            key={match.matchNumber}
            match={match}
            isDoubles={isDoubles}
          />
        ))}
      </div>

      {!bracketData.hasSeeds && (
        <div className="preview-note">
          <small>
            <strong>Note:</strong> No seeds set. Players are placed in order.
            Add seeds to customize matchups.
          </small>
        </div>
      )}
    </div>
  );
};

/**
 * MatchCard Component
 * Displays a single match with seed information
 */
const MatchCard = ({ match, isDoubles }) => {
  const getDisplayName = isDoubles ? getTeamDisplayName : getPlayerDisplayName;

  // Show blank for null players in preview mode (not "BYE")
  const playerAName = match.playerA ? getDisplayName(match.playerA) : '';
  const playerBName = match.playerB ? getDisplayName(match.playerB) : '';

  return (
    <div className="match-card">
      <div className="match-number">Match {match.matchNumber}</div>

      <div className="match-participants">
        <div className={`participant ${match.seedA ? 'seeded' : ''}`}>
          {match.seedA && <span className="seed-badge">Seed {match.seedA}</span>}
          <span className="participant-name">{playerAName}</span>
        </div>

        <div className="vs-divider">vs</div>

        <div className={`participant ${match.seedB ? 'seeded' : ''}`}>
          {match.seedB && <span className="seed-badge">Seed {match.seedB}</span>}
          <span className="participant-name">{playerBName}</span>
        </div>
      </div>
    </div>
  );
};

export default BracketPreview;
