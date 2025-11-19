# Flyway Database Migrations

This directory contains all database schema migrations for the tournament-host application.

## 📋 Migration File Naming Convention

All migration files must follow this pattern:
```
V<version>__<description>.sql
```

Examples:
- `V1__Baseline.sql` - Initial database state
- `V2__Add_user_profile_fields.sql` - Adding profile columns
- `V3__Add_tournament_visibility.sql` - Adding visibility settings
- `V4__Rename_message_to_description.sql` - Renaming column

**Important Rules:**
- Version must be a number (can use dots like V1.1, V1.2)
- Two underscores `__` separate version from description
- Use underscores `_` in description (not spaces or hyphens)
- Files are executed in version order

## 🚀 How to Create a New Migration

### Step 1: Determine the Next Version Number
Check existing files in this directory. If the latest is `V3__something.sql`, your new file should be `V4__your_change.sql`.

### Step 2: Create the SQL File
```bash
# Example: Adding a new column
touch V4__Add_player_rating.sql
```

### Step 3: Write the Migration SQL
```sql
-- V4__Add_player_rating.sql
ALTER TABLE users
ADD COLUMN rating INTEGER DEFAULT 1000;

-- Add index if needed
CREATE INDEX idx_users_rating ON users(rating);
```

### Step 4: Test Locally
1. Restart your Spring Boot application
2. Flyway will automatically detect and run the new migration
3. Check the `flyway_schema_history` table to confirm

### Step 5: Commit to Git
```bash
git add src/main/resources/db/migration/V4__Add_player_rating.sql
git commit -m "Add player rating column to users table"
```

## 📝 Common Migration Patterns

### Adding a Column (Safe)
```sql
ALTER TABLE table_name
ADD COLUMN new_column VARCHAR(255) DEFAULT 'default_value';
```

### Renaming a Column (Requires Care)
```sql
-- Step 1: Add new column
ALTER TABLE table_name
ADD COLUMN new_name VARCHAR(255);

-- Step 2: Copy data
UPDATE table_name
SET new_name = old_name;

-- Step 3: (Deploy new code that uses new_name)

-- Step 4: Drop old column (in a future migration after code deploy)
ALTER TABLE table_name
DROP COLUMN old_name;
```

### Adding a NOT NULL Constraint
```sql
-- First, set a default for existing rows
UPDATE table_name
SET column_name = 'default_value'
WHERE column_name IS NULL;

-- Then add the constraint
ALTER TABLE table_name
ALTER COLUMN column_name SET NOT NULL;
```

### Creating an Index
```sql
CREATE INDEX idx_table_column ON table_name(column_name);
```

### Adding a Foreign Key
```sql
ALTER TABLE child_table
ADD CONSTRAINT fk_child_parent
FOREIGN KEY (parent_id) REFERENCES parent_table(id)
ON DELETE CASCADE;
```

## 🔍 Checking Migration Status

### View Migration History
Connect to your database and run:
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

This shows all migrations that have been applied.

### Flyway Commands (if needed)
```bash
# Check migration status
mvn flyway:info

# Validate migrations
mvn flyway:validate

# Repair migration history (only if corrupted)
mvn flyway:repair
```

## ⚠️ Important Warnings

1. **NEVER modify an already-applied migration file** - This will cause checksum errors
2. **NEVER delete migration files** - Flyway tracks them by checksum
3. **Always test migrations locally first** before deploying to production
4. **Backup your database** before running risky migrations
5. **Use transactions** - Most DDL commands are transactional in PostgreSQL

## 🆘 What If Something Goes Wrong?

### Migration Failed Mid-Execution
1. Check `flyway_schema_history` table - look for `success = false`
2. Manually fix the issue in the database
3. Mark the migration as successful: `UPDATE flyway_schema_history SET success = true WHERE version = 'X'`
4. Or use `mvn flyway:repair`

### Need to Rollback
Flyway doesn't have automatic rollback. Options:
1. Write a new "undo" migration (V5__Undo_previous_change.sql)
2. Restore from database backup
3. Manually write SQL to reverse the change

## 📚 Resources

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [PostgreSQL ALTER TABLE](https://www.postgresql.org/docs/current/sql-altertable.html)
- [SQL Best Practices](https://www.sqlstyle.guide/)
