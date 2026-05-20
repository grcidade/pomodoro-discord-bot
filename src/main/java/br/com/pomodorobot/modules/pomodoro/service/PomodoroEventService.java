package br.com.pomodorobot.modules.pomodoro.service;

import br.com.pomodorobot.core.time.TimeProvider;
import br.com.pomodorobot.modules.pomodoro.domain.PomodoroEvent;
import br.com.pomodorobot.modules.pomodoro.domain.PomodoroSession;
import br.com.pomodorobot.modules.pomodoro.repository.PomodoroEventRepository;
import br.com.pomodorobot.shared.enums.PomodoroEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PomodoroEventService {

    private final PomodoroEventRepository pomodoroEventRepository;
    private final TimeProvider timeProvider;

    public void register(PomodoroSession session, PomodoroEventType eventType) {
        PomodoroEvent event = PomodoroEvent.builder()
                .sessionId(session.getId())
                .eventType(eventType)
                .occurredAt(timeProvider.now())
                .build();

        pomodoroEventRepository.save(event);
    }
}
