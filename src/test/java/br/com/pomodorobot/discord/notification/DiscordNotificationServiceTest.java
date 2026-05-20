package br.com.pomodorobot.discord.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
class DiscordNotificationServiceTest {

    private static final String CHANNEL_ID = "channel-id";
    private static final String MESSAGE_ID = "message-id";
    private static final String MESSAGE = "message";

    @Mock
    private ObjectProvider<JDA> jdaProvider;

    @Mock
    private JDA jda;

    @Mock
    private TextChannel channel;

    @Mock
    private MessageCreateAction messageCreateAction;

    @Mock
    private MessageEditAction messageEditAction;

    @Mock
    private MessageEmbed embed;

    @Mock
    private Message sentMessage;

    @InjectMocks
    private DiscordNotificationService discordNotificationService;

    @Test
    void shouldHandleSendMessageFailureWithoutThrowing() {
        stubChannel();
        when(channel.sendMessage(MESSAGE)).thenReturn(messageCreateAction);

        discordNotificationService.sendMessage(CHANNEL_ID, MESSAGE);

        triggerQueuedFailure(messageCreateAction);
    }

    @Test
    void shouldHandleSendEmbedFailureWithoutThrowing() {
        stubChannel();
        when(channel.sendMessageEmbeds(embed)).thenReturn(messageCreateAction);

        discordNotificationService.sendEmbed(CHANNEL_ID, embed);

        triggerQueuedFailure(messageCreateAction);
    }

    @Test
    void shouldHandleSendEmbedWithControlsFailureWithoutThrowing() {
        stubChannel();
        when(channel.sendMessageEmbeds(embed)).thenReturn(messageCreateAction);
        when(messageCreateAction.setComponents(any(ActionRow.class))).thenReturn(messageCreateAction);

        discordNotificationService.sendEmbed(CHANNEL_ID, embed, List.of(Button.primary("id", "label")));

        triggerQueuedFailure(messageCreateAction);
    }

    @Test
    void shouldHandleEditEmbedFailureWithoutThrowing() {
        stubChannel();
        when(channel.editMessageEmbedsById(MESSAGE_ID, embed)).thenReturn(messageEditAction);
        when(messageEditAction.setComponents(any(ActionRow.class))).thenReturn(messageEditAction);

        discordNotificationService.editEmbed(CHANNEL_ID, MESSAGE_ID, embed, List.of(Button.primary("id", "label")));

        triggerQueuedFailure(messageEditAction);
    }

    private void stubChannel() {
        when(jdaProvider.getIfAvailable()).thenReturn(jda);
        when(jda.getTextChannelById(CHANNEL_ID)).thenReturn(channel);
    }

    @SuppressWarnings("unchecked")
    private void triggerQueuedFailure(MessageCreateAction action) {
        ArgumentCaptor<Consumer<Message>> successCaptor = ArgumentCaptor.forClass(Consumer.class);
        ArgumentCaptor<Consumer<Throwable>> failureCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(action).queue(successCaptor.capture(), failureCaptor.capture());

        successCaptor.getValue().accept(sentMessage);
        failureCaptor.getValue().accept(new DiscordFailureStub());
    }

    @SuppressWarnings("unchecked")
    private void triggerQueuedFailure(MessageEditAction action) {
        ArgumentCaptor<Consumer<Message>> successCaptor = ArgumentCaptor.forClass(Consumer.class);
        ArgumentCaptor<Consumer<Throwable>> failureCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(action).queue(successCaptor.capture(), failureCaptor.capture());

        successCaptor.getValue().accept(sentMessage);
        failureCaptor.getValue().accept(new DiscordFailureStub());
    }

    private static class DiscordFailureStub extends RuntimeException {

        DiscordFailureStub() {
            super("Discord failure", null, false, false);
        }
    }
}
