-- Example Migration: Adding tournament visibility settings
-- This shows how to add a column with a default value for existing rows

-- Add visibility column (PUBLIC, PRIVATE, UNLISTED)
ALTER TABLE tournament
ADD COLUMN IF NOT EXISTS visibility VARCHAR(20) DEFAULT 'PUBLIC';

-- Update existing tournaments to be PUBLIC
UPDATE tournament
SET visibility = 'PUBLIC'
WHERE visibility IS NULL;

-- Add NOT NULL constraint after setting default
ALTER TABLE tournament
ALTER COLUMN visibility SET NOT NULL;

-- Create index for filtering by visibility
CREATE INDEX IF NOT EXISTS idx_tournament_visibility ON tournament(visibility);
