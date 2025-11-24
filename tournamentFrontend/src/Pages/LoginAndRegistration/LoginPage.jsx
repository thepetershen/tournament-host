import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import styles from "./LoginPage.module.css";
import axios from "axios";
import { useAuth } from "../../contexts/AuthContext";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { setToken, setUser } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      // Login to get token
      const loginResponse = await axios.post(`${API_BASE_URL}/auth/login`, {
        username,
        password,
      });
      const { token } = loginResponse.data;

      // Fetch user data
      const userResponse = await axios.get(`${API_BASE_URL}/api/users/me`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      // Update AuthContext with token and user data
      setToken(token);
      setUser(userResponse.data);

      // Redirect to home page
      navigate("/");
    } catch (err) {
      setError("Invalid username or password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.loginContainer}>
      <h2>Login</h2>
      <form onSubmit={handleSubmit} className={styles.loginForm}>
        <label>
          Username:
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            className={styles.input}
          />
        </label>
        <label>
          Password:
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            className={styles.input}
          />
        </label>
        {error && <div className={styles.error}>{error}</div>}
        <button type="submit" className={styles.loginButton} disabled={loading}>
          {loading ? "Logging in..." : "Login"}
        </button>
      </form>
      <div className={styles.forgotPassword}>
        <Link to="/forgot-password" className={styles.forgotLink}>
          Forgot password?
        </Link>
      </div>
      <div className={styles.registerLink}>
        Don't have an account? <Link to="/register" className={styles.link}>Register here</Link>
      </div>
    </div>
  );
}

export default LoginPage;
