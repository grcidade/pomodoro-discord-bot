package br.com.pomodorobot.modules.pomodoro.domain;

import br.com.pomodorobot.shared.enums.PomodoroPhase;
import br.com.pomodorobot.shared.enums.PomodoroStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pomodoro_sessions")
public class PomodoroSession {

    @Id
    @Builder.Default
    @Column(name = "id", nullable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "guild_id", nullable = false, length = 32)
    private String guildId;

    @Column(name = "channel_id", nullable = false, length = 32)
    private String channelId;

    @Column(name = "message_id", length = 32)
    private String messageId;

    @Column(name = "user_id", nullable = false, length = 32)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PomodoroStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_phase", nullable = false, length = 32)
    private PomodoroPhase currentPhase;

    @Column(name = "focus_minutes", nullable = false)
    private int focusMinutes;

    @Column(name = "short_break_minutes", nullable = false)
    private int shortBreakMinutes;

    @Column(name = "long_break_minutes", nullable = false)
    private int longBreakMinutes;

    @Column(name = "cycles_total", nullable = false)
    private int cyclesTotal;

    @Column(name = "cycles_completed", nullable = false)
    private int cyclesCompleted;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "paused_at")
    private Instant pausedAt;

    @Column(name = "current_phase_started_at", nullable = false)
    private Instant currentPhaseStartedAt;

    @Column(name = "current_phase_ends_at", nullable = false)
    private Instant currentPhaseEndsAt;

    @Column(name = "ten_minute_reminder_sent_at")
    private Instant tenMinuteReminderSentAt;

    @Column(name = "five_minute_reminder_sent_at")
    private Instant fiveMinuteReminderSentAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public boolean isActive() {
        return status == PomodoroStatus.RUNNING || status == PomodoroStatus.PAUSED;
    }

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
