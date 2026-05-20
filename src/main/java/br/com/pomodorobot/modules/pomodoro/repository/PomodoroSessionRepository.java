package br.com.pomodorobot.modules.pomodoro.repository;

import br.com.pomodorobot.modules.pomodoro.domain.PomodoroSession;
import br.com.pomodorobot.shared.enums.PomodoroStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, UUID> {

    Optional<PomodoroSession> findFirstByGuildIdAndUserIdAndStatusIn(
            String guildId,
            String userId,
            Collection<PomodoroStatus> statuses
    );

    List<PomodoroSession> findByStatusAndCurrentPhaseEndsAtLessThanEqual(
            PomodoroStatus status,
            Instant currentPhaseEndsAt
    );

    List<PomodoroSession> findByStatusAndCurrentPhaseEndsAtBetweenOrderByCurrentPhaseEndsAtAsc(
            PomodoroStatus status,
            Instant startsAt,
            Instant endsAt
    );

    long countByGuildIdAndUserIdAndStatus(String guildId, String userId, PomodoroStatus status);
}
