package br.com.pomodorobot.core.scheduler;

import br.com.pomodorobot.discord.notification.DiscordNotificationService;
import br.com.pomodorobot.modules.pomodoro.command.PomodoroButtonFactory;
import br.com.pomodorobot.modules.pomodoro.command.PomodoroEmbedFactory;
import br.com.pomodorobot.modules.pomodoro.command.PomodoroMainMessageService;
import br.com.pomodorobot.modules.pomodoro.command.PomodoroReminderMessageFactory;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroReminderResponse;
import br.com.pomodorobot.modules.pomodoro.dto.PomodoroSessionResponse;
import br.com.pomodorobot.modules.pomodoro.service.PomodoroReminderService;
import br.com.pomodorobot.modules.pomodoro.service.PomodoroSessionService;
import br.com.pomodorobot.shared.enums.PomodoroStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PomodoroSessionScheduler {

    private final PomodoroSessionService pomodoroSessionService;
    private final PomodoroReminderService pomodoroReminderService;
    private final DiscordNotificationService discordNotificationService;
    private final PomodoroMainMessageService pomodoroMainMessageService;

    @Scheduled(fixedDelayString = "${pomodoro.scheduler.due-session-check-rate-ms:15000}")
    public void completeDueSessions() {
        List<PomodoroSessionResponse> completedSessions = pomodoroSessionService.completeDueSessions();
        log.debug("Pomodoro due session check finished. sessionsProcessed={}", completedSessions.size());
        completedSessions.forEach(this::notifySessionUpdate);

        List<PomodoroReminderResponse> reminders = pomodoroReminderService.collectDueReminders();
        log.debug("Pomodoro reminder check finished. remindersSent={}", reminders.size());
        reminders.forEach(this::sendReminder);
    }

    private void notifySessionUpdate(PomodoroSessionResponse response) {
        log.info(
                "Pomodoro session update notification. sessionId={}, status={}, phase={}",
                response.id(),
                response.status(),
                response.currentPhase()
        );

        if (pomodoroMainMessageService.update(response)) {
            return;
        }

        if (response.status() == PomodoroStatus.COMPLETED) {
            discordNotificationService.sendEmbed(
                    response.channelId(),
                    PomodoroEmbedFactory.completed(response),
                    PomodoroButtonFactory.controls(response)
            );
            return;
        }

        discordNotificationService.sendEmbed(
                response.channelId(),
                PomodoroEmbedFactory.phaseChanged(response),
                PomodoroButtonFactory.controls(response)
        );
    }

    private void sendReminder(PomodoroReminderResponse response) {
        log.info(
                "Pomodoro reminder notification. sessionId={}, type={}",
                response.sessionId(),
                response.type()
        );

        discordNotificationService.sendMessage(
                response.channelId(),
                PomodoroReminderMessageFactory.reminder(response)
        );
    }
}
