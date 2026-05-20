package br.com.pomodorobot.modules.pomodoro.command;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.pomodorobot.modules.pomodoro.dto.PomodoroSessionResponse;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroStatsResponse;
import br.com.pomodorobot.shared.enums.PomodoroPhase;
import br.com.pomodorobot.shared.enums.PomodoroStatus;
import java.awt.Color;
import java.time.Instant;
import java.util.UUID;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.junit.jupiter.api.Test;

class PomodoroEmbedFactoryTest {

    private static final Instant PHASE_ENDS_AT = Instant.parse("2026-05-19T12:25:00Z");

    @Test
    void shouldBuildStartedEmbedWithSessionFields() {
        MessageEmbed embed = PomodoroEmbedFactory.started(sessionResponse(
                PomodoroStatus.RUNNING,
                PomodoroPhase.FOCUS,
                0,
                4
        ));

        assertThat(embed.getTitle()).isEqualTo("Pomodoro started");
        assertThat(embed.getColor()).isEqualTo(new Color(230, 126, 34));
        assertThat(fieldValue(embed, "Status")).isEqualTo("Running");
        assertThat(fieldValue(embed, "Phase")).isEqualTo("Focus");
        assertThat(fieldValue(embed, "Progress")).isEqualTo("[----] 0/4");
        assertThat(fieldValue(embed, "Phase ends")).isEqualTo("<t:1779193500:t>");
        assertThat(hasField(embed, "Remaining")).isFalse();
    }

    @Test
    void shouldBuildProgressBarForPartialSession() {
        MessageEmbed embed = PomodoroEmbedFactory.status(sessionResponse(
                PomodoroStatus.RUNNING,
                PomodoroPhase.SHORT_BREAK,
                2,
                4
        ));

        assertThat(embed.getTitle()).isEqualTo("Pomodoro Running");
        assertThat(embed.getColor()).isEqualTo(new Color(52, 152, 219));
        assertThat(fieldValue(embed, "Progress")).isEqualTo("[##--] 2/4");
    }

    @Test
    void shouldBuildPausedStatusEmbedWithPausedColor() {
        MessageEmbed embed = PomodoroEmbedFactory.status(sessionResponse(
                PomodoroStatus.PAUSED,
                PomodoroPhase.FOCUS,
                1,
                4
        ));

        assertThat(embed.getTitle()).isEqualTo("Pomodoro Paused");
        assertThat(embed.getColor()).isEqualTo(new Color(241, 196, 15));
    }

    @Test
    void shouldBuildCompletedEmbedWithCompletedColorAndProgress() {
        MessageEmbed embed = PomodoroEmbedFactory.completed(sessionResponse(
                PomodoroStatus.COMPLETED,
                PomodoroPhase.FOCUS,
                4,
                4
        ));

        assertThat(embed.getTitle()).isEqualTo("Pomodoro completed");
        assertThat(embed.getColor()).isEqualTo(new Color(46, 204, 113));
        assertThat(fieldValue(embed, "Progress")).isEqualTo("[####] 4/4");
    }

    @Test
    void shouldBuildStatsEmbed() {
        PomodoroStatsResponse response = PomodoroStatsResponse.builder()
                .guildId("guild-id")
                .userId("user-id")
                .completedSessions(3)
                .build();

        MessageEmbed embed = PomodoroEmbedFactory.stats(response);

        assertThat(embed.getTitle()).isEqualTo("Pomodoro stats");
        assertThat(fieldValue(embed, "Completed sessions")).isEqualTo("3");
    }

    private PomodoroSessionResponse sessionResponse(
            PomodoroStatus status,
            PomodoroPhase phase,
            int cyclesCompleted,
            int cyclesTotal
    ) {
        return PomodoroSessionResponse.builder()
                .id(UUID.randomUUID())
                .guildId("guild-id")
                .channelId("channel-id")
                .userId("user-id")
                .status(status)
                .currentPhase(phase)
                .cyclesCompleted(cyclesCompleted)
                .cyclesTotal(cyclesTotal)
                .currentPhaseEndsAt(PHASE_ENDS_AT)
                .build();
    }

    private boolean hasField(MessageEmbed embed, String fieldName) {
        return embed.getFields().stream()
                .anyMatch(field -> fieldName.equals(field.getName()));
    }

    private String fieldValue(MessageEmbed embed, String fieldName) {
        return embed.getFields().stream()
                .filter(field -> fieldName.equals(field.getName()))
                .findFirst()
                .map(MessageEmbed.Field::getValue)
                .orElseThrow();
    }
}
