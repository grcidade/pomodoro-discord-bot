package br.com.pomodorobot.modules.pomodoro.command;

import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PomodoroButtonIdMapper {

    private static final String PREFIX = "pomodoro";
    private static final String SEPARATOR = ":";
    private static final int PREFIX_INDEX = 0;
    private static final int ACTION_INDEX = 1;
    private static final int SESSION_ID_INDEX = 2;
    private static final int PARTS_COUNT = 3;

    public String toId(PomodoroButtonAction action, UUID sessionId) {
        return PREFIX + SEPARATOR + action.id() + SEPARATOR + sessionId;
    }

    public PomodoroButtonId fromId(String componentId) {
        String[] parts = componentId.split(SEPARATOR);
        validateParts(parts);

        return PomodoroButtonId.builder()
                .action(PomodoroButtonAction.fromId(parts[ACTION_INDEX]))
                .sessionId(UUID.fromString(parts[SESSION_ID_INDEX]))
                .build();
    }

    public boolean supports(String componentId) {
        return componentId != null && componentId.startsWith(PREFIX + SEPARATOR);
    }

    private void validateParts(String[] parts) {
        if (parts.length != PARTS_COUNT || !PREFIX.equals(parts[PREFIX_INDEX])) {
            throw new IllegalArgumentException("Invalid Pomodoro button id.");
        }
    }
}
