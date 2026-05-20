package br.com.pomodorobot.modules.pomodoro.command;

import br.com.pomodorobot.core.exception.BusinessException;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

@UtilityClass
public class PomodoroCommandSupport {

    public String guildId(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) {
            throw new BusinessException("This command must be used inside a Discord server.");
        }

        return event.getGuild().getId();
    }

    public String channelId(SlashCommandInteractionEvent event) {
        return event.getChannel().getId();
    }

    public String userId(SlashCommandInteractionEvent event) {
        return event.getUser().getId();
    }

    public String username(SlashCommandInteractionEvent event) {
        return event.getUser().getName();
    }

    public Integer integerOption(SlashCommandInteractionEvent event, String optionName) {
        OptionMapping option = event.getOption(optionName);
        if (option == null) {
            return null;
        }

        return option.getAsInt();
    }
}
