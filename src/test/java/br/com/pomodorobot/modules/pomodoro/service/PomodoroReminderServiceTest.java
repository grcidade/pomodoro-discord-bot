package br.com.pomodorobot.modules.pomodoro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.pomodorobot.core.time.TimeProvider;
import br.com.pomodorobot.modules.pomodoro.domain.PomodoroSession;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroReminderResponse;
import br.com.pomodorobot.modules.pomodoro.repository.PomodoroSessionRepository;
import br.com.pomodorobot.shared.enums.PomodoroPhase;
import br.com.pomodorobot.shared.enums.PomodoroReminderType;
import br.com.pomodorobot.shared.enums.PomodoroStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PomodoroReminderServiceTest {

    private static final String GUILD_ID = "guild-id";
    private static final String CHANNEL_ID = "channel-id";
    private static final String USER_ID = "user-id";
    private static final UUID SESSION_ID = UUID.fromString("cbad75c7-7dc3-4d7a-991e-afbfcc970000");
    private static final Instant NOW = Instant.parse("2026-05-19T12:00:00Z");

    @Mock
    private PomodoroSessionRepository pomodoroSessionRepository;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private PomodoroReminderService pomodoroReminderService;

    @Test
    void shouldCollectTenMinuteReminderOnce() {
        stubNow();
        PomodoroSession session = activeSessionWithPhaseEndingInSeconds(600);
        stubRunningSessions(session);
        when(pomodoroSessionRepository.save(any(PomodoroSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<PomodoroReminderResponse> reminders = pomodoroReminderService.collectDueReminders();

        assertThat(reminders).hasSize(1);
        assertThat(reminders.getFirst().type()).isEqualTo(PomodoroReminderType.TEN_MINUTES);
        assertThat(session.getTenMinuteReminderSentAt()).isEqualTo(NOW);
        verify(pomodoroSessionRepository).save(session);

        List<PomodoroReminderResponse> repeatedReminders = pomodoroReminderService.collectDueReminders();

        assertThat(repeatedReminders).isEmpty();
    }

    @Test
    void shouldCollectFiveMinuteReminderOnceAndConsumeLateTenMinuteReminder() {
        stubNow();
        PomodoroSession session = activeSessionWithPhaseEndingInSeconds(300);
        stubRunningSessions(session);
        when(pomodoroSessionRepository.save(any(PomodoroSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<PomodoroReminderResponse> reminders = pomodoroReminderService.collectDueReminders();

        assertThat(reminders).hasSize(1);
        assertThat(reminders.getFirst().type()).isEqualTo(PomodoroReminderType.FIVE_MINUTES);
        assertThat(session.getFiveMinuteReminderSentAt()).isEqualTo(NOW);
        assertThat(session.getTenMinuteReminderSentAt()).isEqualTo(NOW);
        verify(pomodoroSessionRepository).save(session);

        List<PomodoroReminderResponse> repeatedReminders = pomodoroReminderService.collectDueReminders();

        assertThat(repeatedReminders).isEmpty();
    }

    @Test
    void shouldNotCollectReminderForPausedSession() {
        stubNow();
        PomodoroSession session = activeSessionWithPhaseEndingInSeconds(300);
        session.setStatus(PomodoroStatus.PAUSED);
        stubRunningSessions(session);

        List<PomodoroReminderResponse> reminders = pomodoroReminderService.collectDueReminders();

        assertThat(reminders).isEmpty();
    }

    private void stubNow() {
        when(timeProvider.now()).thenReturn(NOW);
    }

    private void stubRunningSessions(PomodoroSession session) {
        when(pomodoroSessionRepository.findByStatusAndCurrentPhaseEndsAtBetweenOrderByCurrentPhaseEndsAtAsc(
                PomodoroStatus.RUNNING,
                NOW,
                NOW.plusSeconds(600)
        )).thenReturn(List.of(session));
    }

    private PomodoroSession activeSessionWithPhaseEndingInSeconds(long secondsUntilPhaseEnd) {
        return PomodoroSession.builder()
                .id(SESSION_ID)
                .guildId(GUILD_ID)
                .channelId(CHANNEL_ID)
                .userId(USER_ID)
                .status(PomodoroStatus.RUNNING)
                .currentPhase(PomodoroPhase.FOCUS)
                .focusMinutes(25)
                .shortBreakMinutes(5)
                .longBreakMinutes(15)
                .cyclesTotal(4)
                .cyclesCompleted(0)
                .startedAt(NOW.minusSeconds(900))
                .currentPhaseStartedAt(NOW.minusSeconds(900))
                .currentPhaseEndsAt(NOW.plusSeconds(secondsUntilPhaseEnd))
                .build();
    }
}
