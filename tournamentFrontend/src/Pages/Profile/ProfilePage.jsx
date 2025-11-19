import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import authAxios from '../../utils/authAxios';
import styles from './ProfilePage.module.css';

function ProfilePage() {
    const { user, setUser } = useAuth();
    const [formData, setFormData] = useState({
        name: '',
        username: '',
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    });
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState({ type: '', text: '' });
    const [changePassword, setChangePassword] = useState(false);

    useEffect(() => {
        if (user) {
            setFormData(prev => ({
                ...prev,
                name: user.name || '',
                username: user.username || ''
            }));
        }
    }, [user]);

    const showMessage = (type, text) => {
        setMessage({ type, text });
        setTimeout(() => setMessage({ type: '', text: '' }), 5000);
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        // Validation
        if (!formData.name.trim() || !formData.username.trim()) {
            showMessage('error', 'Name and username are required');
            setLoading(false);
            return;
        }

        if (changePassword) {
            if (!formData.currentPassword) {
                showMessage('error', 'Current password is required to change password');
                setLoading(false);
                return;
            }
            if (formData.newPassword.length < 6) {
                showMessage('error', 'New password must be at least 6 characters');
                setLoading(false);
                return;
            }
            if (formData.newPassword !== formData.confirmPassword) {
                showMessage('error', 'New passwords do not match');
                setLoading(false);
                return;
            }
        }

        try {
            const updateData = {
                name: formData.name,
                username: formData.username,
                password: changePassword ? formData.newPassword : undefined
            };

            const response = await authAxios.put('/api/users/me', updateData);

            // Update user in context
            setUser(response.data);

            showMessage('success', 'Profile updated successfully!');

            // Clear password fields
            if (changePassword) {
                setFormData(prev => ({
                    ...prev,
                    currentPassword: '',
                    newPassword: '',
                    confirmPassword: ''
                }));
                setChangePassword(false);
            }
        } catch (err) {
            console.error('Profile update error:', err);
            showMessage('error', err.response?.data?.message || 'Failed to update profile');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={styles.pageContainer}>
            <div className={styles.profileCard}>
                <h1 className={styles.title}>Profile Settings</h1>

                {message.text && (
                    <div className={`${styles.message} ${styles[message.type]}`}>
                        {message.text}
                    </div>
                )}

                <form onSubmit={handleSubmit} className={styles.form}>
                    <div className={styles.formSection}>
                        <h2 className={styles.sectionTitle}>Basic Information</h2>

                        <div className={styles.formGroup}>
                            <label htmlFor="name" className={styles.label}>
                                Display Name
                            </label>
                            <input
                                type="text"
                                id="name"
                                name="name"
                                value={formData.name}
                                onChange={handleInputChange}
                                className={styles.input}
                                placeholder="Enter your display name"
                                required
                            />
                        </div>

                        <div className={styles.formGroup}>
                            <label htmlFor="username" className={styles.label}>
                                Username (Email)
                            </label>
                            <input
                                type="text"
                                id="username"
                                name="username"
                                value={formData.username}
                                onChange={handleInputChange}
                                className={styles.input}
                                placeholder="Enter your username/email"
                                required
                            />
                        </div>
                    </div>

                    <div className={styles.formSection}>
                        <div className={styles.passwordHeader}>
                            <h2 className={styles.sectionTitle}>Password</h2>
                            <button
                                type="button"
                                onClick={() => setChangePassword(!changePassword)}
                                className={styles.toggleButton}
                            >
                                {changePassword ? 'Cancel Password Change' : 'Change Password'}
                            </button>
                        </div>

                        {changePassword && (
                            <>
                                <div className={styles.formGroup}>
                                    <label htmlFor="currentPassword" className={styles.label}>
                                        Current Password
                                    </label>
                                    <input
                                        type="password"
                                        id="currentPassword"
                                        name="currentPassword"
                                        value={formData.currentPassword}
                                        onChange={handleInputChange}
                                        className={styles.input}
                                        placeholder="Enter current password"
                                    />
                                </div>

                                <div className={styles.formGroup}>
                                    <label htmlFor="newPassword" className={styles.label}>
                                        New Password
                                    </label>
                                    <input
                                        type="password"
                                        id="newPassword"
                                        name="newPassword"
                                        value={formData.newPassword}
                                        onChange={handleInputChange}
                                        className={styles.input}
                                        placeholder="Enter new password (min 6 characters)"
                                    />
                                </div>

                                <div className={styles.formGroup}>
                                    <label htmlFor="confirmPassword" className={styles.label}>
                                        Confirm New Password
                                    </label>
                                    <input
                                        type="password"
                                        id="confirmPassword"
                                        name="confirmPassword"
                                        value={formData.confirmPassword}
                                        onChange={handleInputChange}
                                        className={styles.input}
                                        placeholder="Confirm new password"
                                    />
                                </div>
                            </>
                        )}
                    </div>

                    <div className={styles.buttonGroup}>
                        <button
                            type="submit"
                            className={styles.saveButton}
                            disabled={loading}
                        >
                            {loading ? 'Saving...' : 'Save Changes'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default ProfilePage;
