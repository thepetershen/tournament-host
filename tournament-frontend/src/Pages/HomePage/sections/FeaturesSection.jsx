import React from 'react';
import styles from './FeaturesSection.module.css';

function FeaturesSection() {
    const features = [
        {
            icon: '🏆',
            title: 'Multiple Tournament Formats',
            description: 'Single elimination, double elimination, and round robin brackets for any competition style'
        },
        {
            icon: '⚡',
            title: 'Real-Time Bracket Updates',
            description: 'Live bracket visualization and instant match result tracking keep everyone informed'
        },
        {
            icon: '🎯',
            title: 'Flexible Event System',
            description: 'Host multiple events per tournament with support for singles and doubles matches'
        },
        {
            icon: '📊',
            title: 'League System',
            description: 'Create comprehensive leagues with automated rankings and points distribution'
        },
        {
            icon: '🎖️',
            title: 'Points Tracking',
            description: 'Automated placement tracking and standings calculation across tournaments'
        },
        {
            icon: '⚙️',
            title: 'Easy Management',
            description: 'Player approval, bracket seeding, and match recording all in one streamlined interface'
        }
    ];

    return (
        <div className={styles.featuresContainer}>
            <div className={styles.featuresContent}>
                <h2 className={styles.featuresHeading}>Powerful Features</h2>
                <p className={styles.featuresSubtext}>
                    Everything you need to run professional tournaments
                </p>

                <div className={styles.featuresGrid}>
                    {features.map((feature, index) => (
                        <div key={index} className={styles.featureCard}>
                            <div className={styles.featureIcon}>{feature.icon}</div>
                            <h3 className={styles.featureTitle}>{feature.title}</h3>
                            <p className={styles.featureDescription}>{feature.description}</p>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

export default FeaturesSection;
