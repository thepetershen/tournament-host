import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './HeroSection.module.css';

function HeroSection({ isLoggedIn, user }) {
    const navigate = useNavigate();

    if (isLoggedIn) {
        // Personalized dashboard hero for logged-in users
        return (
            <div className={styles.heroContainer}>
                <div className={styles.heroContent}>
                    <h1 className={styles.welcomeHeading}>
                        Welcome back{user?.name ? `, ${user.name}` : ''}!
                    </h1>
                    <p className={styles.heroSubtext}>
                        Ready to manage your tournaments or join new competitions
                    </p>
                    <div className={styles.ctaButtons}>
                        <Link to="/create-tournament" className={styles.primaryButton}>
                            Create Tournament
                        </Link>
                        <Link to="/tournaments" className={styles.secondaryButton}>
                            Browse Tournaments
                        </Link>
                    </div>
                </div>
            </div>
        );
    }

    // Marketing hero for logged-out users
    return (
        <div className={styles.heroContainer}>
            <div className={styles.heroContent}>
                <h1 className={styles.heroHeading}>
                    Host and Manage Tournaments
                    <span className={styles.heroHighlight}> with Ease</span>
                </h1>
                <p className={styles.heroSubtext}>
                    Professional tournament brackets, real-time results, and comprehensive
                    league management all in one powerful platform
                </p>
                <div className={styles.ctaButtons}>
                    <Link to="/register" className={styles.primaryButton}>
                        Get Started Free
                    </Link>
                    <Link to="/tournaments" className={styles.secondaryButton}>
                        Browse Tournaments
                    </Link>
                </div>
            </div>
        </div>
    );
}

export default HeroSection;
