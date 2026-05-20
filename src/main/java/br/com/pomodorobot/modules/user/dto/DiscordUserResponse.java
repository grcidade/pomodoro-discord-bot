package br.com.pomodorobot.modules.user.dto;

import lombok.Builder;

@Builder
public record DiscordUserResponse(
        String userId,
        String usernameSnapshot
) {
}
