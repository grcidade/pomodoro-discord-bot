package br.com.pomodorobot.modules.pomodoro.dto;

import br.com.pomodorobot.shared.enums.PomodoroReminderType;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PomodoroReminderResponse(
        UUID sessionId,
        String channelId,
        String userId,
        PomodoroReminderType type,
        Instant currentPhaseEndsAt
) {
}
