/**
 * Bracket Generation Utility
 *
 * Ports the seeding algorithm from TournamentUtil.java to JavaScript
 * for client-side bracket preview without backend calls.
 *
 * Matches the backend algorithm exactly for consistent behavior.
 */

/**
 * Calculate the next power of 2 greater than or equal to n
 * @param {number} n - Input number
 * @returns {number} - Next power of 2
 */
function nextPowerOfTwo(n) {
  if (n <= 0) return 1;

  // If already a power of 2, return it
  if ((n & (n - 1)) === 0) return n;

  // Find the next power of 2
  let power = 1;
  while (power < n) {
    power *= 2;
  }
  return power;
}

/**
 * Generate standard tournament seed positions using recursive interleaving
 *
 * This algorithm ensures proper seeding where:
 * - Seed 1 plays the lowest seed
 * - Seed 2 plays the 2nd lowest seed
 * - Top seeds can't meet until later rounds
 *
 * Example for 8 players:
 * Start: [1, 2]
 * Round 1: [1, 8, 2, 7]
 * Round 2: [1, 8, 4, 5, 2, 7, 3, 6]
 *
 * Result: Seed 1 at position 0, Seed 8 at position 1, etc.
 * Matchups: 1v8, 4v5, 2v7, 3v6
 *
 * @param {number} bracketSize - Size of bracket (must be power of 2)
 * @returns {number[]} - Array where index is bracket position, value is seed number
 */
function generateStandardSeedPositions(bracketSize) {
  if (bracketSize <= 0) return [];
  if (bracketSize === 1) return [1];
  if (bracketSize === 2) return [1, 2];

  // Start with base case
  let positions = [1, 2];
  let currentSize = 2;

  // Recursively interleave to build up the pattern
  while (currentSize < bracketSize) {
    const newPositions = [];
    const nextSeed = currentSize + 1;

    // For each existing seed, insert its complement
    for (let i = 0; i < positions.length; i++) {
      const seed = positions[i];
      newPositions.push(seed);

      // Calculate the complement seed (opponent in first round)
      // This follows the pattern: 1+max, 2+max-1, 3+max-2, etc.
      const complement = nextSeed + currentSize - seed;
      newPositions.push(complement);
    }

    positions = newPositions;
    currentSize *= 2;
  }

  return positions;
}

/**
 * Generate bracket draw using seeding
 *
 * Ports the algorithm from TournamentUtil.java (lines 29-106)
 *
 * @param {Array} players - Array of player objects with {id, username, name}
 * @param {Object} seeds - Map of userId to seed number {userId: seedNumber}
 * @param {boolean} previewMode - If true, only place seeded players (leave unseeded positions null)
 * @returns {Object} - Bracket structure with matches and metadata
 */
export function generateDrawUsingSeeding(players, seeds = {}, previewMode = false) {
  if (!players || players.length === 0) {
    return {
      matches: [],
      bracketSize: 0,
      hasSeeds: false
    };
  }

  const participantCount = players.length;
  const bracketSize = nextPowerOfTwo(participantCount);
  const matchAmount = bracketSize / 2;

  // Create position array (size = bracketSize)
  const positions = new Array(bracketSize).fill(null);

  // Generate standard seed positions
  const seedPositions = generateStandardSeedPositions(bracketSize);

  // Create inverse map: seed number -> player
  const seedToPlayer = {};
  const seededPlayerIds = new Set();

  // Separate seeded and unseeded players
  const seededPlayers = [];
  const unseededPlayers = [];

  for (const player of players) {
    const seedNumber = seeds[player.id];
    if (seedNumber && seedNumber > 0) {
      seedToPlayer[seedNumber] = player;
      seededPlayerIds.add(player.id);
      seededPlayers.push(player);
    } else {
      unseededPlayers.push(player);
    }
  }

  const hasSeeds = seededPlayers.length > 0;

  // Shuffle unseeded players for randomness
  const shuffledUnseeded = [...unseededPlayers];
  for (let i = shuffledUnseeded.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffledUnseeded[i], shuffledUnseeded[j]] = [shuffledUnseeded[j], shuffledUnseeded[i]];
  }

  if (!hasSeeds) {
    // In preview mode with no seeds, leave all positions empty
    if (previewMode) {
      // Leave all positions as null - no seeded players to show
    } else {
      // Normal mode: treat all players as if seeded 1 through N
      for (let i = 0; i < seedPositions.length && i < participantCount; i++) {
        const seedNumber = i + 1;
        const targetPosition = seedPositions.indexOf(seedNumber);
        if (targetPosition !== -1 && i < players.length) {
          positions[targetPosition] = players[i];
        }
      }
    }
  } else {
    // Place seeded players first
    for (const [seedNumberStr, player] of Object.entries(seedToPlayer)) {
      const seedNumber = parseInt(seedNumberStr);
      if (seedNumber > 0 && seedNumber <= bracketSize) {
        const targetPosition = seedPositions.indexOf(seedNumber);
        if (targetPosition !== -1) {
          positions[targetPosition] = player;
        }
      }
    }

    // Fill remaining empty positions with unseeded players (skip in preview mode)
    if (!previewMode) {
      let unseededIndex = 0;
      for (let i = 0; i < positions.length && unseededIndex < shuffledUnseeded.length; i++) {
        if (positions[i] === null) {
          positions[i] = shuffledUnseeded[unseededIndex++];
        }
      }
    }
  }

  // Create matches from positions
  const matches = [];
  for (let i = 0; i < matchAmount; i++) {
    const playerAIndex = i * 2;
    const playerBIndex = i * 2 + 1;

    const playerA = positions[playerAIndex];
    const playerB = positions[playerBIndex];

    // Get seed numbers for display
    const seedA = playerA ? seeds[playerA.id] || null : null;
    const seedB = playerB ? seeds[playerB.id] || null : null;

    matches.push({
      matchNumber: i + 1,
      playerA,
      playerB,
      seedA,
      seedB,
      isBye: !playerA || !playerB
    });
  }

  return {
    matches,
    bracketSize,
    matchAmount,
    hasSeeds,
    participantCount
  };
}

/**
 * Generate bracket draw using team seeding (for doubles events)
 *
 * @param {Array} teams - Array of team objects with {id, player1, player2}
 * @param {Object} seeds - Map of teamId to seed number {teamId: seedNumber}
 * @param {boolean} previewMode - If true, only place seeded teams (leave unseeded positions null)
 * @returns {Object} - Bracket structure with matches and metadata
 */
export function generateDrawUsingTeamSeeding(teams, seeds = {}, previewMode = false) {
  // Same algorithm as generateDrawUsingSeeding, but for teams
  return generateDrawUsingSeeding(teams, seeds, previewMode);
}

/**
 * Get display name for a player
 * @param {Object} player - Player object
 * @returns {string} - Display name
 */
export function getPlayerDisplayName(player) {
  if (!player) return 'BYE';
  return player.username || player.name || 'Unknown Player';
}

/**
 * Get display name for a team
 * @param {Object} team - Team object with player1 and player2
 * @returns {string} - Display name
 */
export function getTeamDisplayName(team) {
  if (!team) return 'BYE';
  if (team.teamName) return team.teamName;

  const player1Name = team.player1 ? (team.player1.username || team.player1.name) : 'Unknown';
  const player2Name = team.player2 ? (team.player2.username || team.player2.name) : 'Unknown';

  return `${player1Name} / ${player2Name}`;
}

/**
 * Check if event type supports seeding preview
 * @param {string} eventType - Event type discriminator value
 * @returns {boolean} - True if preview is supported
 */
export function supportsPreview(eventType) {
  // Only show preview for single and double elimination
  // Round robin doesn't use seeding the same way
  return eventType === 'SINGLE_ELIM' || eventType === 'DOUBLE_ELIM';
}
