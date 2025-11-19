import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authAxios from '../../utils/authAxios';
import styles from './CreateTournamentPage.module.css';

function CreateTournamentPage() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        name: '',
        message: '',
        location: '',
        begin: '',
        end: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            // Convert dates to proper format if provided
            const payload = {
                name: formData.name,
                message: formData.message || null,
                location: formData.location || null,
                begin: formData.begin ? new Date(formData.begin).toISOString() : null,
                end: formData.end ? new Date(formData.end).toISOString() : null
            };

            const response = await authAxios.post('/api/tournaments', payload);

            // Navigate to the tournament control page
            navigate(`/tournament/${response.data.id}/control`);
        } catch (err) {
            console.error('Error creating tournament:', err);
            setError(err.response?.data?.message || 'Failed to create tournament. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={styles.createTournamentPage}>
            <div className={styles.container}>
                <button
                    onClick={() => navigate('/')}
                    className={styles.backButton}
                >
                    ← Back to Home
                </button>

                <h1 className={styles.title}>Create Tournament</h1>

                {error && (
                    <div className={styles.error}>
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className={styles.form}>
                    <div className={styles.formGroup}>
                        <label htmlFor="name" className={styles.label}>
                            Tournament Name <span className={styles.required}>*</span>
                        </label>
                        <input
                            type="text"
                            id="name"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            required
                            className={styles.input}
                            placeholder="Enter tournament name"
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label htmlFor="message" className={styles.label}>
                            Description
                        </label>
                        <textarea
                            id="message"
                            name="message"
                            value={formData.message}
                            onChange={handleChange}
                            className={styles.textarea}
                            placeholder="Enter tournament description (optional)"
                            rows="4"
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label htmlFor="location" className={styles.label}>
                            Location
                        </label>
                        <input
                            type="text"
                            id="location"
                            name="location"
                            value={formData.location}
                            onChange={handleChange}
                            className={styles.input}
                            placeholder="Enter tournament location (optional)"
                        />
                    </div>

                    <div className={styles.dateGroup}>
                        <div className={styles.formGroup}>
                            <label htmlFor="begin" className={styles.label}>
                                Start Date
                            </label>
                            <input
                                type="date"
                                id="begin"
                                name="begin"
                                value={formData.begin}
                                onChange={handleChange}
                                className={styles.input}
                            />
                        </div>

                        <div className={styles.formGroup}>
                            <label htmlFor="end" className={styles.label}>
                                End Date
                            </label>
                            <input
                                type="date"
                                id="end"
                                name="end"
                                value={formData.end}
                                onChange={handleChange}
                                className={styles.input}
                            />
                        </div>
                    </div>

                    <div className={styles.buttonGroup}>
                        <button
                            type="button"
                            onClick={() => navigate('/')}
                            className={styles.cancelButton}
                            disabled={loading}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className={styles.submitButton}
                            disabled={loading || !formData.name}
                        >
                            {loading ? 'Creating...' : 'Create Tournament'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default CreateTournamentPage;
