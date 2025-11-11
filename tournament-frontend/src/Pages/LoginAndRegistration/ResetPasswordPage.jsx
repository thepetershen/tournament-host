import React, { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import styles from "./ResetPasswordPage.module.css";
import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get("token");

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [validating, setValidating] = useState(true);
  const [tokenValid, setTokenValid] = useState(false);

  // Validate token on mount
  useEffect(() => {
    if (!token) {
      setError("Invalid reset link");
      setValidating(false);
      return;
    }

    axios
      .get(`${API_BASE_URL}/auth/validate-reset-token/${token}`)
      .then((response) => {
        if (response.data.valid) {
          setTokenValid(true);
        } else {
          setError("This reset link is invalid or has expired");
        }
      })
      .catch(() => {
        setError("This reset link is invalid or has expired");
      })
      .finally(() => {
        setValidating(false);
      });
  }, [token]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");

    // Validate passwords match
    if (newPassword !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    // Validate password length
    if (newPassword.length < 8) {
      setError("Password must be at least 8 characters long");
      return;
    }

    setLoading(true);

    try {
      await axios.post(`${API_BASE_URL}/auth/reset-password`, {
        token,
        newPassword,
      });
      setMessage("Password reset successful! Redirecting to login...");
      setTimeout(() => {
        navigate("/login");
      }, 2000);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to reset password. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  if (validating) {
    return (
      <div className={styles.container}>
        <h2>Validating Reset Link...</h2>
        <p className={styles.subtitle}>Please wait while we verify your reset link.</p>
      </div>
    );
  }

  if (!tokenValid) {
    return (
      <div className={styles.container}>
        <h2>Invalid Reset Link</h2>
        <div className={styles.error}>{error || "This reset link is invalid or has expired"}</div>
        <button onClick={() => navigate("/forgot-password")} className={styles.submitButton}>
          Request New Reset Link
        </button>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <h2>Reset Your Password</h2>
      <p className={styles.subtitle}>
        Enter your new password below. Make sure it's at least 8 characters long.
      </p>

      <form onSubmit={handleSubmit} className={styles.form}>
        <label>
          New Password:
          <input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            required
            minLength={8}
            className={styles.input}
            placeholder="At least 8 characters"
          />
        </label>

        <label>
          Confirm Password:
          <input
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
            minLength={8}
            className={styles.input}
            placeholder="Confirm your password"
          />
        </label>

        {message && <div className={styles.success}>{message}</div>}
        {error && <div className={styles.error}>{error}</div>}

        <button
          type="submit"
          className={styles.submitButton}
          disabled={loading}
        >
          {loading ? "Resetting..." : "Reset Password"}
        </button>
      </form>
    </div>
  );
}

export default ResetPasswordPage;
