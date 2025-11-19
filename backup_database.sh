#!/bin/bash

# Database Backup Script for Tournament Host
# Run this before making any risky database changes

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}🗄️  Tournament Host Database Backup${NC}"
echo "=================================="

# Load database credentials from .env file
if [ -f "connectFrontendWithBackend/.env" ]; then
    source connectFrontendWithBackend/.env
    echo -e "${GREEN}✓${NC} Loaded credentials from .env"
else
    echo -e "${RED}✗${NC} .env file not found!"
    echo "Please create connectFrontendWithBackend/.env with:"
    echo "  DB_URL=jdbc:postgresql://localhost:5432/your_db"
    echo "  DB_USERNAME=your_username"
    echo "  DB_PASSWORD=your_password"
    exit 1
fi

# Extract database name from JDBC URL
# Format: jdbc:postgresql://localhost:5432/database_name
DB_NAME=$(echo "$DB_URL" | sed 's/.*\///' | sed 's/?.*//')
DB_HOST=$(echo "$DB_URL" | sed 's/.*:\/\///' | sed 's/:.*//')
DB_PORT=$(echo "$DB_URL" | sed 's/.*://' | sed 's/\/.*//')

if [ -z "$DB_NAME" ]; then
    echo -e "${RED}✗${NC} Could not extract database name from DB_URL"
    exit 1
fi

echo "Database: $DB_NAME"
echo "Host: $DB_HOST:$DB_PORT"
echo "User: $DB_USERNAME"

# Create backups directory if it doesn't exist
mkdir -p database_backups

# Generate backup filename with timestamp
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="database_backups/tournament_host_${TIMESTAMP}.sql"

echo ""
echo -e "${YELLOW}Creating backup...${NC}"

# Run pg_dump
PGPASSWORD="$DB_PASSWORD" pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USERNAME" "$DB_NAME" > "$BACKUP_FILE"

# Check if backup was successful
if [ $? -eq 0 ]; then
    # Get file size
    FILE_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    echo -e "${GREEN}✓${NC} Backup created successfully!"
    echo ""
    echo "📁 Backup file: $BACKUP_FILE"
    echo "📊 Size: $FILE_SIZE"
    echo ""
    echo -e "${GREEN}Your database is now safely backed up!${NC}"
    echo ""
    echo "To restore this backup later, run:"
    echo "  psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME $DB_NAME < $BACKUP_FILE"
else
    echo -e "${RED}✗${NC} Backup failed!"
    echo "Make sure PostgreSQL client tools are installed:"
    echo "  macOS: brew install postgresql"
    echo "  Ubuntu: sudo apt-get install postgresql-client"
    exit 1
fi

# Clean up old backups (keep last 10)
echo ""
echo "Cleaning up old backups (keeping last 10)..."
ls -t database_backups/tournament_host_*.sql | tail -n +11 | xargs rm -f 2>/dev/null
echo -e "${GREEN}✓${NC} Cleanup complete"
