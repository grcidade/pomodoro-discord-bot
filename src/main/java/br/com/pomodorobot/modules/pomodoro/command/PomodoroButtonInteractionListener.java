package br.com.pomodorobot.modules.pomodoro.command;

import br.com.pomodorobot.core.exception.BusinessException;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroSessionResponse;
import br.com.pomodorobot.modules.pomodoro.service.PomodoroSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PomodoroButtonInteractionListener extends ListenerAdapter {

    private final PomodoroSessionService pomodoroSessionService;
    private final PomodoroMainMessageService pomodoroMainMessageService;

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (!PomodoroButtonIdMapper.supports(componentId)) {
            return;
        }

        executeButton(event, PomodoroButtonIdMapper.fromId(componentId));
    }

    private void executeButton(ButtonInteractionEvent event, PomodoroButtonId buttonId) {
        try {
            handleButton(event, buttonId);
        } catch (BusinessException exception) {
            replyError(event, exception.getMessage());
        } catch (RuntimeException exception) {
            log.error("Unexpected error while handling Pomodoro button.", exception);
            replyError(event, "Unexpected error while processing button.");
        }
    }

    private void handleButton(ButtonInteractionEvent event, PomodoroButtonId buttonId) {
        PomodoroSessionResponse response = switch (buttonId.action()) {
            case PAUSE -> pomodoroSessionService.pauseFromButton(buttonId.sessionId(), userId(event));
            case RESUME -> pomodoroSessionService.resumeFromButton(buttonId.sessionId(), userId(event));
            case STOP -> pomodoroSessionService.stopFromButton(buttonId.sessionId(), userId(event));
            case STATUS -> pomodoroSessionService.statusFromButton(buttonId.sessionId(), userId(event));
        };

        if (buttonId.action() == PomodoroButtonAction.STATUS) {
            replyStatus(event, response);
            return;
        }

        pomodoroMainMessageService.update(response);
        event.deferEdit().queue();
    }

    private void replyStatus(ButtonInteractionEvent event, PomodoroSessionResponse response) {
        event.replyEmbeds(PomodoroEmbedFactory.status(response))
                .setEphemeral(true)
                .queue();
    }

    private void replyError(ButtonInteractionEvent event, String message) {
        event.reply(message)
                .setEphemeral(true)
                .queue();
    }

    private String userId(ButtonInteractionEvent event) {
        return event.getUser().getId();
    }
}
