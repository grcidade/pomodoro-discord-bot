package br.com.pomodorobot.modules.pomodoro.command;

import br.com.pomodorobot.modules.pomodoro.dto.PomodoroReminderResponse;
import java.time.Instant;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PomodoroReminderMessageFactory {

    public String reminder(PomodoroReminderResponse response) {
        return userMention(response.userId())
                + ", "
                + formatMinutes(response)
                + " minutes left in this Pomodoro phase. Phase ends at "
                + formatTimestamp(response.currentPhaseEndsAt())
                + ".";
    }

    private long formatMinutes(PomodoroReminderResponse response) {
        return response.type().getTimeBeforePhaseEnd().toMinutes();
    }

    private String formatTimestamp(Instant instant) {
        return "<t:" + instant.getEpochSecond() + ":t>";
    }

    private String userMention(String userId) {
        return "<@" + userId + ">";
    }
}
