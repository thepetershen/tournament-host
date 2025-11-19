-- Example Migration: Adding new columns to users table
-- This shows how to safely add optional fields

-- Add bio field (for player profiles)
ALTER TABLE users
ADD COLUMN IF NOT EXISTS bio TEXT;

-- Add profile image URL
ALTER TABLE users
ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(500);

-- Add social media links
ALTER TABLE users
ADD COLUMN IF NOT EXISTS twitter_handle VARCHAR(100);

-- Add account created timestamp if not exists
ALTER TABLE users
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Create index on username for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
