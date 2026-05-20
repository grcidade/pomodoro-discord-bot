package br.com.pomodorobot.modules.pomodoro.command;

import br.com.pomodorobot.modules.pomodoro.dto.PomodoroSessionResponse;
import br.com.pomodorobot.modules.pomodoro.service.PomodoroSessionService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatusPomodoroCommandHandler implements PomodoroSubcommandHandler {

    private final PomodoroSessionService pomodoroSessionService;

    @Override
    public String subcommandName() {
        return "status";
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        PomodoroSessionResponse response = pomodoroSessionService.status(
                PomodoroCommandSupport.guildId(event),
                PomodoroCommandSupport.userId(event)
        );

        event.replyEmbeds(PomodoroEmbedFactory.status(response))
                .setComponents(PomodoroButtonFactory.controlsRow(response))
                .setEphemeral(true)
                .queue();
    }
}
