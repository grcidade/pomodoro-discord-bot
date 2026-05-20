package br.com.pomodorobot.modules.pomodoro.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.pomodorobot.modules.pomodoro.domain.PomodoroSession;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroSessionResponse;
import br.com.pomodorobot.shared.enums.PomodoroPhase;
import br.com.pomodorobot.shared.enums.PomodoroStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PomodoroSessionMapperTest {

    @Test
    void shouldMapSessionToResponse() {
        Instant now = Instant.parse("2026-05-19T12:00:00Z");
        PomodoroSession session = PomodoroSession.builder()
                .id(UUID.randomUUID())
                .guildId("guild-id")
                .channelId("channel-id")
                .userId("user-id")
                .status(PomodoroStatus.RUNNING)
                .currentPhase(PomodoroPhase.FOCUS)
                .cyclesTotal(4)
                .cyclesCompleted(1)
                .currentPhaseEndsAt(now.plusSeconds(90))
                .build();

        PomodoroSessionResponse response = PomodoroSessionMapper.toResponse(session);

        assertThat(response.guildId()).isEqualTo("guild-id");
        assertThat(response.channelId()).isEqualTo("channel-id");
        assertThat(response.userId()).isEqualTo("user-id");
        assertThat(response.currentPhaseEndsAt()).isEqualTo(now.plusSeconds(90));
    }
}
