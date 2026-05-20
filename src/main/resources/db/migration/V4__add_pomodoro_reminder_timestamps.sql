ALTER TABLE pomodoro_sessions
    ADD COLUMN ten_minute_reminder_sent_at TIMESTAMPTZ,
    ADD COLUMN five_minute_reminder_sent_at TIMESTAMPTZ;
