package br.com.pomodorobot.modules.pomodoro.command;

import br.com.pomodorobot.modules.guild.dto.GuildSettingsResponse;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroSessionResponse;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroStatsResponse;
import br.com.pomodorobot.shared.enums.PomodoroPhase;
import br.com.pomodorobot.shared.enums.PomodoroStatus;
import java.awt.Color;
import java.time.Instant;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@UtilityClass
public class PomodoroEmbedFactory {

    private static final Color FOCUS_COLOR = new Color(230, 126, 34);
    private static final Color SHORT_BREAK_COLOR = new Color(52, 152, 219);
    private static final Color LONG_BREAK_COLOR = new Color(142, 68, 173);
    private static final Color COMPLETED_COLOR = new Color(46, 204, 113);
    private static final Color CANCELLED_COLOR = new Color(127, 140, 141);
    private static final Color PAUSED_COLOR = new Color(241, 196, 15);
    private static final Color CONFIG_COLOR = new Color(52, 73, 94);

    public MessageEmbed started(PomodoroSessionResponse response) {
        return sessionEmbed("Pomodoro started", "Focus time has started.", response, true);
    }

    public MessageEmbed paused(PomodoroSessionResponse response) {
        return sessionEmbed("Pomodoro paused", "Your session is paused.", response, true);
    }

    public MessageEmbed resumed(PomodoroSessionResponse response) {
        return sessionEmbed("Pomodoro resumed", "Your session is running again.", response, true);
    }

    public MessageEmbed stopped(PomodoroSessionResponse response) {
        return sessionEmbed("Pomodoro stopped", "Your session was cancelled.", response, false);
    }

    public MessageEmbed status(PomodoroSessionResponse response) {
        return sessionEmbed("Pomodoro " + formatStatus(response.status()), "Current session summary.", response, true);
    }

    public MessageEmbed phaseChanged(PomodoroSessionResponse response) {
        return sessionEmbed("Pomodoro phase changed", phaseChangedDescription(response), response, true);
    }

    public MessageEmbed completed(PomodoroSessionResponse response) {
        return sessionEmbed("Pomodoro completed", userMention(response.userId()) + " session completed.", response, false);
    }

    public MessageEmbed settingsUpdated(GuildSettingsResponse response) {
        return new EmbedBuilder()
                .setTitle("Pomodoro settings updated")
                .setDescription("Default settings for this server were updated.")
                .setColor(CONFIG_COLOR)
                .addField("Focus", response.defaultFocusMinutes() + " minutes", true)
                .addField("Short break", response.defaultShortBreakMinutes() + " minutes", true)
                .addField("Long break", response.defaultLongBreakMinutes() + " minutes", true)
                .addField("Cycles", String.valueOf(response.defaultCycles()), true)
                .addField("Notification channel", channelMention(response.notificationChannelId()), true)
                .build();
    }

    public MessageEmbed stats(PomodoroStatsResponse response) {
        return new EmbedBuilder()
                .setTitle("Pomodoro stats")
                .setDescription(userMention(response.userId()) + " study summary.")
                .setColor(COMPLETED_COLOR)
                .addField("Completed sessions", String.valueOf(response.completedSessions()), true)
                .build();
    }

    private MessageEmbed sessionEmbed(
            String title,
            String description,
            PomodoroSessionResponse response,
            boolean includeTiming
    ) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(resolveColor(response))
                .addField("Status", formatStatus(response.status()), true)
                .addField("Phase", formatPhase(response.currentPhase()), true)
                .addField("Cycles", formatCycles(response), true)
                .addField("Progress", progressBar(response), false);

        addTimingFields(builder, response, includeTiming);

        return builder.build();
    }

    private void addTimingFields(EmbedBuilder builder, PomodoroSessionResponse response, boolean includeTiming) {
        if (!includeTiming) {
            return;
        }

        builder.addField("Phase ends", formatTimestamp(response.currentPhaseEndsAt()), true);
    }

    private Color resolveColor(PomodoroSessionResponse response) {
        if (response.status() == PomodoroStatus.COMPLETED) {
            return COMPLETED_COLOR;
        }

        if (response.status() == PomodoroStatus.CANCELLED) {
            return CANCELLED_COLOR;
        }

        if (response.status() == PomodoroStatus.PAUSED) {
            return PAUSED_COLOR;
        }

        return resolvePhaseColor(response.currentPhase());
    }

    private Color resolvePhaseColor(PomodoroPhase phase) {
        return switch (phase) {
            case FOCUS -> FOCUS_COLOR;
            case SHORT_BREAK -> SHORT_BREAK_COLOR;
            case LONG_BREAK -> LONG_BREAK_COLOR;
        };
    }

    private String phaseChangedDescription(PomodoroSessionResponse response) {
        if (response.currentPhase() == PomodoroPhase.FOCUS) {
            return userMention(response.userId()) + " break finished. Focus time started.";
        }

        return userMention(response.userId()) + " focus finished. Break time started.";
    }

    private String formatStatus(PomodoroStatus status) {
        return switch (status) {
            case RUNNING -> "Running";
            case PAUSED -> "Paused";
            case COMPLETED -> "Completed";
            case CANCELLED -> "Cancelled";
            case FAILED -> "Failed";
        };
    }

    private String formatPhase(PomodoroPhase phase) {
        return switch (phase) {
            case FOCUS -> "Focus";
            case SHORT_BREAK -> "Short break";
            case LONG_BREAK -> "Long break";
        };
    }

    private String formatCycles(PomodoroSessionResponse response) {
        return response.cyclesCompleted() + "/" + response.cyclesTotal();
    }

    private String progressBar(PomodoroSessionResponse response) {
        int total = response.cyclesTotal();
        if (total <= 0) {
            return "No cycles";
        }

        int completed = Math.min(Math.max(response.cyclesCompleted(), 0), total);
        return "[" + "#".repeat(completed) + "-".repeat(total - completed) + "] " + completed + "/" + total;
    }

    private String formatTimestamp(Instant instant) {
        return "<t:" + instant.getEpochSecond() + ":t>";
    }

    private String userMention(String userId) {
        return "<@" + userId + ">";
    }

    private String channelMention(String channelId) {
        if (channelId == null || channelId.isBlank()) {
            return "Not configured";
        }

        return "<#" + channelId + ">";
    }
}
