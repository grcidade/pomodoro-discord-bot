CREATE TABLE guild_settings (
    guild_id VARCHAR(32) PRIMARY KEY,
    default_focus_minutes INTEGER NOT NULL,
    default_short_break_minutes INTEGER NOT NULL,
    default_long_break_minutes INTEGER NOT NULL,
    default_cycles INTEGER NOT NULL,
    notification_channel_id VARCHAR(32),
    timezone VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE discord_users (
    user_id VARCHAR(32) PRIMARY KEY,
    username_snapshot VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE pomodoro_sessions (
    id UUID PRIMARY KEY,
    guild_id VARCHAR(32) NOT NULL,
    channel_id VARCHAR(32) NOT NULL,
    user_id VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    current_phase VARCHAR(32) NOT NULL,
    focus_minutes INTEGER NOT NULL,
    short_break_minutes INTEGER NOT NULL,
    long_break_minutes INTEGER NOT NULL,
    cycles_total INTEGER NOT NULL,
    cycles_completed INTEGER NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    paused_at TIMESTAMPTZ,
    current_phase_started_at TIMESTAMPTZ NOT NULL,
    current_phase_ends_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE pomodoro_events (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    metadata JSONB,
    CONSTRAINT fk_pomodoro_events_session
        FOREIGN KEY (session_id)
        REFERENCES pomodoro_sessions (id)
);

CREATE INDEX ix_pomodoro_sessions_due
    ON pomodoro_sessions (status, current_phase_ends_at);

CREATE INDEX ix_pomodoro_sessions_user_status
    ON pomodoro_sessions (guild_id, user_id, status);

CREATE UNIQUE INDEX ux_pomodoro_sessions_active_user
    ON pomodoro_sessions (guild_id, user_id)
    WHERE status IN ('RUNNING', 'PAUSED');
