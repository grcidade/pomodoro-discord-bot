package br.com.pomodorobot.modules.guild.dto;

import lombok.Builder;

@Builder
public record GuildSettingsRequest(
        String guildId,
        Integer defaultFocusMinutes,
        Integer defaultShortBreakMinutes,
        Integer defaultLongBreakMinutes,
        Integer defaultCycles,
        String notificationChannelId
) {
}
