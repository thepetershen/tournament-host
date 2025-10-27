import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import authAxios from "../../utils/authAxios";
import styles from "./SignUpPage.module.css";

function SignUpPage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const [tournament, setTournament] = useState(null);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [signupStatus, setSignupStatus] = useState({});
  const [message, setMessage] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      try {
        // Fetch tournament general info
        const tournamentResponse = await authAxios.get(`/api/tournaments/${tournamentId}`);
        setTournament(tournamentResponse.data);

        // Fetch events
        const eventsResponse = await authAxios.get(`/api/tournaments/${tournamentId}/events`);
        setEvents(eventsResponse.data);

        setLoading(false);
      } catch (err) {
        console.error(err);
        setMessage("Failed to load tournament data");
        setLoading(false);
      }
    };

    fetchData();
  }, [tournamentId]);

  const handleSignUp = async (eventIndex) => {
    try {
      setMessage("");
      await authAxios.post(`/api/tournaments/${tournamentId}/event/${eventIndex}/signup`);

      setSignupStatus({
        ...signupStatus,
        [eventIndex]: "success"
      });
      setMessage(`Successfully signed up for ${events.find(e => e.id === eventIndex)?.name || 'event'}!`);
    } catch (err) {
      const errorMsg = err.response?.data || "Failed to sign up";
      setSignupStatus({
        ...signupStatus,
        [eventIndex]: "error"
      });
      setMessage(errorMsg);
    }
  };

  if (loading) {
    return <div className={styles.loadingContainer}>Loading...</div>;
  }

  return (
    <div className={styles.pageContainer}>
      {/* Tournament Header Section with colored background */}
      <div className={styles.tournamentHeader}>
        <h1 className={styles.tournamentTitle}>
          {tournament ? tournament.name : "Loading..."}
        </h1>
        {tournament && (
          <div className={styles.tournamentMeta}>
            {tournament.owner && (
              <div className={styles.metaItem}>
                <span className={styles.metaLabel}>Owner:</span>
                <span>{tournament.owner.username || tournament.owner.name}</span>
              </div>
            )}
            <div className={styles.metaItem}>
              <span className={styles.metaLabel}>Events:</span>
              <span>{events.length}</span>
            </div>
          </div>
        )}
      </div>

      {/* Sign Up Content Area */}
      <div className={styles.contentArea}>
        <h2 className={styles.sectionTitle}>Sign Up for Events</h2>

        {message && (
          <div className={`${styles.messageBox} ${message.includes("Failed") || message.includes("error") ? styles.error : styles.success}`}>
            {message}
          </div>
        )}

        {events.length === 0 ? (
          <div className={styles.emptyState}>No events available for signup.</div>
        ) : (
          <div>
            <p className={styles.instructions}>
              Select the events you would like to participate in. Your registration will be pending approval from the tournament organizers.
            </p>
            <ul className={styles.eventsList}>
              {events.map(event => (
                <li key={event.id} className={styles.eventCard}>
                  <div className={styles.eventInfo}>
                    <h3 className={styles.eventName}>{event.name}</h3>
                    {signupStatus[event.id] === "success" && (
                      <p className={styles.eventStatus}>Registration pending approval</p>
                    )}
                  </div>
                  <button
                    onClick={() => handleSignUp(event.id)}
                    disabled={signupStatus[event.id] === "success"}
                    className={`${styles.signUpButton} ${signupStatus[event.id] === "success" ? styles.success : ""}`}
                  >
                    {signupStatus[event.id] === "success" ? "✓ Signed Up" : "Sign Up"}
                  </button>
                </li>
              ))}
            </ul>
          </div>
        )}

        <div className={styles.actionsContainer}>
          <button
            onClick={() => navigate(`/tournament/${tournamentId}`)}
            className={styles.backButton}
          >
            Back to Tournament
          </button>
        </div>
      </div>
    </div>
  );
}

export default SignUpPage;
