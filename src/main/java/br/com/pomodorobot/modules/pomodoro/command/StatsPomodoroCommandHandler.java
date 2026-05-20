package br.com.pomodorobot.modules.pomodoro.command;

import br.com.pomodorobot.modules.pomodoro.dto.PomodoroStatsResponse;
import br.com.pomodorobot.modules.pomodoro.service.PomodoroSessionService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatsPomodoroCommandHandler implements PomodoroSubcommandHandler {

    private final PomodoroSessionService pomodoroSessionService;

    @Override
    public String subcommandName() {
        return "stats";
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        PomodoroStatsResponse response = pomodoroSessionService.stats(
                PomodoroCommandSupport.guildId(event),
                PomodoroCommandSupport.userId(event)
        );

        event.replyEmbeds(PomodoroEmbedFactory.stats(response))
                .setEphemeral(true)
                .queue();
    }
}
