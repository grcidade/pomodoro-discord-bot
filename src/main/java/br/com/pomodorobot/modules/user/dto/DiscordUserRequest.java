package br.com.pomodorobot.modules.user.dto;

import lombok.Builder;

@Builder
public record DiscordUserRequest(
        String userId,
        String username
) {
}
