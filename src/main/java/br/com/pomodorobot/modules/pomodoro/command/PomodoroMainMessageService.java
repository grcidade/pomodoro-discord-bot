package br.com.pomodorobot.modules.pomodoro.command;

import br.com.pomodorobot.discord.notification.DiscordNotificationService;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroSessionResponse;
import br.com.pomodorobot.shared.enums.PomodoroStatus;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PomodoroMainMessageService {

    private final DiscordNotificationService discordNotificationService;

    public boolean update(PomodoroSessionResponse response) {
        if (response.messageId() == null || response.messageId().isBlank()) {
            return false;
        }

        discordNotificationService.editEmbed(
                response.channelId(),
                response.messageId(),
                resolveEmbed(response),
                PomodoroButtonFactory.controls(response)
        );

        return true;
    }

    private MessageEmbed resolveEmbed(PomodoroSessionResponse response) {
        if (response.status() == PomodoroStatus.COMPLETED) {
            return PomodoroEmbedFactory.completed(response);
        }

        if (response.status() == PomodoroStatus.CANCELLED) {
            return PomodoroEmbedFactory.stopped(response);
        }

        return PomodoroEmbedFactory.status(response);
    }
}
