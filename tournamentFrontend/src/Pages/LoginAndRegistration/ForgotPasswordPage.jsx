import React, { useState } from "react";
import { Link } from "react-router-dom";
import styles from "./ForgotPasswordPage.module.css";
import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    setLoading(true);

    try {
      const response = await axios.post(`${API_BASE_URL}/auth/forgot-password`, {
        email,
      });
      setMessage(response.data.message + " Please check your spam/junk folder if you don't see it in your inbox.");
      setEmail(""); // Clear the input
    } catch (err) {
      setError("Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <h2>Forgot Password</h2>
      <p className={styles.subtitle}>
        Enter your email address and we'll send you a link to reset your password.
      </p>

      <form onSubmit={handleSubmit} className={styles.form}>
        <label>
          Email Address:
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            className={styles.input}
            placeholder="your.email@example.com"
          />
        </label>

        {message && <div className={styles.success}>{message}</div>}
        {error && <div className={styles.error}>{error}</div>}

        <button
          type="submit"
          className={styles.submitButton}
          disabled={loading}
        >
          {loading ? "Sending..." : "Send Reset Link"}
        </button>
      </form>

      <div className={styles.links}>
        <Link to="/login" className={styles.link}>
          Back to Login
        </Link>
      </div>
    </div>
  );
}

export default ForgotPasswordPage;
