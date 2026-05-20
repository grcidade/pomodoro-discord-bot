package br.com.pomodorobot.modules.pomodoro.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.pomodorobot.modules.pomodoro.dto.PomodoroSessionResponse;
import br.com.pomodorobot.modules.pomodoro.service.PomodoroSessionService;
import br.com.pomodorobot.shared.enums.PomodoroPhase;
import br.com.pomodorobot.shared.enums.PomodoroStatus;
import java.time.Instant;
import java.util.UUID;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PomodoroButtonInteractionListenerTest {

    private static final UUID SESSION_ID = UUID.fromString("cbad75c7-7dc3-4d7a-991e-afbfcc970000");
    private static final String USER_ID = "user-id";

    @Mock
    private PomodoroSessionService pomodoroSessionService;

    @Mock
    private PomodoroMainMessageService pomodoroMainMessageService;

    @Mock
    private ButtonInteractionEvent event;

    @Mock
    private User user;

    @Mock
    private ReplyCallbackAction replyCallbackAction;

    @Mock
    private MessageEditCallbackAction messageEditCallbackAction;

    @InjectMocks
    private PomodoroButtonInteractionListener listener;

    @Test
    void shouldPauseSessionFromButton() {
        stubUser();
        stubDeferEdit();
        when(event.getComponentId()).thenReturn("pomodoro:pause:" + SESSION_ID);
        when(pomodoroSessionService.pauseFromButton(SESSION_ID, USER_ID)).thenReturn(sessionResponse(PomodoroStatus.PAUSED));

        listener.onButtonInteraction(event);

        verify(pomodoroSessionService).pauseFromButton(SESSION_ID, USER_ID);
        verify(pomodoroMainMessageService).update(any(PomodoroSessionResponse.class));
        verify(event, never()).replyEmbeds(any(MessageEmbed.class));
        verify(messageEditCallbackAction).queue();
    }

    @Test
    void shouldReplyStatusWithoutUpdatingMainMessage() {
        stubUser();
        stubReplyEmbeds();
        when(event.getComponentId()).thenReturn("pomodoro:status:" + SESSION_ID);
        when(pomodoroSessionService.statusFromButton(SESSION_ID, USER_ID)).thenReturn(sessionResponse(PomodoroStatus.RUNNING));

        listener.onButtonInteraction(event);

        verify(pomodoroSessionService).statusFromButton(SESSION_ID, USER_ID);
        verify(pomodoroMainMessageService, never()).update(any());
        verify(replyCallbackAction).queue();
    }

    private void stubUser() {
        when(event.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(USER_ID);
    }

    private void stubReplyEmbeds() {
        when(event.replyEmbeds(any(MessageEmbed.class))).thenReturn(replyCallbackAction);
        when(replyCallbackAction.setEphemeral(true)).thenReturn(replyCallbackAction);
    }

    private void stubDeferEdit() {
        when(event.deferEdit()).thenReturn(messageEditCallbackAction);
    }

    private PomodoroSessionResponse sessionResponse(PomodoroStatus status) {
        return PomodoroSessionResponse.builder()
                .id(SESSION_ID)
                .guildId("guild-id")
                .channelId("channel-id")
                .messageId("message-id")
                .userId(USER_ID)
                .status(status)
                .currentPhase(PomodoroPhase.FOCUS)
                .cyclesTotal(4)
                .cyclesCompleted(1)
                .currentPhaseEndsAt(Instant.parse("2026-05-19T12:25:00Z"))
                .build();
    }
}
