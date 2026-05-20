package br.com.pomodorobot.modules.user.mapper;

import br.com.pomodorobot.modules.user.domain.DiscordUser;
import br.com.pomodorobot.modules.user.dto.DiscordUserResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DiscordUserMapper {

    public DiscordUserResponse toResponse(DiscordUser discordUser) {
        return DiscordUserResponse.builder()
                .userId(discordUser.getUserId())
                .usernameSnapshot(discordUser.getUsernameSnapshot())
                .build();
    }
}
