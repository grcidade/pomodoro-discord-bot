package br.com.pomodorobot.modules.pomodoro.command;

import br.com.pomodorobot.modules.pomodoro.dto.PomodoroSessionResponse;
import br.com.pomodorobot.shared.enums.PomodoroStatus;
import java.util.List;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;

@UtilityClass
public class PomodoroButtonFactory {

    public List<Button> controls(PomodoroSessionResponse response) {
        return List.of(
                pauseButton(response),
                resumeButton(response),
                stopButton(response),
                statusButton(response)
        );
    }

    public ActionRow controlsRow(PomodoroSessionResponse response) {
        return ActionRow.of(controls(response));
    }

    private Button pauseButton(PomodoroSessionResponse response) {
        Button button = Button.primary(
                PomodoroButtonIdMapper.toId(PomodoroButtonAction.PAUSE, response.id()),
                "Pausar"
        );

        return applyDisabled(button, response.status() != PomodoroStatus.RUNNING);
    }

    private Button resumeButton(PomodoroSessionResponse response) {
        Button button = Button.success(
                PomodoroButtonIdMapper.toId(PomodoroButtonAction.RESUME, response.id()),
                "Retomar"
        );

        return applyDisabled(button, response.status() != PomodoroStatus.PAUSED);
    }

    private Button stopButton(PomodoroSessionResponse response) {
        Button button = Button.danger(
                PomodoroButtonIdMapper.toId(PomodoroButtonAction.STOP, response.id()),
                "Parar"
        );

        return applyDisabled(button, !isActive(response));
    }

    private Button statusButton(PomodoroSessionResponse response) {
        Button button = Button.secondary(
                PomodoroButtonIdMapper.toId(PomodoroButtonAction.STATUS, response.id()),
                "Status"
        );

        return applyDisabled(button, !isActive(response));
    }

    private Button applyDisabled(Button button, boolean disabled) {
        if (disabled) {
            return button.asDisabled();
        }

        return button;
    }

    private boolean isActive(PomodoroSessionResponse response) {
        return response.status() == PomodoroStatus.RUNNING || response.status() == PomodoroStatus.PAUSED;
    }
}
