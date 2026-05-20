ALTER TABLE pomodoro_events
    ALTER COLUMN metadata TYPE TEXT
    USING metadata::TEXT;
