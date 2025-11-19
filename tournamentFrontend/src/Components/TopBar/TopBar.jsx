import React, { useState, useRef, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import styles from "./TopBar.module.css";
import publicAxios from "../../utils/publicAxios";
import { useAuth } from "../../contexts/AuthContext";

function TopBar({ onMenuToggle }) {
  const navigate = useNavigate();
  const { isLoggedIn, clearAuth } = useAuth();
  const [searchFocused, setSearchFocused] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState({ players: [], tournaments: [] });
  const [loading, setLoading] = useState(false);
  const searchRef = useRef(null);
  const debounceTimeout = useRef();

  const handleLogin = () => {
    navigate("/login");
  };

  const handleLogout = () => {
    clearAuth();
    navigate("/login");
  };

  // Debounced search
  useEffect(() => {
    if (!searchQuery) {
      setSearchResults({ players: [], tournaments: [] });
      setLoading(false);
      return;
    }
    setLoading(true);
    if (debounceTimeout.current) clearTimeout(debounceTimeout.current);
    debounceTimeout.current = setTimeout(() => {
      publicAxios.get(`/api/search?query=${encodeURIComponent(searchQuery)}`)
        .then(res => setSearchResults(res.data))
        .catch(() => setSearchResults({ players: [], tournaments: [] }))
        .finally(() => setLoading(false));
    }, 300);
    return () => clearTimeout(debounceTimeout.current);
  }, [searchQuery]);

  // Close search dropdown and overlay when clicking outside
  useEffect(() => {
    function handleClick(e) {
      if (searchRef.current && !searchRef.current.contains(e.target)) {
        setSearchFocused(false);
      }
    }
    if (searchFocused) {
      document.addEventListener("mousedown", handleClick);
    } else {
      document.removeEventListener("mousedown", handleClick);
    }
    return () => document.removeEventListener("mousedown", handleClick);
  }, [searchFocused]);

  return (
    <>
      {searchFocused && <div className={styles.overlay} />}
      <div className={styles.topBar} style={{ position: 'relative', zIndex: 101 }}>
        <button className={styles.menuButton} onClick={onMenuToggle} aria-label="Toggle menu">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <line x1="3" y1="12" x2="21" y2="12"></line>
            <line x1="3" y1="6" x2="21" y2="6"></line>
            <line x1="3" y1="18" x2="21" y2="18"></line>
          </svg>
        </button>
        <div className={styles.searchContainer} ref={searchRef}>
          <input
            type="text"
            className={styles.searchInput}
            placeholder="Search players, tournaments..."
            value={searchQuery}
            onFocus={() => setSearchFocused(true)}
            onChange={e => setSearchQuery(e.target.value)}
            autoComplete="off"
          />
          {searchFocused && (
            <div className={styles.searchDropdown}>
              {loading ? (
                <div className={styles.searchPlaceholder}>Searching...</div>
              ) : (
                <>
                  <div style={{fontWeight: 'bold', padding: '4px 16px 2px'}}>Players</div>
                  {searchResults.players.length === 0 ? (
                    <div className={styles.searchPlaceholder}>No players found</div>
                  ) : (
                    searchResults.players.map(player => (
                      <Link
                        key={player.id}
                        to={`/player/${player.id}`}
                        className={styles.searchResultItem}
                        onClick={() => {
                          setSearchFocused(false);
                          setSearchQuery('');
                        }}
                      >
                        {player.name || player.username}
                      </Link>
                    ))
                  )}
                  <div style={{fontWeight: 'bold', padding: '12px 16px 2px'}}>Tournaments</div>
                  {searchResults.tournaments.length === 0 ? (
                    <div className={styles.searchPlaceholder}>No tournaments found</div>
                  ) : (
                    searchResults.tournaments.map(tournament => (
                      <Link
                        key={tournament.id}
                        to={`/tournament/${tournament.id}`}
                        className={styles.searchResultItem}
                        onClick={() => {
                          setSearchFocused(false);
                          setSearchQuery('');
                        }}
                      >
                        {tournament.name}
                      </Link>
                    ))
                  )}
                </>
              )}
            </div>
          )}
        </div>
        <div className={styles.buttonGroup}>
          {!isLoggedIn ? (
            <button className={styles.loginButton} onClick={handleLogin}>Login</button>
          ) : (
            <>
              <Link to="/profile" className={styles.profileLink}>Profile</Link>
              <button className={styles.logoutButton} onClick={handleLogout}>Logout</button>
            </>
          )}
        </div>
      </div>
    </>
  );
}

export default TopBar;
