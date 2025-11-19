# ⚡ Flyway Quick Start Guide

## 🎯 TL;DR - What You Need to Know

Flyway is now set up! Instead of letting Hibernate auto-update your database, you now write SQL files that Flyway runs automatically.

## 🚀 Quick Start in 3 Steps

### 1️⃣ Start Your App to Initialize Flyway

```bash
cd connectFrontendWithBackend
./mvnw spring-boot:run
```

**What happens:**
- Flyway creates a `flyway_schema_history` table in your database
- Marks your current schema as "Version 1" (baseline)
- Your existing data is safe! ✅

### 2️⃣ Check It Worked

Connect to your database and run:
```sql
SELECT * FROM flyway_schema_history;
```

You should see Version 1 marked as successful.

### 3️⃣ Make Your First Change

Want to add a "rating" column to users? Create this file:

**File:** `connectFrontendWithBackend/src/main/resources/db/migration/V4__Add_player_rating.sql`

```sql
ALTER TABLE users ADD COLUMN rating INTEGER DEFAULT 1000;
CREATE INDEX idx_users_rating ON users(rating);
```

Restart your app → Flyway runs it automatically! 🎉

## 📋 Common Tasks

### Add a New Column
```sql
-- V4__Add_bio_to_users.sql
ALTER TABLE users ADD COLUMN bio TEXT;
```

### Rename a Column (Safe Way)
```sql
-- V5__Rename_message_to_description.sql
-- Step 1: Add new column
ALTER TABLE tournament ADD COLUMN description TEXT;

-- Step 2: Copy data
UPDATE tournament SET description = message;

-- Step 3: Drop old column (do this in V6 after deploying code!)
```

### Add an Index
```sql
-- V6__Index_tournament_status.sql
CREATE INDEX idx_tournament_status ON tournament(status);
```

## 🆘 Quick Troubleshooting

### Problem: "Migration checksum mismatch"
**Cause:** You edited a file that already ran
**Fix:** `cd connectFrontendWithBackend && mvn flyway:repair`

### Problem: "Migration failed"
**Cause:** SQL error in your migration
**Fix:**
1. Fix the SQL in your database manually
2. Run: `mvn flyway:repair`
3. Restart app

### Problem: Need to undo a migration
**Fix:** Write a new migration that reverses it (e.g., DROP COLUMN)

## 💾 Backup Before Changes

Always backup before risky changes:
```bash
./backup_database.sh
```

Restore if needed:
```bash
psql -U your_user your_db < database_backups/tournament_host_TIMESTAMP.sql
```

## 📚 Full Documentation

See `FLYWAY_GUIDE.md` for detailed examples and patterns.

## ✅ What Changed in Your Setup

| Before | After |
|--------|-------|
| `spring.jpa.hibernate.ddl-auto=update` | `spring.jpa.hibernate.ddl-auto=validate` |
| Hibernate auto-updates schema | Flyway manages schema via SQL files |
| Changes not tracked | Every change has a version in Git |
| Risky in production | Safe for production ✅ |

## 🎓 Remember

1. **Never edit** a migration file after it runs
2. **Always test** migrations locally first
3. **Use version numbers** sequentially (V1, V2, V3...)
4. **Commit migrations** to Git with your code
5. **Backup before** making complex changes

---

That's it! You're ready for production-safe database migrations! 🚀
