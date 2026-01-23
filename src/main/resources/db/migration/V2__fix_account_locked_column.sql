-- Add account_locked column with default value for existing rows
ALTER TABLE users ADD COLUMN IF NOT EXISTS account_locked boolean DEFAULT false NOT NULL;

-- Add lock_time column
ALTER TABLE users ADD COLUMN IF NOT EXISTS lock_time timestamp with time zone;

-- Ensure failed_attempts has no nulls
UPDATE users SET failed_attempts = 0 WHERE failed_attempts IS NULL;
ALTER TABLE users ALTER COLUMN failed_attempts SET NOT NULL;
ALTER TABLE users ALTER COLUMN failed_attempts SET DEFAULT 0;
