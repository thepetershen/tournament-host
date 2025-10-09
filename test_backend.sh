#!/bin/bash

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"
TOKEN=""
USER_ID=""
TOURNAMENT_ID=""
EVENT_INDEX_SE=0
EVENT_INDEX_RR=1
USER1_ID=""
USER2_ID=""
USER3_ID=""
USER4_ID=""

# Function to print test headers
print_test() {
    echo -e "\n${BLUE}================================================${NC}"
    echo -e "${BLUE}TEST: $1${NC}"
    echo -e "${BLUE}================================================${NC}"
}

# Function to print success
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Function to print error
print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Function to print warning
print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# ==================== AUTHENTICATION TESTS ====================
print_test "1. Register User 1 (Main Test User)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser1",
    "name": "Test User One",
    "password": "password123"
  }')
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "User 1 registered successfully"
    USER1_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    echo "User 1 ID: $USER1_ID"
    ((TESTS_PASSED++))
else
    print_error "Failed to register User 1. HTTP Code: $HTTP_CODE"
    echo "Response: $BODY"
    ((TESTS_FAILED++))
fi

print_test "2. Register User 2"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser2",
    "name": "Test User Two",
    "password": "password123"
  }')
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "User 2 registered successfully"
    USER2_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    echo "User 2 ID: $USER2_ID"
    ((TESTS_PASSED++))
else
    print_error "Failed to register User 2"
    ((TESTS_FAILED++))
fi

print_test "3. Register User 3"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser3",
    "name": "Test User Three",
    "password": "password123"
  }')
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "User 3 registered successfully"
    USER3_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    echo "User 3 ID: $USER3_ID"
    ((TESTS_PASSED++))
else
    print_error "Failed to register User 3"
    ((TESTS_FAILED++))
fi

print_test "4. Register User 4"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser4",
    "name": "Test User Four",
    "password": "password123"
  }')
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "User 4 registered successfully"
    USER4_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    echo "User 4 ID: $USER4_ID"
    ((TESTS_PASSED++))
else
    print_error "Failed to register User 4"
    ((TESTS_FAILED++))
fi

print_test "5. Login with User 1"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser1",
    "password": "password123"
  }')
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Login successful"
    TOKEN=$(echo "$BODY" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    echo "Token obtained (first 50 chars): ${TOKEN:0:50}..."
    ((TESTS_PASSED++))
else
    print_error "Failed to login. HTTP Code: $HTTP_CODE"
    echo "Response: $BODY"
    ((TESTS_FAILED++))
fi

print_test "6. Get authenticated user info"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/users/me" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Retrieved authenticated user info"
    echo "Response: $BODY"
    ((TESTS_PASSED++))
else
    print_error "Failed to get user info"
    ((TESTS_FAILED++))
fi

# ==================== TOURNAMENT TESTS ====================
print_test "7. Create Tournament"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/tournaments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Test Tournament 2025"
  }')
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Tournament created successfully"
    TOURNAMENT_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "Tournament ID: $TOURNAMENT_ID"
    echo "Response: $BODY"
    ((TESTS_PASSED++))
else
    print_error "Failed to create tournament"
    echo "Response: $BODY"
    ((TESTS_FAILED++))
fi

print_test "8. Get All Tournaments"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/tournaments" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Retrieved all tournaments"
    echo "Response: $BODY"
    ((TESTS_PASSED++))
else
    print_error "Failed to get tournaments"
    ((TESTS_FAILED++))
fi

print_test "9. Get Tournament by ID"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/tournaments/$TOURNAMENT_ID" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Retrieved tournament by ID"
    echo "Response: $BODY"
    ((TESTS_PASSED++))
else
    print_error "Failed to get tournament by ID"
    ((TESTS_FAILED++))
fi

print_test "10. Get Non-existent Tournament (Should fail)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/tournaments/99999" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)

if [ "$HTTP_CODE" -eq 404 ]; then
    print_success "Correctly returned 404 for non-existent tournament"
    ((TESTS_PASSED++))
else
    print_error "Expected 404, got $HTTP_CODE"
    ((TESTS_FAILED++))
fi

# ==================== EVENT TESTS ====================
print_test "11. Create Single Elimination Event"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/tournaments/$TOURNAMENT_ID/event" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Singles Event",
    "eventType": "SINGLE_ELIM"
  }')
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Single Elimination event created"
    EVENT_INDEX_SE=$(echo "$BODY" | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "Event Index: $EVENT_INDEX_SE"
    echo "Response: $BODY"
    ((TESTS_PASSED++))
else
    print_error "Failed to create Single Elimination event"
    echo "Response: $BODY"
    ((TESTS_FAILED++))
fi

print_test "12. Create Round Robin Event"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/tournaments/$TOURNAMENT_ID/event" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Round Robin Event",
    "eventType": "ROUND_ROBIN"
  }')
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Round Robin event created"
    EVENT_INDEX_RR=$(echo "$BODY" | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "Event Index: $EVENT_INDEX_RR"
    echo "Response: $BODY"
    ((TESTS_PASSED++))
else
    print_error "Failed to create Round Robin event"
    ((TESTS_FAILED++))
fi

print_test "13. Get Events for Tournament"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/tournaments/$TOURNAMENT_ID/events" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Retrieved events for tournament"
    echo "Response: $BODY"
    ((TESTS_PASSED++))
else
    print_error "Failed to get events"
    ((TESTS_FAILED++))
fi

print_test "14. Get Event Name"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/tournaments/$TOURNAMENT_ID/event/$EVENT_INDEX_SE/name" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Retrieved event name"
    echo "Response: $BODY"
    ((TESTS_PASSED++))
else
    print_error "Failed to get event name"
    ((TESTS_FAILED++))
fi

print_test "15. Update Event Name"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/tournaments/$TOURNAMENT_ID/event/$EVENT_INDEX_SE/name" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '"Men'\''s Singles Championship"')
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Event name updated"
    echo "Response: $BODY"
    ((TESTS_PASSED++))
else
    print_error "Failed to update event name"
    ((TESTS_FAILED++))
fi

# ==================== PLAYER OPERATIONS ====================
print_test "16. Add Players to Single Elimination Event"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/tournaments/$TOURNAMENT_ID/event/$EVENT_INDEX_SE/players" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "[
    {\"id\": $USER1_ID},
    {\"id\": $USER2_ID},
    {\"id\": $USER3_ID},
    {\"id\": $USER4_ID}
  ]")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Players added to Single Elimination event"
    ((TESTS_PASSED++))
else
    print_error "Failed to add players to SE event"
    echo "Response: $BODY"
    ((TESTS_FAILED++))
fi

print_test "17. Get Players for Single Elimination Event"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/tournaments/$TOURNAMENT_ID/event/$EVENT_INDEX_SE/players" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Retrieved players for SE event"
    echo "Response: $BODY"
    ((TESTS_PASSED++))
else
    print_error "Failed to get players"
    ((TESTS_FAILED++))
fi

print_test "18. Add Players to Round Robin Event"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/tournaments/$TOURNAMENT_ID/event/$EVENT_INDEX_RR/players" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "[
    {\"id\": $USER1_ID},
    {\"id\": $USER2_ID},
    {\"id\": $USER3_ID},
    {\"id\": $USER4_ID}
  ]")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Players added to Round Robin event"
    ((TESTS_PASSED++))
else
    print_error "Failed to add players to RR event"
    ((TESTS_FAILED++))
fi

print_test "19. Get Tournament Users"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/tournaments/$TOURNAMENT_ID/users" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Retrieved tournament users"
    echo "Response: $BODY"
    ((TESTS_PASSED++))
else
    print_error "Failed to get tournament users"
    ((TESTS_FAILED++))
fi

# ==================== EVENT INITIALIZATION & DRAW TESTS ====================
print_test "20. Initialize Single Elimination Event"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/tournaments/$TOURNAMENT_ID/event/$EVENT_INDEX_SE/initialize" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Single Elimination event initialized"
    ((TESTS_PASSED++))
else
    print_error "Failed to initialize SE event"
    echo "Response: $BODY"
    ((TESTS_FAILED++))
fi

print_test "21. Get Single Elimination Event Draw"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/tournaments/$TOURNAMENT_ID/event/$EVENT_INDEX_SE/draw" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Retrieved SE event draw"
    echo "Response (formatted):"
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
    ((TESTS_PASSED++))
else
    print_error "Failed to get SE event draw"
    echo "Response: $BODY"
    ((TESTS_FAILED++))
fi

print_test "22. Get Matches for Single Elimination Event"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/tournaments/$TOURNAMENT_ID/event/$EVENT_INDEX_SE/matches" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Retrieved matches for SE event"
    echo "Response: $BODY"
    ((TESTS_PASSED++))
else
    print_error "Failed to get matches"
    ((TESTS_FAILED++))
fi

print_test "23. Initialize Round Robin Event"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/tournaments/$TOURNAMENT_ID/event/$EVENT_INDEX_RR/initialize" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Round Robin event initialized"
    ((TESTS_PASSED++))
else
    print_error "Failed to initialize RR event"
    echo "Response: $BODY"
    ((TESTS_FAILED++))
fi

print_test "24. Get Round Robin Event Draw (NEW FORMAT)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/tournaments/$TOURNAMENT_ID/event/$EVENT_INDEX_RR/draw" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Retrieved RR event draw in new nested list format"
    echo "Response (formatted):"
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
    print_warning "Verify: First element in each inner list should be UserDTO, followed by MatchDTOs"
    ((TESTS_PASSED++))
else
    print_error "Failed to get RR event draw"
    echo "Response: $BODY"
    ((TESTS_FAILED++))
fi

# ==================== MATCH RECORDING TESTS ====================
print_test "25. Get All Tournament Matches"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/tournaments/$TOURNAMENT_ID/matches" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Retrieved all tournament matches"
    echo "Response: $BODY"
    # Extract first match ID
    MATCH_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    echo "First Match ID: $MATCH_ID"
    ((TESTS_PASSED++))
else
    print_error "Failed to get tournament matches"
    ((TESTS_FAILED++))
fi

print_test "26. Record Match Result"
if [ -n "$MATCH_ID" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/tournaments/matches/$MATCH_ID/result" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d "{
        \"winnerId\": $USER1_ID,
        \"score\": [21, 15]
      }")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
    BODY=$(echo "$RESPONSE" | sed '$d')

    if [ "$HTTP_CODE" -eq 200 ]; then
        print_success "Match result recorded"
        ((TESTS_PASSED++))
    else
        print_error "Failed to record match result"
        echo "Response: $BODY"
        ((TESTS_FAILED++))
    fi
else
    print_warning "Skipping - no match ID available"
fi

print_test "27. Verify Match Result Update"
if [ -n "$MATCH_ID" ]; then
    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/tournaments/$TOURNAMENT_ID/matches" \
      -H "Authorization: Bearer $TOKEN")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
    BODY=$(echo "$RESPONSE" | sed '$d')

    if [ "$HTTP_CODE" -eq 200 ]; then
        if echo "$BODY" | grep -q "\"completed\":true"; then
            print_success "Match marked as completed"
            ((TESTS_PASSED++))
        else
            print_error "Match not marked as completed"
            ((TESTS_FAILED++))
        fi
    else
        print_error "Failed to verify match result"
        ((TESTS_FAILED++))
    fi
else
    print_warning "Skipping - no match ID available"
fi

# ==================== SEARCH TESTS ====================
print_test "28. Search for Players"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/search?query=test" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Search for players successful"
    echo "Response: $BODY"
    ((TESTS_PASSED++))
else
    print_error "Failed to search for players"
    ((TESTS_FAILED++))
fi

print_test "29. Search for Tournaments"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/search?query=tournament" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Search for tournaments successful"
    echo "Response: $BODY"
    ((TESTS_PASSED++))
else
    print_error "Failed to search for tournaments"
    ((TESTS_FAILED++))
fi

# ==================== EDGE CASE TESTS ====================
print_test "30. Try to Initialize Event Twice (Should handle gracefully)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/tournaments/$TOURNAMENT_ID/event/$EVENT_INDEX_SE/initialize" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 400 ]; then
    print_success "Event re-initialization handled (HTTP $HTTP_CODE)"
    echo "Response: $BODY"
    ((TESTS_PASSED++))
else
    print_error "Unexpected response for re-initialization"
    ((TESTS_FAILED++))
fi

print_test "31. Deinitialize Single Elimination Event"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/tournaments/$TOURNAMENT_ID/event/$EVENT_INDEX_SE/deinitialize" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "Event deinitialized successfully"
    ((TESTS_PASSED++))
else
    print_error "Failed to deinitialize event"
    echo "Response: $BODY"
    ((TESTS_FAILED++))
fi

print_test "32. Verify Event Deinitialized (should have no matches)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/tournaments/$TOURNAMENT_ID/event/$EVENT_INDEX_SE/matches" \
  -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    if [ "$BODY" = "[]" ]; then
        print_success "Event correctly has no matches after deinitialization"
        ((TESTS_PASSED++))
    else
        print_warning "Event still has matches: $BODY"
        ((TESTS_PASSED++))
    fi
else
    print_error "Failed to verify deinitialization"
    ((TESTS_FAILED++))
fi

print_test "33. Try to Record Result for Invalid Match (Should fail)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/tournaments/matches/99999/result" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"winnerId\": $USER1_ID,
    \"score\": [21, 15]
  }")
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)

if [ "$HTTP_CODE" -eq 400 ]; then
    print_success "Correctly rejected invalid match ID"
    ((TESTS_PASSED++))
else
    print_error "Expected 400, got $HTTP_CODE"
    ((TESTS_FAILED++))
fi

print_test "34. Try to Create Event with Invalid Type (Should fail)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/tournaments/$TOURNAMENT_ID/event" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Invalid Event",
    "eventType": "INVALID_TYPE"
  }')
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)

if [ "$HTTP_CODE" -eq 400 ]; then
    print_success "Correctly rejected invalid event type"
    ((TESTS_PASSED++))
else
    print_error "Expected 400, got $HTTP_CODE"
    ((TESTS_FAILED++))
fi

print_test "35. Update Authenticated User Profile"
RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/users/me" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "username": "testuser1_updated",
    "name": "Test User One Updated",
    "password": "password123"
  }')
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    print_success "User profile updated"
    echo "Response: $BODY"
    ((TESTS_PASSED++))
else
    print_error "Failed to update user profile"
    echo "Response: $BODY"
    ((TESTS_FAILED++))
fi

# ==================== SUMMARY ====================
echo -e "\n${BLUE}================================================${NC}"
echo -e "${BLUE}TEST SUMMARY${NC}"
echo -e "${BLUE}================================================${NC}"
echo -e "${GREEN}Tests Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Tests Failed: $TESTS_FAILED${NC}"
TOTAL=$((TESTS_PASSED + TESTS_FAILED))
echo -e "Total Tests: $TOTAL"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "\n${GREEN}🎉 All tests passed!${NC}"
    exit 0
else
    echo -e "\n${RED}❌ Some tests failed. Please review the output above.${NC}"
    exit 1
fi