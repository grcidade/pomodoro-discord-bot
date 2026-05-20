package br.com.pomodorobot.shared.enums;

import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PomodoroReminderType {
    TEN_MINUTES(Duration.ofMinutes(10)),
    FIVE_MINUTES(Duration.ofMinutes(5));

    private final Duration timeBeforePhaseEnd;
}
