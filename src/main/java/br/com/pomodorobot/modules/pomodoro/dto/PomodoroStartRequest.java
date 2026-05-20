package br.com.pomodorobot.modules.pomodoro.dto;

import lombok.Builder;

@Builder
public record PomodoroStartRequest(
        String guildId,
        String channelId,
        String userId,
        String username,
        Integer focusMinutes,
        Integer shortBreakMinutes,
        Integer longBreakMinutes,
        Integer cycles
) {
}
