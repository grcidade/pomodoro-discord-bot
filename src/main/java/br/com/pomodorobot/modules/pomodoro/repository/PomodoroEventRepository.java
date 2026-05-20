package br.com.pomodorobot.modules.pomodoro.repository;

import br.com.pomodorobot.modules.pomodoro.domain.PomodoroEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PomodoroEventRepository extends JpaRepository<PomodoroEvent, UUID> {
}
