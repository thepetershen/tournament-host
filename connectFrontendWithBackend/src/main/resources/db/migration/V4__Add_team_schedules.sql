-- Migration: Replace PlayerSchedule with TeamSchedule for Round Robin events
-- This enables proper handling of both singles and doubles matches

-- Step 1: Create the team_schedules table
CREATE TABLE IF NOT EXISTS team_schedules (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    CONSTRAINT fk_team_schedules_team FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_schedules_event FOREIGN KEY (event_id) REFERENCES base_event(id) ON DELETE CASCADE
);

-- Step 2: Create the team_schedule_matches join table
CREATE TABLE IF NOT EXISTS team_schedule_matches (
    schedule_id BIGINT NOT NULL,
    match_id BIGINT NOT NULL,
    PRIMARY KEY (schedule_id, match_id),
    CONSTRAINT fk_team_schedule_matches_schedule FOREIGN KEY (schedule_id) REFERENCES team_schedules(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_schedule_matches_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE
);

-- Step 3: Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_team_schedules_team ON team_schedules(team_id);
CREATE INDEX IF NOT EXISTS idx_team_schedules_event ON team_schedules(event_id);
CREATE INDEX IF NOT EXISTS idx_team_schedule_matches_schedule ON team_schedule_matches(schedule_id);
CREATE INDEX IF NOT EXISTS idx_team_schedule_matches_match ON team_schedule_matches(match_id);

-- Step 4: Migrate data from player_schedules to team_schedules (if they exist)
-- This creates team schedules for existing round robin events
DO $$
DECLARE
    schedule_record RECORD;
    new_team_id BIGINT;
    new_schedule_id BIGINT;
BEGIN
    -- Only run if player_schedules table exists
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'player_schedules') THEN

        -- For each player schedule, create a single-player team and team schedule
        FOR schedule_record IN
            SELECT ps.id, ps.player_id, ps.event_id
            FROM player_schedules ps
            WHERE ps.event_id IS NOT NULL
        LOOP
            -- Check if a team already exists for this player in this event
            SELECT id INTO new_team_id
            FROM team
            WHERE player1_id = schedule_record.player_id
              AND event_id = schedule_record.event_id
              AND player2_id IS NULL
            LIMIT 1;

            -- If no team exists, create one
            IF new_team_id IS NULL THEN
                INSERT INTO team (player1_id, team_type, event_id)
                VALUES (schedule_record.player_id, 'SINGLES', schedule_record.event_id)
                RETURNING id INTO new_team_id;
            END IF;

            -- Create team schedule
            INSERT INTO team_schedules (team_id, event_id)
            VALUES (new_team_id, schedule_record.event_id)
            RETURNING id INTO new_schedule_id;

            -- Copy match associations from schedule_matches to team_schedule_matches
            INSERT INTO team_schedule_matches (schedule_id, match_id)
            SELECT new_schedule_id, sm.match_id
            FROM schedule_matches sm
            WHERE sm.schedule_id = schedule_record.id;
        END LOOP;

        -- Drop old player_schedules tables
        DROP TABLE IF EXISTS schedule_matches CASCADE;
        DROP TABLE IF EXISTS player_schedules CASCADE;
    END IF;
END $$;
