import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import authAxios from "../../utils/authAxios";
import publicAxios from "../../utils/publicAxios";
import PlayerLink from "../../Components/PlayerLink/PlayerLink";
import { useAuth } from "../../contexts/AuthContext";
import styles from "./SignUpPage.module.css";

function SignUpPage() {
  const { tournamentId } = useParams();
  const navigate = useNavigate();
  const { token } = useAuth();
  const [tournament, setTournament] = useState(null);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [signupStatus, setSignupStatus] = useState({});
  const [message, setMessage] = useState("");
  const [partnerInputs, setPartnerInputs] = useState({});
  const [showGuestModal, setShowGuestModal] = useState(false);
  const [guestName, setGuestName] = useState("");
  const [selectedEventForGuest, setSelectedEventForGuest] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        // Fetch tournament general info (public data)
        const tournamentResponse = await publicAxios.get(`/api/tournaments/${tournamentId}`);
        setTournament(tournamentResponse.data);

        // Fetch events (public data)
        const eventsResponse = await publicAxios.get(`/api/tournaments/${tournamentId}/events`);
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
    // Check if user is authenticated
    if (!token) {
      // Show guest signup modal
      setSelectedEventForGuest(eventIndex);
      setShowGuestModal(true);
      return;
    }

    try {
      setMessage("");
      const event = events.find(e => e.id === eventIndex);
      const requestBody = event?.matchType === 'DOUBLES' && partnerInputs[eventIndex]
        ? { desiredPartner: partnerInputs[eventIndex] }
        : {};

      await authAxios.post(`/api/tournaments/${tournamentId}/event/${eventIndex}/signup`, requestBody);

      setSignupStatus({
        ...signupStatus,
        [eventIndex]: "success"
      });
      setMessage(`Successfully signed up for ${event?.name || 'event'}!`);
    } catch (err) {
      const errorMsg = err.response?.data || "Failed to sign up";
      setSignupStatus({
        ...signupStatus,
        [eventIndex]: "error"
      });
      setMessage(errorMsg);
    }
  };

  const handleGuestSignUp = async () => {
    if (!guestName.trim()) {
      setMessage("Please enter your full name");
      return;
    }

    try {
      setMessage("");
      const event = events.find(e => e.id === selectedEventForGuest);
      const requestBody = {
        fullName: guestName.trim(),
        desiredPartner: event?.matchType === 'DOUBLES' && partnerInputs[selectedEventForGuest]
          ? partnerInputs[selectedEventForGuest]
          : null
      };

      await publicAxios.post(
        `/api/tournaments/${tournamentId}/event/${selectedEventForGuest}/signup/guest`,
        requestBody
      );

      setSignupStatus({
        ...signupStatus,
        [selectedEventForGuest]: "success"
      });
      setMessage(`Successfully signed up for ${event?.name || 'event'} as a guest!`);
      setShowGuestModal(false);
      setGuestName("");
      setSelectedEventForGuest(null);
    } catch (err) {
      const errorMsg = err.response?.data || "Failed to sign up as guest";
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
                <span><PlayerLink player={tournament.owner} /></span>
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
        ) : events.filter(event => !event.initialized).length === 0 ? (
          <div className={styles.emptyState}>All events have been initialized. No events are currently accepting new signups.</div>
        ) : (
          <div>
            <p className={styles.instructions}>
              Select the events you would like to participate in. Your registration will be pending approval from the tournament organizers.
            </p>
            <ul className={styles.eventsList}>
              {events.filter(event => !event.initialized).map(event => (
                <li key={event.id} className={styles.eventCard}>
                  <div className={styles.eventInfo}>
                    <h3 className={styles.eventName}>
                      {event.name}
                      {event.matchType === 'DOUBLES' && (
                        <span className={styles.eventBadge}>Doubles</span>
                      )}
                    </h3>
                    {signupStatus[event.id] === "success" && (
                      <p className={styles.eventStatus}>Registration pending approval</p>
                    )}
                    {event.matchType === 'DOUBLES' && signupStatus[event.id] !== "success" && (
                      <div className={styles.partnerInput}>
                        <label className={styles.partnerLabel}>
                          Desired Partner (username):
                        </label>
                        <input
                          type="text"
                          placeholder="Enter partner's username (optional)"
                          value={partnerInputs[event.id] || ''}
                          onChange={(e) => setPartnerInputs({
                            ...partnerInputs,
                            [event.id]: e.target.value
                          })}
                          className={styles.partnerInputField}
                        />
                        <p className={styles.partnerHint}>
                          The host will match you with your partner after signup
                        </p>
                      </div>
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

      {/* Guest Signup Modal */}
      {showGuestModal && (
        <div className={styles.modalOverlay} onClick={() => setShowGuestModal(false)}>
          <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <h2 className={styles.modalTitle}>Sign Up as Guest</h2>
            <div className={styles.warningBox}>
              <p><strong>⚠️ Warning:</strong> You are not logged in.</p>
              <p>
                You can sign up as a guest by providing your full name. This creates a one-time account that <strong>CANNOT be recovered</strong>.
              </p>
              <p>
                If you want to track your tournament history and manage your registrations, please <a href="/login" className={styles.link}>login</a> or <a href="/register" className={styles.link}>create an account</a> instead.
              </p>
            </div>
            <div className={styles.formGroup}>
              <label className={styles.label}>Full Name:</label>
              <input
                type="text"
                value={guestName}
                onChange={(e) => setGuestName(e.target.value)}
                placeholder="Enter your full name"
                className={styles.input}
                autoFocus
              />
            </div>
            <div className={styles.modalActions}>
              <button
                onClick={() => {
                  setShowGuestModal(false);
                  setGuestName("");
                  setSelectedEventForGuest(null);
                }}
                className={styles.cancelButton}
              >
                Cancel
              </button>
              <button
                onClick={handleGuestSignUp}
                className={styles.confirmButton}
              >
                Continue as Guest
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default SignUpPage;
