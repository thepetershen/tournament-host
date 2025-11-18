import React from 'react';
import { Link } from 'react-router-dom';
import styles from './CTASection.module.css';

function CTASection({ isLoggedIn }) {
    if (isLoggedIn) {
        return (
            <div className={styles.ctaContainer}>
                <div className={styles.ctaContent}>
                    <h2 className={styles.ctaHeading}>Create Your Next Tournament</h2>
                    <p className={styles.ctaSubtext}>
                        Set up a new tournament in minutes with our easy-to-use bracket management tools
                    </p>
                    <Link to="/create-tournament" className={styles.ctaButton}>
                        Create Tournament
                    </Link>
                </div>
            </div>
        );
    }

    return (
        <div className={styles.ctaContainer}>
            <div className={styles.ctaContent}>
                <h2 className={styles.ctaHeading}>Ready to Host Your Tournament?</h2>
                <p className={styles.ctaSubtext}>
                    Join thousands of organizers using our platform to run professional tournaments
                </p>
                <div className={styles.ctaButtons}>
                    <Link to="/register" className={styles.ctaButton}>
                        Sign Up Free
                    </Link>
                    <Link to="/tournaments" className={styles.secondaryButton}>
                        Browse Tournaments
                    </Link>
                </div>
            </div>
        </div>
    );
}

export default CTASection;
