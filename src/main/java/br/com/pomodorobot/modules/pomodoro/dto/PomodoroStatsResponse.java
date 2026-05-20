package br.com.pomodorobot.modules.pomodoro.dto;

import lombok.Builder;

@Builder
public record PomodoroStatsResponse(
        String guildId,
        String userId,
        long completedSessions
) {
}
