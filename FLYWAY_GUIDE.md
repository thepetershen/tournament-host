# 🚀 Flyway Database Migration Guide

## What Just Happened?

Your application now has **Flyway** - a professional database migration tool that tracks all database changes like Git tracks code changes.

## 📊 Current Setup Status

✅ **Flyway dependencies added** to pom.xml
✅ **Configuration set** in application.properties
✅ **Migration directory created** at `src/main/resources/db/migration/`
✅ **Baseline migration** (V1) created for existing database
✅ **Example migrations** (V2, V3) created to show how it works

## 🎯 How Flyway Works (Simple Explanation)

### Before (Your Old Setup):
```
1. You change Java code (add new @Column)
2. Restart app
3. Hibernate tries to auto-update database
4. Hope it works! 🤞
```

### Now (With Flyway):
```
1. You write a SQL migration file (V2__Add_new_column.sql)
2. Commit file to Git
3. Deploy to production
4. Flyway automatically runs the migration
5. Flyway marks it as "done" in flyway_schema_history table
6. Same migration won't run twice ✅
```

## 📝 The Migration File Format

**File Name Pattern:**
```
V<VERSION>__<DESCRIPTION>.sql
```

**Examples:**
- `V1__Baseline.sql` ✅
- `V2__Add_user_profile_fields.sql` ✅
- `V3__Add_tournament_visibility.sql` ✅
- `V4__Rename_status_column.sql` ✅

**Rules:**
- Version must be a number (V1, V2, V3... or V1.1, V1.2)
- **TWO underscores** `__` between version and description
- Use **underscores** in description (not spaces or dashes)
- Once a migration runs, NEVER change it!

## 🚀 Your First Migration Scenario

Let's say you want to add a "rating" field to the users table.

### Step 1: Create the Migration File

```bash
cd connectFrontendWithBackend/src/main/resources/db/migration/
touch V4__Add_player_rating.sql
```

### Step 2: Write the SQL

```sql
-- V4__Add_player_rating.sql
-- Add ELO-style rating for players

ALTER TABLE users
ADD COLUMN rating INTEGER DEFAULT 1000;

-- Add index for sorting by rating
CREATE INDEX idx_users_rating ON users(rating);

-- Set initial rating for existing users
UPDATE users
SET rating = 1000
WHERE rating IS NULL;
```

### Step 3: Update Your Java Entity

```java
@Entity
public class User {
    // ... existing fields ...

    @Column(name = "rating")
    private Integer rating = 1000;

    // getters and setters
}
```

### Step 4: Restart Your App

Flyway automatically detects V4, runs it, marks it complete. Done! 🎉

### Step 5: Check It Worked

Connect to your database:
```sql
-- See all migrations
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- Check your new column
SELECT id, username, rating FROM users LIMIT 5;
```

## ⚠️ Important Things to Remember

### ✅ DO:
- Write descriptive migration names
- Test migrations locally first
- Commit migration files to Git
- Use `IF NOT EXISTS` for safety
- Add comments explaining why

### ❌ DON'T:
- Modify existing migration files after they've run
- Delete migration files
- Skip version numbers
- Run migrations manually in production

## 🔧 Common Migration Patterns

### Pattern 1: Adding a New Column (Safe & Easy)

```sql
-- V5__Add_user_timezone.sql
ALTER TABLE users
ADD COLUMN timezone VARCHAR(50) DEFAULT 'UTC';
```

### Pattern 2: Renaming a Column (Requires 2 Migrations)

**Migration V6** - Add new column, copy data:
```sql
-- V6__Add_description_column.sql
ALTER TABLE tournament
ADD COLUMN description TEXT;

UPDATE tournament
SET description = message;
```

**Deploy code that uses `description`**

**Migration V7** - Drop old column:
```sql
-- V7__Remove_message_column.sql
ALTER TABLE tournament
DROP COLUMN message;
```

### Pattern 3: Adding a NOT NULL Constraint

```sql
-- V8__Make_tournament_name_required.sql

-- First, fix any NULL values
UPDATE tournament
SET name = 'Unnamed Tournament'
WHERE name IS NULL;

-- Then add the constraint
ALTER TABLE tournament
ALTER COLUMN name SET NOT NULL;
```

### Pattern 4: Creating an Index for Performance

```sql
-- V9__Add_tournament_status_index.sql
CREATE INDEX idx_tournament_status ON tournament(status);
CREATE INDEX idx_tournament_begin ON tournament(begin);
```

### Pattern 5: Adding a Foreign Key

```sql
-- V10__Add_tournament_organizer_fk.sql
ALTER TABLE tournament
ADD CONSTRAINT fk_tournament_organizer
FOREIGN KEY (owner_id) REFERENCES users(id)
ON DELETE CASCADE;
```

## 🔍 Checking Migration Status

### In Your Database (PostgreSQL):
```sql
-- See all migrations that have run
SELECT
    installed_rank,
    version,
    description,
    type,
    success,
    installed_on
FROM flyway_schema_history
ORDER BY installed_rank;
```

### From Command Line:
```bash
cd connectFrontendWithBackend
mvn flyway:info    # Shows migration status
mvn flyway:validate # Checks if migrations are valid
```

### In Application Logs:
When you start your app, you'll see:
```
Flyway: Migrating schema to version 2 - Add user profile fields
Flyway: Migrating schema to version 3 - Add tournament visibility
Flyway: Successfully applied 2 migrations
```

## 🆘 What If Something Goes Wrong?

### Scenario 1: Migration Failed

**Symptoms:** App won't start, error says "Migration failed"

**Solution:**
1. Check the error message
2. Look at `flyway_schema_history` - find failed migration
3. Fix the SQL manually in database
4. Either:
   - Mark it successful: `UPDATE flyway_schema_history SET success = true WHERE version = 'X'`
   - Or run: `mvn flyway:repair`

### Scenario 2: Checksum Mismatch

**Symptoms:** Error says "Migration checksum mismatch"

**Cause:** You edited a migration file that already ran

**Solution:**
```bash
# Option 1: Repair (tells Flyway to recalculate checksums)
mvn flyway:repair

# Option 2: Manual fix in database
UPDATE flyway_schema_history
SET checksum = <new_checksum>
WHERE version = 'X';
```

### Scenario 3: Need to Rollback

**Bad news:** Flyway doesn't auto-rollback

**Solutions:**
1. Write an "undo" migration (V11__Undo_V10_changes.sql)
2. Restore from database backup
3. Manually reverse with SQL

## 📦 Before Production Deployment Checklist

- [ ] All migrations tested locally
- [ ] Database backup created
- [ ] Team members aware of schema changes
- [ ] Migration files committed to Git
- [ ] No pending code changes that depend on migrations
- [ ] Flyway enabled in production config
- [ ] Database credentials have migration privileges

## 🎓 Learning Resources

- [Flyway Official Docs](https://flywaydb.org/documentation/)
- [PostgreSQL ALTER TABLE](https://www.postgresql.org/docs/current/sql-altertable.html)
- [Migration Best Practices](https://flywaydb.org/documentation/bestpractices)

## 💡 Pro Tips

1. **Version numbering:** Use V1, V2, V3... Don't skip numbers. Don't use dates.

2. **Idempotent migrations:** Use `IF NOT EXISTS` and `IF EXISTS` to make migrations safe to re-run:
   ```sql
   ALTER TABLE users ADD COLUMN IF NOT EXISTS rating INTEGER;
   ```

3. **Separate structure from data:** Put schema changes (ALTER TABLE) in one migration, data changes (UPDATE) in another.

4. **Test migrations locally** before pushing to production.

5. **Small migrations:** Better to have many small migrations than one giant one.

6. **Use transactions:** PostgreSQL wraps DDL in transactions automatically, so failed migrations don't leave partial changes.

## 🔄 Your Workflow Now

### Old Workflow:
```
Edit Java entity → Restart app → Hope Hibernate gets it right
```

### New Workflow:
```
1. Create migration SQL file (V4__my_change.sql)
2. Write SQL for the change
3. Update Java entities to match
4. Restart app locally
5. Flyway runs migration
6. Test your code
7. Commit migration file + code
8. Deploy → Flyway runs in production
```

## 📞 Getting Help

If you need help with a specific database change:
1. Describe what you want to change
2. I'll write the exact migration SQL for you
3. You test it locally
4. Deploy with confidence!

---

**Remember:** Flyway is your friend. It makes database changes trackable, reversible, and safe for production! 🎉
