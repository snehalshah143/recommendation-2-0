-- Add since_days column to alerts table
ALTER TABLE alerts ADD COLUMN since_days INTEGER DEFAULT 0;

-- Update existing alerts with calculated since_days values
-- This is a simplified update - in production you might want to recalculate all values
UPDATE alerts SET since_days = 0 WHERE since_days IS NULL;
