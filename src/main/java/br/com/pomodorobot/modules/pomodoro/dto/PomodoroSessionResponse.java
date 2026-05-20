package br.com.pomodorobot.modules.pomodoro.dto;

import br.com.pomodorobot.shared.enums.PomodoroPhase;
import br.com.pomodorobot.shared.enums.PomodoroStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PomodoroSessionResponse(
        UUID id,
        String guildId,
        String channelId,
        String messageId,
        String userId,
        PomodoroStatus status,
        PomodoroPhase currentPhase,
        int cyclesTotal,
        int cyclesCompleted,
        Instant currentPhaseEndsAt
) {
}
