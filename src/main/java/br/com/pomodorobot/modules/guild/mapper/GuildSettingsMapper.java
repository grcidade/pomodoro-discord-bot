package br.com.pomodorobot.modules.guild.mapper;

import br.com.pomodorobot.modules.guild.domain.GuildSettings;
import br.com.pomodorobot.modules.guild.dto.GuildSettingsResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GuildSettingsMapper {

    public GuildSettingsResponse toResponse(GuildSettings settings) {
        return GuildSettingsResponse.builder()
                .guildId(settings.getGuildId())
                .defaultFocusMinutes(settings.getDefaultFocusMinutes())
                .defaultShortBreakMinutes(settings.getDefaultShortBreakMinutes())
                .defaultLongBreakMinutes(settings.getDefaultLongBreakMinutes())
                .defaultCycles(settings.getDefaultCycles())
                .notificationChannelId(settings.getNotificationChannelId())
                .timezone(settings.getTimezone())
                .build();
    }
}
