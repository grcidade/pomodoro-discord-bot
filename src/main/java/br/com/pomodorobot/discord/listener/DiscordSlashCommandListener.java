package br.com.pomodorobot.discord.listener;

import br.com.pomodorobot.core.exception.BusinessException;
import br.com.pomodorobot.discord.command.DiscordCommand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordSlashCommandListener extends ListenerAdapter {

    private final List<DiscordCommand> discordCommands;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        discordCommands.stream()
                .filter(command -> command.name().equals(event.getName()))
                .findFirst()
                .ifPresentOrElse(
                        command -> executeCommand(command, event),
                        () -> replyError(event, "Unknown command.")
                );
    }

    private void executeCommand(DiscordCommand command, SlashCommandInteractionEvent event) {
        try {
            command.handle(event);
        } catch (BusinessException exception) {
            replyError(event, exception.getMessage());
        } catch (RuntimeException exception) {
            log.error("Unexpected error while handling Discord command.", exception);
            replyError(event, "Unexpected error while processing command.");
        }
    }

    private void replyError(SlashCommandInteractionEvent event, String message) {
        event.reply(message)
                .setEphemeral(true)
                .queue();
    }
}
