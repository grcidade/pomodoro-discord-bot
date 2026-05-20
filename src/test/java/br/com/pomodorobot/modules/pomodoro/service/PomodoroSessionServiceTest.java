package br.com.pomodorobot.modules.pomodoro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.pomodorobot.core.exception.BusinessException;
import br.com.pomodorobot.core.time.TimeProvider;
import br.com.pomodorobot.modules.guild.domain.GuildSettings;
import br.com.pomodorobot.modules.guild.service.GuildSettingsService;
import br.com.pomodorobot.modules.pomodoro.domain.PomodoroSession;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroSessionResponse;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroStartRequest;
import br.com.pomodorobot.modules.pomodoro.repository.PomodoroSessionRepository;
import br.com.pomodorobot.modules.user.service.DiscordUserService;
import br.com.pomodorobot.shared.enums.PomodoroEventType;
import br.com.pomodorobot.shared.enums.PomodoroPhase;
import br.com.pomodorobot.shared.enums.PomodoroStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PomodoroSessionServiceTest {

    private static final String GUILD_ID = "guild-id";
    private static final String CHANNEL_ID = "channel-id";
    private static final String MESSAGE_ID = "message-id";
    private static final String USER_ID = "user-id";
    private static final UUID SESSION_ID = UUID.fromString("cbad75c7-7dc3-4d7a-991e-afbfcc970000");
    private static final Instant NOW = Instant.parse("2026-05-19T12:00:00Z");

    @Mock
    private PomodoroSessionRepository pomodoroSessionRepository;

    @Mock
    private PomodoroEventService pomodoroEventService;

    @Mock
    private GuildSettingsService guildSettingsService;

    @Mock
    private DiscordUserService discordUserService;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private PomodoroSessionService pomodoroSessionService;

    @Test
    void shouldStartSessionUsingGuildDefaults() {
        stubNow();
        when(pomodoroSessionRepository.findFirstByGuildIdAndUserIdAndStatusIn(GUILD_ID, USER_ID, activeStatuses()))
                .thenReturn(Optional.empty());
        when(guildSettingsService.getOrCreate(GUILD_ID)).thenReturn(defaultSettings());
        when(pomodoroSessionRepository.save(any(PomodoroSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PomodoroSessionResponse response = pomodoroSessionService.start(startRequest());

        assertThat(response.status()).isEqualTo(PomodoroStatus.RUNNING);
        assertThat(response.currentPhase()).isEqualTo(PomodoroPhase.FOCUS);
        assertThat(response.cyclesTotal()).isEqualTo(4);
        assertThat(response.currentPhaseEndsAt()).isEqualTo(NOW.plusSeconds(1500));
        verify(discordUserService).upsert(any());
        verify(pomodoroEventService).register(any(PomodoroSession.class), eq(PomodoroEventType.STARTED));
    }

    @Test
    void shouldAttachMessageToSession() {
        when(pomodoroSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(activeSession()));
        when(pomodoroSessionRepository.save(any(PomodoroSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PomodoroSessionResponse response = pomodoroSessionService.attachMessage(SESSION_ID, USER_ID, MESSAGE_ID);

        assertThat(response.messageId()).isEqualTo(MESSAGE_ID);
        verify(pomodoroSessionRepository).save(any(PomodoroSession.class));
    }

    @Test
    void shouldRejectButtonActionFromAnotherUser() {
        when(pomodoroSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(activeSession()));

        assertThatThrownBy(() -> pomodoroSessionService.statusFromButton(SESSION_ID, "other-user-id"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Only the session owner can use these controls.");
    }

    @Test
    void shouldRejectStartWhenUserAlreadyHasActiveSession() {
        when(pomodoroSessionRepository.findFirstByGuildIdAndUserIdAndStatusIn(GUILD_ID, USER_ID, activeStatuses()))
                .thenReturn(Optional.of(activeSession()));

        assertThatThrownBy(() -> pomodoroSessionService.start(startRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("You already have an active Pomodoro session.");
    }

    @Test
    void shouldPauseRunningSession() {
        stubNow();
        when(pomodoroSessionRepository.findFirstByGuildIdAndUserIdAndStatusIn(GUILD_ID, USER_ID, activeStatuses()))
                .thenReturn(Optional.of(activeSession()));
        when(pomodoroSessionRepository.save(any(PomodoroSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PomodoroSessionResponse response = pomodoroSessionService.pause(GUILD_ID, USER_ID);

        assertThat(response.status()).isEqualTo(PomodoroStatus.PAUSED);
        assertThat(response.currentPhaseEndsAt()).isEqualTo(NOW.plusSeconds(1500));
        verify(pomodoroEventService).register(any(PomodoroSession.class), eq(PomodoroEventType.PAUSED));
    }

    @Test
    void shouldResetReminderTimestampsWhenPhaseChanges() {
        stubNow();
        PomodoroSession session = activeSession();
        session.setTenMinuteReminderSentAt(NOW.minusSeconds(900));
        session.setFiveMinuteReminderSentAt(NOW.minusSeconds(600));
        session.setCurrentPhaseEndsAt(NOW);
        when(pomodoroSessionRepository.findByStatusAndCurrentPhaseEndsAtLessThanEqual(PomodoroStatus.RUNNING, NOW))
                .thenReturn(List.of(session));
        when(pomodoroSessionRepository.save(any(PomodoroSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<PomodoroSessionResponse> responses = pomodoroSessionService.completeDueSessions();

        assertThat(responses).hasSize(1);
        assertThat(session.getTenMinuteReminderSentAt()).isNull();
        assertThat(session.getFiveMinuteReminderSentAt()).isNull();
    }

    private void stubNow() {
        when(timeProvider.now()).thenReturn(NOW);
    }

    private PomodoroStartRequest startRequest() {
        return PomodoroStartRequest.builder()
                .guildId(GUILD_ID)
                .channelId(CHANNEL_ID)
                .userId(USER_ID)
                .username("student")
                .build();
    }

    private GuildSettings defaultSettings() {
        return GuildSettings.builder()
                .guildId(GUILD_ID)
                .defaultFocusMinutes(25)
                .defaultShortBreakMinutes(5)
                .defaultLongBreakMinutes(15)
                .defaultCycles(4)
                .timezone("America/Sao_Paulo")
                .build();
    }

    private PomodoroSession activeSession() {
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
                .startedAt(NOW)
                .currentPhaseStartedAt(NOW)
                .currentPhaseEndsAt(NOW.plusSeconds(1500))
                .build();
    }

    private List<PomodoroStatus> activeStatuses() {
        return List.of(PomodoroStatus.RUNNING, PomodoroStatus.PAUSED);
    }
}
