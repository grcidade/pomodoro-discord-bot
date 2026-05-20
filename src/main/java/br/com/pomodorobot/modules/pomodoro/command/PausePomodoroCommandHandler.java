package br.com.pomodorobot.modules.pomodoro.command;

import br.com.pomodorobot.modules.pomodoro.dto.PomodoroSessionResponse;
import br.com.pomodorobot.modules.pomodoro.service.PomodoroSessionService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PausePomodoroCommandHandler implements PomodoroSubcommandHandler {

    private final PomodoroSessionService pomodoroSessionService;
    private final PomodoroMainMessageService pomodoroMainMessageService;

    @Override
    public String subcommandName() {
        return "pause";
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        PomodoroSessionResponse response = pomodoroSessionService.pause(
                PomodoroCommandSupport.guildId(event),
                PomodoroCommandSupport.userId(event)
        );

        pomodoroMainMessageService.update(response);
        event.replyEmbeds(PomodoroEmbedFactory.paused(response))
                .setComponents(PomodoroButtonFactory.controlsRow(response))
                .queue();
    }
}
