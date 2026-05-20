package br.com.pomodorobot.modules.pomodoro.command;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.pomodorobot.modules.pomodoro.dto.PomodoroSessionResponse;
import br.com.pomodorobot.shared.enums.PomodoroPhase;
import br.com.pomodorobot.shared.enums.PomodoroStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import net.dv8tion.jda.api.components.buttons.Button;
import org.junit.jupiter.api.Test;

class PomodoroButtonFactoryTest {

    private static final UUID SESSION_ID = UUID.fromString("cbad75c7-7dc3-4d7a-991e-afbfcc970000");

    @Test
    void shouldEnableRunningSessionControls() {
        List<Button> buttons = PomodoroButtonFactory.controls(sessionResponse(PomodoroStatus.RUNNING));

        assertThat(buttons).hasSize(4);
        assertThat(buttons.get(0).getCustomId()).isEqualTo("pomodoro:pause:" + SESSION_ID);
        assertThat(buttons.get(0).isDisabled()).isFalse();
        assertThat(buttons.get(1).isDisabled()).isTrue();
        assertThat(buttons.get(2).isDisabled()).isFalse();
        assertThat(buttons.get(3).isDisabled()).isFalse();
    }

    @Test
    void shouldEnablePausedSessionControls() {
        List<Button> buttons = PomodoroButtonFactory.controls(sessionResponse(PomodoroStatus.PAUSED));

        assertThat(buttons.get(0).isDisabled()).isTrue();
        assertThat(buttons.get(1).isDisabled()).isFalse();
        assertThat(buttons.get(2).isDisabled()).isFalse();
        assertThat(buttons.get(3).isDisabled()).isFalse();
    }

    @Test
    void shouldDisableFinishedSessionControls() {
        List<Button> buttons = PomodoroButtonFactory.controls(sessionResponse(PomodoroStatus.COMPLETED));

        assertThat(buttons).allMatch(Button::isDisabled);
    }

    private PomodoroSessionResponse sessionResponse(PomodoroStatus status) {
        return PomodoroSessionResponse.builder()
                .id(SESSION_ID)
                .guildId("guild-id")
                .channelId("channel-id")
                .messageId("message-id")
                .userId("user-id")
                .status(status)
                .currentPhase(PomodoroPhase.FOCUS)
                .cyclesTotal(4)
                .cyclesCompleted(0)
                .currentPhaseEndsAt(Instant.parse("2026-05-19T12:25:00Z"))
                .build();
    }
}
