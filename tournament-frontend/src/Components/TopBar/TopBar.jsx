import React, { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./TopBar.module.css";
import authAxios from "../../utils/authAxios";

function TopBar() {
  const navigate = useNavigate();
  const isLoggedIn = !!localStorage.getItem("token");
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
    localStorage.removeItem("token");
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
      authAxios.get(`/api/search?query=${encodeURIComponent(searchQuery)}`)
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
                      <div key={player.id} className={styles.searchResultItem}>
                        {player.username} {player.name ? `(${player.name})` : ""}
                      </div>
                    ))
                  )}
                  <div style={{fontWeight: 'bold', padding: '12px 16px 2px'}}>Tournaments</div>
                  {searchResults.tournaments.length === 0 ? (
                    <div className={styles.searchPlaceholder}>No tournaments found</div>
                  ) : (
                    searchResults.tournaments.map(tournament => (
                      <div key={tournament.id} className={styles.searchResultItem}>
                        {tournament.name}
                      </div>
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
            <button className={styles.logoutButton} onClick={handleLogout}>Logout</button>
          )}
        </div>
      </div>
    </>
  );
}

export default TopBar;
