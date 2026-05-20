package br.com.pomodorobot.modules.pomodoro.service;

import br.com.pomodorobot.core.exception.BusinessException;
import br.com.pomodorobot.core.time.TimeProvider;
import br.com.pomodorobot.modules.guild.domain.GuildSettings;
import br.com.pomodorobot.modules.guild.service.GuildSettingsService;
import br.com.pomodorobot.modules.pomodoro.domain.PomodoroSession;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroSessionResponse;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroStartRequest;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroStatsResponse;
import br.com.pomodorobot.modules.pomodoro.mapper.PomodoroSessionMapper;
import br.com.pomodorobot.modules.pomodoro.repository.PomodoroSessionRepository;
import br.com.pomodorobot.modules.user.dto.DiscordUserRequest;
import br.com.pomodorobot.modules.user.service.DiscordUserService;
import br.com.pomodorobot.shared.enums.PomodoroEventType;
import br.com.pomodorobot.shared.enums.PomodoroPhase;
import br.com.pomodorobot.shared.enums.PomodoroStatus;
import br.com.pomodorobot.shared.validation.PomodoroValidation;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PomodoroSessionService {

    private static final List<PomodoroStatus> ACTIVE_STATUSES = List.of(PomodoroStatus.RUNNING, PomodoroStatus.PAUSED);
    private static final long MINIMUM_RESUME_SECONDS = 1L;
    private static final int LONG_BREAK_INTERVAL = 4;

    private final PomodoroSessionRepository pomodoroSessionRepository;
    private final PomodoroEventService pomodoroEventService;
    private final GuildSettingsService guildSettingsService;
    private final DiscordUserService discordUserService;
    private final TimeProvider timeProvider;

    @Transactional
    public PomodoroSessionResponse start(PomodoroStartRequest request) {
        ensureNoActiveSession(request.guildId(), request.userId());
        discordUserService.upsert(DiscordUserRequest.builder()
                .userId(request.userId())
                .username(request.username())
                .build());

        GuildSettings settings = guildSettingsService.getOrCreate(request.guildId());
        PomodoroSession session = buildNewSession(request, settings);
        PomodoroSession savedSession = pomodoroSessionRepository.save(session);
        pomodoroEventService.register(savedSession, PomodoroEventType.STARTED);

        return toResponse(savedSession);
    }

    @Transactional
    public PomodoroSessionResponse pause(String guildId, String userId) {
        PomodoroSession session = findActiveSession(guildId, userId);
        return pauseSession(session);
    }

    @Transactional
    public PomodoroSessionResponse pauseFromButton(UUID sessionId, String userId) {
        PomodoroSession session = findSessionForButton(sessionId, userId);
        return pauseSession(session);
    }

    private PomodoroSessionResponse pauseSession(PomodoroSession session) {
        ensureRunning(session);

        session.setStatus(PomodoroStatus.PAUSED);
        session.setPausedAt(timeProvider.now());

        PomodoroSession savedSession = pomodoroSessionRepository.save(session);
        pomodoroEventService.register(savedSession, PomodoroEventType.PAUSED);

        return toResponse(savedSession);
    }

    @Transactional
    public PomodoroSessionResponse resume(String guildId, String userId) {
        PomodoroSession session = findActiveSession(guildId, userId);
        return resumeSession(session);
    }

    @Transactional
    public PomodoroSessionResponse resumeFromButton(UUID sessionId, String userId) {
        PomodoroSession session = findSessionForButton(sessionId, userId);
        return resumeSession(session);
    }

    private PomodoroSessionResponse resumeSession(PomodoroSession session) {
        ensurePaused(session);

        Instant now = timeProvider.now();
        long secondsUntilPhaseEnd = calculateSecondsUntilPhaseEndOnResume(session);
        session.setStatus(PomodoroStatus.RUNNING);
        session.setPausedAt(null);
        session.setCurrentPhaseStartedAt(now);
        session.setCurrentPhaseEndsAt(now.plusSeconds(secondsUntilPhaseEnd));

        PomodoroSession savedSession = pomodoroSessionRepository.save(session);
        pomodoroEventService.register(savedSession, PomodoroEventType.RESUMED);

        return toResponse(savedSession);
    }

    @Transactional
    public PomodoroSessionResponse stop(String guildId, String userId) {
        PomodoroSession session = findActiveSession(guildId, userId);
        return stopSession(session);
    }

    @Transactional
    public PomodoroSessionResponse stopFromButton(UUID sessionId, String userId) {
        PomodoroSession session = findSessionForButton(sessionId, userId);
        return stopSession(session);
    }

    private PomodoroSessionResponse stopSession(PomodoroSession session) {
        ensureActive(session);
        Instant now = timeProvider.now();

        session.setStatus(PomodoroStatus.CANCELLED);
        session.setEndedAt(now);

        PomodoroSession savedSession = pomodoroSessionRepository.save(session);
        pomodoroEventService.register(savedSession, PomodoroEventType.CANCELLED);

        return toResponse(savedSession);
    }

    @Transactional(readOnly = true)
    public PomodoroSessionResponse status(String guildId, String userId) {
        return toResponse(findActiveSession(guildId, userId));
    }

    @Transactional(readOnly = true)
    public PomodoroSessionResponse statusFromButton(UUID sessionId, String userId) {
        return toResponse(findSessionForButton(sessionId, userId));
    }

    @Transactional(readOnly = true)
    public PomodoroStatsResponse stats(String guildId, String userId) {
        long completedSessions = pomodoroSessionRepository.countByGuildIdAndUserIdAndStatus(
                guildId,
                userId,
                PomodoroStatus.COMPLETED
        );

        return PomodoroStatsResponse.builder()
                .guildId(guildId)
                .userId(userId)
                .completedSessions(completedSessions)
                .build();
    }

    @Transactional
    public PomodoroSessionResponse attachMessage(UUID sessionId, String userId, String messageId) {
        PomodoroSession session = findSessionForButton(sessionId, userId);
        session.setMessageId(messageId);
        PomodoroSession savedSession = pomodoroSessionRepository.save(session);
        log.info("Pomodoro main message attached. sessionId={}, messageId={}", sessionId, messageId);
        return toResponse(savedSession);
    }

    @Transactional
    public List<PomodoroSessionResponse> completeDueSessions() {
        Instant now = timeProvider.now();
        List<PomodoroSession> dueSessions = pomodoroSessionRepository.findByStatusAndCurrentPhaseEndsAtLessThanEqual(
                PomodoroStatus.RUNNING,
                now
        );

        return dueSessions.stream()
                .map(session -> advanceSession(session, now))
                .map(this::toResponse)
                .toList();
    }

    private PomodoroSession buildNewSession(PomodoroStartRequest request, GuildSettings settings) {
        int focusMinutes = resolveMinutes("focus minutes", request.focusMinutes(), settings.getDefaultFocusMinutes());
        int shortBreakMinutes = resolveMinutes(
                "short break minutes",
                request.shortBreakMinutes(),
                settings.getDefaultShortBreakMinutes()
        );
        int longBreakMinutes = resolveMinutes(
                "long break minutes",
                request.longBreakMinutes(),
                settings.getDefaultLongBreakMinutes()
        );
        int cycles = resolveCycles(request.cycles(), settings.getDefaultCycles());
        Instant now = timeProvider.now();

        return PomodoroSession.builder()
                .guildId(request.guildId())
                .channelId(request.channelId())
                .userId(request.userId())
                .status(PomodoroStatus.RUNNING)
                .currentPhase(PomodoroPhase.FOCUS)
                .focusMinutes(focusMinutes)
                .shortBreakMinutes(shortBreakMinutes)
                .longBreakMinutes(longBreakMinutes)
                .cyclesTotal(cycles)
                .cyclesCompleted(0)
                .startedAt(now)
                .currentPhaseStartedAt(now)
                .currentPhaseEndsAt(now.plus(Duration.ofMinutes(focusMinutes)))
                .build();
    }

    private PomodoroSession advanceSession(PomodoroSession session, Instant now) {
        if (session.getCurrentPhase() == PomodoroPhase.FOCUS) {
            return completeFocusPhase(session, now);
        }

        return startNextFocusPhase(session, now);
    }

    private PomodoroSession completeFocusPhase(PomodoroSession session, Instant now) {
        int completedCycles = session.getCyclesCompleted() + 1;
        session.setCyclesCompleted(completedCycles);
        pomodoroEventService.register(session, PomodoroEventType.PHASE_COMPLETED);

        if (completedCycles >= session.getCyclesTotal()) {
            return completeSession(session, now);
        }

        PomodoroPhase nextPhase = resolveBreakPhase(completedCycles);
        int breakMinutes = resolveBreakMinutes(session, nextPhase);
        session.setCurrentPhase(nextPhase);
        session.setCurrentPhaseStartedAt(now);
        session.setCurrentPhaseEndsAt(now.plus(Duration.ofMinutes(breakMinutes)));
        resetReminderTimestamps(session);

        return pomodoroSessionRepository.save(session);
    }

    private PomodoroSession completeSession(PomodoroSession session, Instant now) {
        session.setStatus(PomodoroStatus.COMPLETED);
        session.setEndedAt(now);
        PomodoroSession savedSession = pomodoroSessionRepository.save(session);
        pomodoroEventService.register(savedSession, PomodoroEventType.COMPLETED);
        return savedSession;
    }

    private PomodoroSession startNextFocusPhase(PomodoroSession session, Instant now) {
        pomodoroEventService.register(session, PomodoroEventType.PHASE_COMPLETED);
        session.setCurrentPhase(PomodoroPhase.FOCUS);
        session.setCurrentPhaseStartedAt(now);
        session.setCurrentPhaseEndsAt(now.plus(Duration.ofMinutes(session.getFocusMinutes())));
        resetReminderTimestamps(session);
        return pomodoroSessionRepository.save(session);
    }

    private int resolveMinutes(String fieldName, Integer requestedValue, int defaultValue) {
        int value = requestedValue == null ? defaultValue : requestedValue;
        PomodoroValidation.validateMinutes(fieldName, value);
        return value;
    }

    private int resolveCycles(Integer requestedValue, int defaultValue) {
        int value = requestedValue == null ? defaultValue : requestedValue;
        PomodoroValidation.validateCycles(value);
        return value;
    }

    private PomodoroPhase resolveBreakPhase(int completedCycles) {
        if (completedCycles % LONG_BREAK_INTERVAL == 0) {
            return PomodoroPhase.LONG_BREAK;
        }

        return PomodoroPhase.SHORT_BREAK;
    }

    private int resolveBreakMinutes(PomodoroSession session, PomodoroPhase phase) {
        if (phase == PomodoroPhase.LONG_BREAK) {
            return session.getLongBreakMinutes();
        }

        return session.getShortBreakMinutes();
    }

    private void ensureNoActiveSession(String guildId, String userId) {
        pomodoroSessionRepository.findFirstByGuildIdAndUserIdAndStatusIn(guildId, userId, ACTIVE_STATUSES)
                .ifPresent(session -> {
                    throw new BusinessException("You already have an active Pomodoro session.");
                });
    }

    private PomodoroSession findActiveSession(String guildId, String userId) {
        return pomodoroSessionRepository.findFirstByGuildIdAndUserIdAndStatusIn(guildId, userId, ACTIVE_STATUSES)
                .orElseThrow(() -> new BusinessException("No active Pomodoro session found."));
    }

    private PomodoroSession findSessionForButton(UUID sessionId, String userId) {
        PomodoroSession session = pomodoroSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("Pomodoro session not found."));

        ensureSessionOwner(session, userId);
        return session;
    }

    private void ensureSessionOwner(PomodoroSession session, String userId) {
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException("Only the session owner can use these controls.");
        }
    }

    private void ensureRunning(PomodoroSession session) {
        if (session.getStatus() != PomodoroStatus.RUNNING) {
            throw new BusinessException("The Pomodoro session is not running.");
        }
    }

    private void ensurePaused(PomodoroSession session) {
        if (session.getStatus() != PomodoroStatus.PAUSED) {
            throw new BusinessException("The Pomodoro session is not paused.");
        }
    }

    private void ensureActive(PomodoroSession session) {
        if (!session.isActive()) {
            throw new BusinessException("The Pomodoro session is not active.");
        }
    }

    private long calculateSecondsUntilPhaseEndOnResume(PomodoroSession session) {
        long secondsUntilPhaseEnd = Duration.between(session.getPausedAt(), session.getCurrentPhaseEndsAt()).toSeconds();
        return Math.max(secondsUntilPhaseEnd, MINIMUM_RESUME_SECONDS);
    }

    private PomodoroSessionResponse toResponse(PomodoroSession session) {
        return PomodoroSessionMapper.toResponse(session);
    }

    private void resetReminderTimestamps(PomodoroSession session) {
        session.setTenMinuteReminderSentAt(null);
        session.setFiveMinuteReminderSentAt(null);
    }
}
