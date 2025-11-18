# Event Registration System - API Documentation

## Overview

The event registration system allows players to sign up for events and tournament moderators to approve/reject those registrations. This creates a controlled approval workflow for event participation.

## Flow

```
1. Player signs up for event → Registration created with PENDING status
2. Moderator views pending registrations
3. Moderator approves selected registrations → Players added to event
4. Event can be initialized with approved players
```

---

## API Endpoints

### 1. Sign Up for Event (Player)

**Any authenticated user can sign up for an event**

```http
POST /api/tournaments/{tournamentId}/event/{eventIndex}/signup
```

**Headers:**
```
Authorization: Bearer {jwt_token}
```

**Example Request:**
```bash
curl -X POST http://localhost:8080/api/tournaments/1/event/0/signup \
  -H "Authorization: Bearer eyJhbGc..."
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "eventId": 5,
  "eventName": "Championship Singles",
  "user": {
    "id": 123,
    "username": "player1@example.com",
    "name": "John Doe"
  },
  "status": "PENDING",
  "registeredAt": "2025-10-20T18:30:00.000+00:00",
  "reviewedAt": null,
  "reviewedBy": null
}
```

**Error Responses:**
```json
// Already signed up
{
  "error": "You have already signed up for this event"
}

// Already a player
{
  "error": "You are already a player in this event"
}

// Event already initialized
{
  "error": "Cannot sign up for an event that has already been initialized"
}
```

---

### 2. Cancel Registration (Player)

**Cancel your own pending registration**

```http
DELETE /api/tournaments/{tournamentId}/event/{eventIndex}/signup
```

**Headers:**
```
Authorization: Bearer {jwt_token}
```

**Example:**
```bash
curl -X DELETE http://localhost:8080/api/tournaments/1/event/0/signup \
  -H "Authorization: Bearer eyJhbGc..."
```

**Success Response (200 OK):**
```json
{}
```

**Error Responses:**
```json
// Registration not found
{
  "error": "Registration not found"
}

// Already approved/rejected
{
  "error": "Can only cancel pending registrations"
}
```

---

### 3. View All Registrations (Moderator)

**View all registrations for an event (pending, approved, rejected)**

```http
GET /api/tournaments/{tournamentId}/event/{eventIndex}/registrations
```

**Requires:** Tournament owner or authorized editor

**Example:**
```bash
curl http://localhost:8080/api/tournaments/1/event/0/registrations \
  -H "Authorization: Bearer eyJhbGc..."
```

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "eventId": 5,
    "eventName": "Championship Singles",
    "user": {
      "id": 123,
      "username": "player1@example.com",
      "name": "John Doe"
    },
    "status": "PENDING",
    "registeredAt": "2025-10-20T18:30:00.000+00:00",
    "reviewedAt": null,
    "reviewedBy": null
  },
  {
    "id": 2,
    "eventId": 5,
    "eventName": "Championship Singles",
    "user": {
      "id": 124,
      "username": "player2@example.com",
      "name": "Jane Smith"
    },
    "status": "APPROVED",
    "registeredAt": "2025-10-20T18:25:00.000+00:00",
    "reviewedAt": "2025-10-20T18:35:00.000+00:00",
    "reviewedBy": {
      "id": 100,
      "username": "admin@example.com",
      "name": "Admin"
    }
  }
]
```

---

### 4. View Pending Registrations (Moderator)

**View only pending registrations (not yet approved/rejected)**

```http
GET /api/tournaments/{tournamentId}/event/{eventIndex}/registrations/pending
```

**Requires:** Tournament owner or authorized editor

**Example:**
```bash
curl http://localhost:8080/api/tournaments/1/event/0/registrations/pending \
  -H "Authorization: Bearer eyJhbGc..."
```

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "eventId": 5,
    "eventName": "Championship Singles",
    "user": {
      "id": 123,
      "username": "player1@example.com",
      "name": "John Doe"
    },
    "status": "PENDING",
    "registeredAt": "2025-10-20T18:30:00.000+00:00",
    "reviewedAt": null,
    "reviewedBy": null
  },
  {
    "id": 3,
    "eventId": 5,
    "eventName": "Championship Singles",
    "user": {
      "id": 125,
      "username": "player3@example.com",
      "name": "Bob Johnson"
    },
    "status": "PENDING",
    "registeredAt": "2025-10-20T18:32:00.000+00:00",
    "reviewedAt": null,
    "reviewedBy": null
  }
]
```

---

### 5. Approve Registrations (Moderator)

**Approve multiple registrations at once**

```http
POST /api/tournaments/{tournamentId}/event/{eventIndex}/registrations/approve
```

**Requires:** Tournament owner or authorized editor

**Request Body:**
```json
{
  "registrationIds": [1, 3, 5]
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/tournaments/1/event/0/registrations/approve \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json" \
  -d '{
    "registrationIds": [1, 3, 5]
  }'
```

**Success Response (200 OK):**
```json
{}
```

**What happens:**
1. Each registration's status is changed to `APPROVED`
2. `reviewedAt` is set to current timestamp
3. `reviewedBy` is set to the current user
4. Players are automatically added to the event

**Error Responses:**
```json
// Event already initialized
{
  "error": "Cannot approve registrations after event has been initialized"
}

// Registration doesn't belong to this event
{
  "error": "Registration 5 does not belong to this event"
}

// Registration not found
{
  "error": "Registration not found: 99"
}
```

---

### 6. Reject Registration (Moderator)

**Reject a single registration**

```http
POST /api/tournaments/{tournamentId}/event/{eventIndex}/registrations/{registrationId}/reject
```

**Requires:** Tournament owner or authorized editor

**Example:**
```bash
curl -X POST http://localhost:8080/api/tournaments/1/event/0/registrations/2/reject \
  -H "Authorization: Bearer eyJhbGc..."
```

**Success Response (200 OK):**
```json
{}
```

**What happens:**
1. Registration status is changed to `REJECTED`
2. `reviewedAt` is set to current timestamp
3. `reviewedBy` is set to the current user
4. Player is NOT added to the event

---

## Registration Statuses

| Status | Description |
|--------|-------------|
| `PENDING` | Player has signed up, waiting for moderator approval |
| `APPROVED` | Moderator approved the registration, player added to event |
| `REJECTED` | Moderator rejected the registration, player NOT added |

---

## Use Cases

### Use Case 1: Player Signs Up

```javascript
// Frontend code
async function signUpForEvent(tournamentId, eventIndex) {
  const response = await fetch(
    `http://localhost:8080/api/tournaments/${tournamentId}/event/${eventIndex}/signup`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`
      }
    }
  );

  if (response.ok) {
    alert('Successfully signed up! Waiting for approval.');
  } else {
    const error = await response.text();
    alert(error);
  }
}
```

### Use Case 2: Moderator Approves All Pending

```javascript
// Frontend code
async function approvePendingRegistrations(tournamentId, eventIndex) {
  // 1. Get pending registrations
  const pendingResponse = await fetch(
    `http://localhost:8080/api/tournaments/${tournamentId}/event/${eventIndex}/registrations/pending`,
    {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`
      }
    }
  );

  const pending = await pendingResponse.json();
  const registrationIds = pending.map(r => r.id);

  // 2. Approve all
  await fetch(
    `http://localhost:8080/api/tournaments/${tournamentId}/event/${eventIndex}/registrations/approve`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ registrationIds })
    }
  );

  alert(`Approved ${registrationIds.length} players!`);
}
```

### Use Case 3: Moderator Selectively Approves

```javascript
// Frontend code - user selects which registrations to approve
async function approveSelectedRegistrations(tournamentId, eventIndex, selectedIds) {
  await fetch(
    `http://localhost:8080/api/tournaments/${tournamentId}/event/${eventIndex}/registrations/approve`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        registrationIds: selectedIds // [1, 3, 5] - only approve these
      })
    }
  );
}
```

---

## Validation Rules

✅ **Players can sign up if:**
- Event is NOT initialized yet
- Player hasn't already signed up
- Player is not already a player in the event

✅ **Moderators can approve if:**
- They own the tournament OR are authorized editors
- Event is NOT initialized yet
- Registration belongs to the event

✅ **Players can cancel if:**
- Registration status is PENDING (not yet reviewed)

---

## Database Schema

### `event_registrations` Table

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key |
| `event_id` | BIGINT | Foreign key to events |
| `user_id` | BIGINT | Foreign key to users (the player) |
| `status` | VARCHAR | PENDING, APPROVED, or REJECTED |
| `registered_at` | TIMESTAMP | When player signed up |
| `reviewed_at` | TIMESTAMP | When moderator reviewed (nullable) |
| `reviewed_by` | BIGINT | Foreign key to users (the moderator, nullable) |

---

## Complete Workflow Example

```bash
# 1. Player signs up
curl -X POST http://localhost:8080/api/tournaments/1/event/0/signup \
  -H "Authorization: Bearer player_token"

# 2. Moderator views pending registrations
curl http://localhost:8080/api/tournaments/1/event/0/registrations/pending \
  -H "Authorization: Bearer moderator_token"

# Response:
# [
#   {"id": 1, "user": {"name": "John"}, "status": "PENDING"},
#   {"id": 2, "user": {"name": "Jane"}, "status": "PENDING"}
# ]

# 3. Moderator approves both
curl -X POST http://localhost:8080/api/tournaments/1/event/0/registrations/approve \
  -H "Authorization: Bearer moderator_token" \
  -H "Content-Type: application/json" \
  -d '{"registrationIds": [1, 2]}'

# 4. Check players are now in the event
curl http://localhost:8080/api/tournaments/1/event/0/players \
  -H "Authorization: Bearer moderator_token"

# Response:
# [
#   {"id": 123, "name": "John"},
#   {"id": 124, "name": "Jane"}
# ]

# 5. Initialize event
curl -X POST http://localhost:8080/api/tournaments/1/event/0/initialize \
  -H "Authorization: Bearer moderator_token"
```

---

## Notes

- **Public events:** Anyone authenticated can sign up
- **Private tournaments:** Still controlled by moderator approval
- **After initialization:** No more signups or approvals allowed
- **Bulk operations:** Approve multiple registrations at once for efficiency
- **Audit trail:** Track who approved and when
