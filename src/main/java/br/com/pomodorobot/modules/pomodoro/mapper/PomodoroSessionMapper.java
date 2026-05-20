package br.com.pomodorobot.modules.pomodoro.mapper;

import br.com.pomodorobot.modules.pomodoro.domain.PomodoroSession;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroSessionResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PomodoroSessionMapper {

    public PomodoroSessionResponse toResponse(PomodoroSession session) {
        return PomodoroSessionResponse.builder()
                .id(session.getId())
                .guildId(session.getGuildId())
                .channelId(session.getChannelId())
                .messageId(session.getMessageId())
                .userId(session.getUserId())
                .status(session.getStatus())
                .currentPhase(session.getCurrentPhase())
                .cyclesTotal(session.getCyclesTotal())
                .cyclesCompleted(session.getCyclesCompleted())
                .currentPhaseEndsAt(session.getCurrentPhaseEndsAt())
                .build();
    }
}
