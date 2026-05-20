package br.com.pomodorobot.discord.notification;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordNotificationService {

    private final ObjectProvider<JDA> jdaProvider;

    public void sendMessage(String channelId, String message) {
        TextChannel channel = findTextChannel(channelId);
        if (channel == null) {
            return;
        }

        channel.sendMessage(message)
                .queue(
                        sentMessage -> { },
                        error -> logNotificationFailure("send message", channelId, null, error)
                );
    }

    public void sendEmbed(String channelId, MessageEmbed embed) {
        TextChannel channel = findTextChannel(channelId);
        if (channel == null) {
            return;
        }

        channel.sendMessageEmbeds(embed)
                .queue(
                        message -> { },
                        error -> logNotificationFailure("send embed", channelId, null, error)
                );
    }

    public void sendEmbed(String channelId, MessageEmbed embed, List<Button> buttons) {
        TextChannel channel = findTextChannel(channelId);
        if (channel == null) {
            return;
        }

        channel.sendMessageEmbeds(embed)
                .setComponents(ActionRow.of(buttons))
                .queue(
                        message -> { },
                        error -> logNotificationFailure("send embed with controls", channelId, null, error)
                );
    }

    public void editEmbed(String channelId, String messageId, MessageEmbed embed, List<Button> buttons) {
        TextChannel channel = findTextChannel(channelId);
        if (channel == null) {
            return;
        }

        channel.editMessageEmbedsById(messageId, embed)
                .setComponents(ActionRow.of(buttons))
                .queue(
                        message -> { },
                        error -> logNotificationFailure("edit embed", channelId, messageId, error)
                );
    }

    private void logNotificationFailure(String operation, String channelId, String messageId, Throwable error) {
        if (messageId == null || messageId.isBlank()) {
            log.warn(
                    "Could not complete Discord notification operation. operation={}, channelId={}",
                    operation,
                    channelId,
                    error
            );
            return;
        }

        log.warn(
                "Could not complete Discord notification operation. operation={}, channelId={}, messageId={}",
                operation,
                channelId,
                messageId,
                error
        );
    }

    private TextChannel findTextChannel(String channelId) {
        JDA jda = jdaProvider.getIfAvailable();
        if (jda == null) {
            log.warn("Could not send Discord notification because JDA is not available. channelId={}", channelId);
            return null;
        }

        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            log.warn("Could not send Discord notification because channel is not a supported text channel. channelId={}", channelId);
            return null;
        }

        return channel;
    }
}
