package br.com.pomodorobot.modules.guild.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
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
@Table(name = "guild_settings")
public class GuildSettings {

    public static final int DEFAULT_FOCUS_MINUTES = 25;
    public static final int DEFAULT_SHORT_BREAK_MINUTES = 5;
    public static final int DEFAULT_LONG_BREAK_MINUTES = 15;
    public static final int DEFAULT_CYCLES = 4;
    public static final String DEFAULT_TIMEZONE = "America/Sao_Paulo";

    @Id
    @Column(name = "guild_id", nullable = false, length = 32)
    private String guildId;

    @Column(name = "default_focus_minutes", nullable = false)
    private int defaultFocusMinutes;

    @Column(name = "default_short_break_minutes", nullable = false)
    private int defaultShortBreakMinutes;

    @Column(name = "default_long_break_minutes", nullable = false)
    private int defaultLongBreakMinutes;

    @Column(name = "default_cycles", nullable = false)
    private int defaultCycles;

    @Column(name = "notification_channel_id", length = 32)
    private String notificationChannelId;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static GuildSettings createDefault(String guildId) {
        return GuildSettings.builder()
                .guildId(guildId)
                .defaultFocusMinutes(DEFAULT_FOCUS_MINUTES)
                .defaultShortBreakMinutes(DEFAULT_SHORT_BREAK_MINUTES)
                .defaultLongBreakMinutes(DEFAULT_LONG_BREAK_MINUTES)
                .defaultCycles(DEFAULT_CYCLES)
                .timezone(DEFAULT_TIMEZONE)
                .build();
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
