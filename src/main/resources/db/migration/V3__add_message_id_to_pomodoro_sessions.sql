ALTER TABLE pomodoro_sessions
    ADD COLUMN message_id VARCHAR(32);

CREATE INDEX ix_pomodoro_sessions_active_message
    ON pomodoro_sessions (status, message_id)
    WHERE message_id IS NOT NULL;
