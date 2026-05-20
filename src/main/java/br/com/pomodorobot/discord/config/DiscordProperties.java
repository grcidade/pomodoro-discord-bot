package br.com.pomodorobot.discord.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Builder
@Validated
@ConfigurationProperties(prefix = "bot.discord")
public record DiscordProperties(
        @NotBlank String token
) {
}
