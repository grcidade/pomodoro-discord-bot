package br.com.pomodorobot.modules.pomodoro.service;

import br.com.pomodorobot.core.time.TimeProvider;
import br.com.pomodorobot.modules.pomodoro.domain.PomodoroSession;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroReminderResponse;
import br.com.pomodorobot.modules.pomodoro.repository.PomodoroSessionRepository;
import br.com.pomodorobot.shared.enums.PomodoroReminderType;
import br.com.pomodorobot.shared.enums.PomodoroStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PomodoroReminderService {

    private static final PomodoroReminderType LARGEST_REMINDER_THRESHOLD = PomodoroReminderType.TEN_MINUTES;

    private final PomodoroSessionRepository pomodoroSessionRepository;
    private final TimeProvider timeProvider;

    @Transactional
    public List<PomodoroReminderResponse> collectDueReminders() {
        Instant now = timeProvider.now();
        Instant reminderWindowEndsAt = now.plus(LARGEST_REMINDER_THRESHOLD.getTimeBeforePhaseEnd());
        List<PomodoroSession> runningSessions = pomodoroSessionRepository
                .findByStatusAndCurrentPhaseEndsAtBetweenOrderByCurrentPhaseEndsAtAsc(
                        PomodoroStatus.RUNNING,
                        now,
                        reminderWindowEndsAt
                );
        List<PomodoroReminderResponse> reminders = new ArrayList<>();

        for (PomodoroSession session : runningSessions) {
            findDueReminder(session, now).ifPresent(reminder -> {
                markReminderAsSent(session, reminder, now);
                pomodoroSessionRepository.save(session);
                reminders.add(toReminderResponse(session, reminder));
            });
        }

        return reminders;
    }

    private Optional<PomodoroReminderType> findDueReminder(PomodoroSession session, Instant now) {
        if (shouldSendReminder(session, now, PomodoroReminderType.FIVE_MINUTES)) {
            return Optional.of(PomodoroReminderType.FIVE_MINUTES);
        }

        if (shouldSendReminder(session, now, PomodoroReminderType.TEN_MINUTES)) {
            return Optional.of(PomodoroReminderType.TEN_MINUTES);
        }

        return Optional.empty();
    }

    private boolean shouldSendReminder(PomodoroSession session, Instant now, PomodoroReminderType reminderType) {
        return session.getStatus() == PomodoroStatus.RUNNING
                && isPhaseLongerThanReminderThreshold(session, reminderType)
                && isReminderPending(session, reminderType)
                && isReminderDue(session, now, reminderType);
    }

    private boolean isPhaseLongerThanReminderThreshold(PomodoroSession session, PomodoroReminderType reminderType) {
        long phaseSeconds = Duration.between(
                session.getCurrentPhaseStartedAt(),
                session.getCurrentPhaseEndsAt()
        ).toSeconds();

        return phaseSeconds > reminderType.getTimeBeforePhaseEnd().toSeconds();
    }

    private boolean isReminderPending(PomodoroSession session, PomodoroReminderType reminderType) {
        if (reminderType == PomodoroReminderType.TEN_MINUTES) {
            return session.getTenMinuteReminderSentAt() == null;
        }

        return session.getFiveMinuteReminderSentAt() == null;
    }

    private boolean isReminderDue(PomodoroSession session, Instant now, PomodoroReminderType reminderType) {
        long secondsUntilPhaseEnd = Duration.between(now, session.getCurrentPhaseEndsAt()).toSeconds();
        return secondsUntilPhaseEnd <= reminderType.getTimeBeforePhaseEnd().toSeconds();
    }

    private void markReminderAsSent(PomodoroSession session, PomodoroReminderType reminderType, Instant now) {
        if (reminderType == PomodoroReminderType.TEN_MINUTES) {
            session.setTenMinuteReminderSentAt(now);
            return;
        }

        if (session.getTenMinuteReminderSentAt() == null) {
            session.setTenMinuteReminderSentAt(now);
        }

        session.setFiveMinuteReminderSentAt(now);
    }

    private PomodoroReminderResponse toReminderResponse(PomodoroSession session, PomodoroReminderType reminderType) {
        return PomodoroReminderResponse.builder()
                .sessionId(session.getId())
                .channelId(session.getChannelId())
                .userId(session.getUserId())
                .type(reminderType)
                .currentPhaseEndsAt(session.getCurrentPhaseEndsAt())
                .build();
    }
}
