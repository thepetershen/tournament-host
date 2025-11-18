import React from 'react';
import styles from './HowItWorksSection.module.css';

function HowItWorksSection() {
    const steps = [
        {
            number: '1',
            title: 'Create Tournament',
            description: 'Set up your tournament with dates, location, and custom settings in minutes'
        },
        {
            number: '2',
            title: 'Add Events & Players',
            description: 'Configure multiple events, invite players, and manage registrations easily'
        },
        {
            number: '3',
            title: 'Generate Brackets',
            description: 'Automatically create brackets with manual or automatic seeding options'
        },
        {
            number: '4',
            title: 'Track Results',
            description: 'Record matches in real-time and view live standings as they update'
        }
    ];

    return (
        <div className={styles.howItWorksContainer}>
            <div className={styles.howItWorksContent}>
                <h2 className={styles.howItWorksHeading}>How It Works</h2>
                <p className={styles.howItWorksSubtext}>
                    Get started in four simple steps
                </p>

                <div className={styles.stepsContainer}>
                    {steps.map((step, index) => (
                        <div key={index} className={styles.stepWrapper}>
                            <div className={styles.stepCard}>
                                <div className={styles.stepNumber}>{step.number}</div>
                                <h3 className={styles.stepTitle}>{step.title}</h3>
                                <p className={styles.stepDescription}>{step.description}</p>
                            </div>
                            {index < steps.length - 1 && (
                                <div className={styles.stepConnector}>→</div>
                            )}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

export default HowItWorksSection;
