import React from 'react';
import { Link } from 'react-router-dom';
import styles from './PlayerLink.module.css';

/**
 * Reusable component for displaying player names as clickable links to their profile page
 * @param {Object} props
 * @param {Object} props.player - Player/user object with id, name, and username
 * @param {string} props.className - Optional additional CSS class
 * @param {boolean} props.showUsername - Whether to show @username after name (default: false)
 */
function PlayerLink({ player, className = '', showUsername = false }) {
    if (!player) {
        return <span className={className}>TBD</span>;
    }

    const displayName = player.name || player.username;

    return (
        <Link
            to={`/player/${player.id}`}
            className={`${styles.playerLink} ${className}`}
        >
            {displayName}
            {showUsername && player.username && (
                <span className={styles.username}> @{player.username}</span>
            )}
        </Link>
    );
}

export default PlayerLink;
