package br.com.pomodorobot.modules.guild.dto;

import lombok.Builder;

@Builder
public record GuildSettingsResponse(
        String guildId,
        int defaultFocusMinutes,
        int defaultShortBreakMinutes,
        int defaultLongBreakMinutes,
        int defaultCycles,
        String notificationChannelId,
        String timezone
) {
}
