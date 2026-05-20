package br.com.pomodorobot.modules.pomodoro.command;

import java.util.UUID;
import lombok.Builder;

@Builder
public record PomodoroButtonId(
        PomodoroButtonAction action,
        UUID sessionId
) {
}
