package br.com.pomodorobot.modules.pomodoro.command;

import br.com.pomodorobot.modules.pomodoro.dto.PomodoroSessionResponse;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroStartRequest;
import br.com.pomodorobot.modules.pomodoro.service.PomodoroSessionService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartPomodoroCommandHandler implements PomodoroSubcommandHandler {

    private final PomodoroSessionService pomodoroSessionService;
    private final PomodoroMainMessageService pomodoroMainMessageService;

    @Override
    public String subcommandName() {
        return "start";
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        PomodoroStartRequest request = PomodoroStartRequest.builder()
                .guildId(PomodoroCommandSupport.guildId(event))
                .channelId(PomodoroCommandSupport.channelId(event))
                .userId(PomodoroCommandSupport.userId(event))
                .username(PomodoroCommandSupport.username(event))
                .focusMinutes(PomodoroCommandSupport.integerOption(event, "focus_minutes"))
                .shortBreakMinutes(PomodoroCommandSupport.integerOption(event, "short_break_minutes"))
                .longBreakMinutes(PomodoroCommandSupport.integerOption(event, "long_break_minutes"))
                .cycles(PomodoroCommandSupport.integerOption(event, "cycles"))
                .build();

        PomodoroSessionResponse response = pomodoroSessionService.start(request);

        event.replyEmbeds(PomodoroEmbedFactory.started(response))
                .setComponents(PomodoroButtonFactory.controlsRow(response))
                .queue(hook -> hook.retrieveOriginal()
                        .queue(message -> {
                            PomodoroSessionResponse updatedResponse = pomodoroSessionService.attachMessage(
                                    response.id(),
                                    response.userId(),
                                    message.getId()
                            );
                            pomodoroMainMessageService.update(updatedResponse);
                        }));
    }
}
