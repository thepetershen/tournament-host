# Implementation Guide for Unused Endpoints

## Summary
This document provides specific code changes needed to implement all 16 unused backend endpoints.

---

## PHASE 1: USER PROFILE ✅ COMPLETED

### Files Created:
- `tournamentFrontend/src/Pages/Profile/ProfilePage.jsx`
- `tournamentFrontend/src/Pages/Profile/ProfilePage.module.css`

### Files Modified:
- `tournamentFrontend/src/main.jsx` - Added profile route
- `tournamentFrontend/src/Components/TopBar/TopBar.jsx` - Added profile link
- `tournamentFrontend/src/Components/TopBar/TopBar.module.css` - Added profile link styling

---

## PHASE 2: TOURNAMENT MANAGEMENT

### 2.1 Tournament Details Editor

**Backend Endpoint:** `PUT /api/tournaments/{id}/details`
**Request Body:** `{ message, begin, end, location }`

**File to Modify:** `tournamentFrontend/src/Pages/TournamentControl/TournamentControl.jsx`

**Changes Needed:**

1. Add state for edit modal:
```javascript
const [showEditTournamentModal, setShowEditTournamentModal] = useState(false);
const [tournamentEditData, setTournamentEditData] = useState({
  message: '',
  begin: '',
  end: '',
  location: ''
});
```

2. Add function to open edit modal (populate with existing data):
```javascript
const handleOpenEditTournament = () => {
  setTournamentEditData({
    message: tournament.message || '',
    begin: tournament.begin ? new Date(tournament.begin).toISOString().split('T')[0] : '',
    end: tournament.end ? new Date(tournament.end).toISOString().split('T')[0] : '',
    location: tournament.location || ''
  });
  setShowEditTournamentModal(true);
};
```

3. Add function to save tournament details:
```javascript
const handleSaveTournamentDetails = async () => {
  try {
    const updateData = {
      message: tournamentEditData.message,
      begin: tournamentEditData.begin ? new Date(tournamentEditData.begin) : null,
      end: tournamentEditData.end ? new Date(tournamentEditData.end) : null,
      location: tournamentEditData.location
    };

    await authAxios.put(`/api/tournaments/${tournamentId}/details`, updateData);
    showMessage('success', 'Tournament details updated successfully');
    setShowEditTournamentModal(false);

    // Refresh tournament data
    const response = await authAxios.get(`/api/tournaments/${tournamentId}`);
    setTournament(response.data);
  } catch (err) {
    showMessage('error', err.response?.data || 'Failed to update tournament details');
  }
};
```

4. Add "Edit Tournament Details" button in overview section (around line 1500-1600):
```jsx
<button onClick={handleOpenEditTournament} className={styles.button}>
  Edit Tournament Details
</button>
```

5. Add modal JSX (add near other modals, around line 1700+):
```jsx
{showEditTournamentModal && (
  <div className={styles.modalOverlay} onClick={() => setShowEditTournamentModal(false)}>
    <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
      <h3>Edit Tournament Details</h3>

      <div className={styles.formGroup}>
        <label>Message/Description</label>
        <textarea
          value={tournamentEditData.message}
          onChange={(e) => setTournamentEditData(prev => ({...prev, message: e.target.value}))}
          placeholder="Tournament description..."
          rows="4"
        />
      </div>

      <div className={styles.formGroup}>
        <label>Start Date</label>
        <input
          type="date"
          value={tournamentEditData.begin}
          onChange={(e) => setTournamentEditData(prev => ({...prev, begin: e.target.value}))}
        />
      </div>

      <div className={styles.formGroup}>
        <label>End Date</label>
        <input
          type="date"
          value={tournamentEditData.end}
          onChange={(e) => setTournamentEditData(prev => ({...prev, end: e.target.value}))}
        />
      </div>

      <div className={styles.formGroup}>
        <label>Location</label>
        <input
          type="text"
          value={tournamentEditData.location}
          onChange={(e) => setTournamentEditData(prev => ({...prev, location: e.target.value}))}
          placeholder="Tournament location"
        />
      </div>

      <div className={styles.modalActions}>
        <button onClick={handleSaveTournamentDetails} className={styles.saveButton}>
          Save Changes
        </button>
        <button onClick={() => setShowEditTournamentModal(false)} className={styles.cancelButton}>
          Cancel
        </button>
      </div>
    </div>
  </div>
)}
```

---

### 2.2 Event Name Editor

**Backend Endpoint:** `PUT /api/tournaments/{tournamentId}/event/{eventIndex}/name`
**Request Body:** `{ name: string }`

**File to Modify:** `tournamentFrontend/src/Pages/TournamentControl/TournamentControl.jsx`

**Changes Needed:**

1. Add state:
```javascript
const [editingEventName, setEditingEventName] = useState(null); // eventIndex being edited
const [editEventNameValue, setEditEventNameValue] = useState('');
```

2. Add function to save event name:
```javascript
const handleSaveEventName = async (eventIndex) => {
  try {
    await authAxios.put(`/api/tournaments/${tournamentId}/event/${eventIndex}/name`, {
      name: editEventNameValue
    });
    showMessage('success', 'Event name updated');
    setEditingEventName(null);

    // Refresh events
    const response = await authAxios.get(`/api/tournaments/${tournamentId}/events`);
    setEvents(response.data);
  } catch (err) {
    showMessage('error', 'Failed to update event name');
  }
};
```

3. Modify event display to include inline editing (find where events are rendered):
```jsx
{events.map((event, index) => (
  <div key={index}>
    {editingEventName === index ? (
      <div style={{display: 'flex', gap: '8px', alignItems: 'center'}}>
        <input
          type="text"
          value={editEventNameValue}
          onChange={(e) => setEditEventNameValue(e.target.value)}
          autoFocus
        />
        <button onClick={() => handleSaveEventName(index)}>Save</button>
        <button onClick={() => setEditingEventName(null)}>Cancel</button>
      </div>
    ) : (
      <div style={{display: 'flex', gap: '8px', alignItems: 'center'}}>
        <span>{event.name}</span>
        <button
          onClick={() => {
            setEditingEventName(index);
            setEditEventNameValue(event.name);
          }}
          style={{fontSize: '12px', padding: '2px 8px'}}
        >
          ✏️ Edit
        </button>
      </div>
    )}
  </div>
))}
```

---

Continues for all other phases...

## Testing Checklist

- [ ] Profile page loads and displays user data
- [ ] Profile can be updated (name, username)
- [ ] Password can be changed
- [ ] Tournament details can be edited
- [ ] Event names can be changed
- [ ] Tournament owner is displayed
- [ ] Users can cancel event registration
- [ ] Leagues can be renamed
- [ ] Leagues can be deleted
- [ ] League players list displays
- [ ] League statistics show correctly
- [ ] Individual player rankings work
- [ ] Auto-seeding from league works
- [ ] Auto-seeding teams works
- [ ] Event points display correctly
- [ ] Cumulative points show
- [ ] Points distributions overview works

