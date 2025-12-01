# Soft Delete Implementation Plan for Tournaments

## Overview
Implement soft delete functionality to mark tournaments as deleted without removing them from the database. This preserves historical data while hiding deleted tournaments from users.

## Current State Analysis

### Existing Architecture
1. **Tournament Model** (`Tournament.java`)
   - No soft delete fields currently exist
   - Uses standard JPA `@Entity` annotations
   - Has relationships: owner, authorizedEditors, events

2. **Tournament Repository** (`TournamentRepository.java`)
   - Uses Spring Data `CrudRepository`
   - Custom queries for name-based searches
   - Query: `findByNameContainingIgnoreCaseAndSpaces`
   - Query: `findAllTournamentsWithPlayer`

3. **Tournament Service** (`TournamentService.java`)
   - `getTournament(Long id)` - fetches by ID using `findById()`
   - `getAllTournaments()` - fetches all using `findAll()`
   - `getActiveTournaments(int limit)` - fetches active tournaments
   - Hard delete method exists at line 3235-3317: `removeTournament()`

4. **Tournament Controller** (`TournamentController.java`)
   - `GET /api/tournaments` - calls `getActiveTournaments(limit)`
   - `GET /api/tournaments/{tournamentId}` - calls `getTournament(id)`
   - `DELETE /api/tournaments/{tournamentId}` - calls `removeTournament(id)`

5. **Search Controller** (`SearchController.java`)
   - `GET /api/search` - uses `findByNameContainingIgnoreCaseAndSpaces`

6. **Frontend Usage**
   - HomePage fetches: `/api/tournaments?limit=6`
   - TournamentPage fetches: `/api/tournaments/{id}`
   - Search uses: `/api/search?query=...`

### Hard Delete vs Soft Delete
**Current (Hard Delete):**
- Permanently removes tournament and all related data from database
- Cannot be recovered
- May break historical references

**Proposed (Soft Delete):**
- Marks tournament as deleted with flag
- Preserves all data in database
- Can be filtered from queries
- Allows potential restoration
- Better for auditing and analytics

## Implementation Plan

### Phase 1: Database Schema Changes

**1.1 Add soft delete fields to Tournament entity**
- File: `Tournament.java`
- Add fields:
  ```java
  private boolean deleted = false;

  @Column(name = "deleted_at")
  private Date deletedAt;
  ```
- Add getters/setters:
  ```java
  public boolean isDeleted() { return deleted; }
  public void setDeleted(boolean deleted) { this.deleted = deleted; }
  public Date getDeletedAt() { return deletedAt; }
  public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }
  ```

**Database migration:** Hibernate auto-DDL will add columns with default values (false, null)

### Phase 2: Repository Layer Updates

**2.1 Update TournamentRepository with soft delete aware queries**
- File: `TournamentRepository.java`
- Add methods to exclude deleted tournaments:
  ```java
  // Find all non-deleted tournaments
  List<Tournament> findByDeletedFalse();

  // Find by ID excluding deleted
  Optional<Tournament> findByIdAndDeletedFalse(Long id);

  // Update search to exclude deleted
  @Query("SELECT t FROM Tournament t WHERE t.deleted = false AND " +
         "LOWER(REPLACE(t.name, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:name, ' ', ''), '%'))")
  List<Tournament> findByNameContainingIgnoreCaseAndSpacesAndNotDeleted(@Param("name") String name, Pageable pageable);

  // Update player tournaments query
  @Query("SELECT DISTINCT t FROM Tournament t JOIN t.events e JOIN e.players p " +
         "WHERE p = :player AND t.deleted = false")
  List<Tournament> findAllTournamentsWithPlayerAndNotDeleted(@Param("player") User player);
  ```

### Phase 3: Service Layer Updates

**3.1 Update TournamentService methods**
- File: `TournamentService.java`

**Change `getAllTournaments()` (line ~114):**
```java
public List<Tournament> getAllTournaments() {
    return tournamentRepo.findByDeletedFalse(); // Exclude deleted
}
```

**Change `getTournament(Long id)` (line ~221):**
```java
public Tournament getTournament(Long id) {
    return tournamentRepo.findByIdAndDeletedFalse(id)
        .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
}
```

**Add new `getTournamentIncludingDeleted()` method:**
```java
// For internal use when we need to access deleted tournaments
private Tournament getTournamentIncludingDeleted(Long id) {
    return tournamentRepo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
}
```

**3.2 Replace hard delete with soft delete**
- Rename existing `removeTournament()` to `hardDeleteTournament()` (keep for admin use)
- Create new `removeTournament()` method:
```java
@Transactional
public void removeTournament(Long tournamentId) {
    Tournament tournament = getTournamentIncludingDeleted(tournamentId);

    if (tournament.isDeleted()) {
        throw new IllegalArgumentException("Tournament is already deleted");
    }

    tournament.setDeleted(true);
    tournament.setDeletedAt(new Date());
    tournamentRepo.save(tournament);
}
```

**3.3 Optional: Add restore functionality**
```java
@Transactional
public void restoreTournament(Long tournamentId) {
    Tournament tournament = getTournamentIncludingDeleted(tournamentId);

    if (!tournament.isDeleted()) {
        throw new IllegalArgumentException("Tournament is not deleted");
    }

    tournament.setDeleted(false);
    tournament.setDeletedAt(null);
    tournamentRepo.save(tournament);
}
```

### Phase 4: Update All Service Methods Using Tournament Queries

**4.1 Update methods that fetch tournaments:**
- `getAllPlayers()` - already uses `getTournament()`, will auto-filter
- `getAllMatches()` - uses `findById()`, change to use `getTournament()`
- `getPlayerMatchesForTournament()` - uses `findById()`, change to use `getTournament()`
- `createEvent()` - uses `findById()`, change to use `getTournament()`
- `getEventsForTournament()` - uses `findById()`, change to use `getTournament()`

**Strategy:** Replace all direct `tournamentRepo.findById()` calls with `getTournament()` helper method to ensure consistent soft delete filtering.

### Phase 5: Search Controller Update

**5.1 Update SearchController**
- File: `SearchController.java` (line 44)
- Change from `findByNameContainingIgnoreCaseAndSpaces` to `findByNameContainingIgnoreCaseAndSpacesAndNotDeleted`

### Phase 6: Update UserService (Player Tournaments)

**6.1 Update UserService**
- File: `UserService.java` (line ~70)
- Change from `findAllTournamentsWithPlayer` to `findAllTournamentsWithPlayerAndNotDeleted`

### Phase 7: League Service Update

**7.1 Update methods that filter tournaments in leagues**
- Need to ensure league tournament lists exclude deleted tournaments
- May need to update `getLeagueTournaments()` to filter out deleted tournaments from the result set

### Phase 8: Controller Layer (No Changes Needed)

**8.1 TournamentController**
- `DELETE /api/tournaments/{tournamentId}` - no changes needed, still calls `removeTournament()`
- The behavior changes from hard delete to soft delete automatically

**8.2 Optional: Add restore endpoint**
```java
@PostMapping("/{tournamentId}/restore")
public ResponseEntity<?> restoreTournament(@PathVariable Long tournamentId) {
    try {
        User currentUser = getCurrentUser();
        tournamentService.verifyEditPermission(tournamentId, currentUser);
        tournamentService.restoreTournament(tournamentId);
        return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
```

### Phase 9: Frontend (No Changes Required)

**9.1 Existing functionality works automatically:**
- Delete button in TournamentControl.jsx continues to work
- API still calls `DELETE /api/tournaments/{id}`
- Backend now soft deletes instead of hard deletes
- Users won't see deleted tournaments in listings or searches

**9.2 Optional: Add restore UI (future enhancement)**
- Add admin panel to view deleted tournaments
- Add restore button for tournament owners

## Migration Strategy

### Backward Compatibility
1. **Existing data:** All existing tournaments will have `deleted = false` by default
2. **Existing code:** Will continue to work, just returns filtered results
3. **Zero downtime:** Changes are additive, no breaking changes

### Testing Strategy
1. **Unit tests:** Test soft delete on new tournament
2. **Integration tests:** Verify deleted tournaments don't appear in:
   - `/api/tournaments` endpoint
   - `/api/search` endpoint
   - Individual tournament fetch (404 expected)
3. **Manual testing:**
   - Create tournament → Delete → Verify not in list
   - Try to access deleted tournament by ID → Should get 404
   - Verify hard delete still works for data cleanup if needed

## Rollback Plan
If issues arise, can temporarily disable soft delete filtering by:
1. Reverting repository queries to original versions
2. Keeping `deleted` flag in database for future use
3. No data loss as we're only adding fields, not removing

## Benefits of This Approach

1. **Data Preservation:** All tournament data remains in database
2. **Referential Integrity:** Relationships remain intact
3. **Auditability:** Can track when tournaments were deleted
4. **Restoration:** Can undelete if needed
5. **Analytics:** Can analyze deleted tournaments
6. **Minimal Code Changes:** Most changes are in repository layer
7. **No Frontend Changes:** Works with existing UI
8. **Backward Compatible:** Existing data continues to work

## Files to Modify

1. `Tournament.java` - Add deleted fields
2. `TournamentRepository.java` - Add soft delete aware queries
3. `TournamentService.java` - Update methods to use soft delete
4. `SearchController.java` - Update search query
5. `UserService.java` - Update player tournament query

## Estimated Impact

- **Backend files modified:** 5
- **Frontend files modified:** 0
- **Database migrations:** Automatic via Hibernate
- **Breaking changes:** None
- **API contract changes:** None (same endpoints, same behavior from user perspective)
