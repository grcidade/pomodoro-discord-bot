package br.com.pomodorobot.modules.pomodoro.command;

import java.util.Arrays;

public enum PomodoroButtonAction {
    PAUSE("pause"),
    RESUME("resume"),
    STOP("stop"),
    STATUS("status");

    private final String id;

    PomodoroButtonAction(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static PomodoroButtonAction fromId(String id) {
        return Arrays.stream(values())
                .filter(action -> action.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported Pomodoro button action."));
    }
}
