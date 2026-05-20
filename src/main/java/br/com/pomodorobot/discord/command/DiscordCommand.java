package br.com.pomodorobot.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface DiscordCommand {

    String name();

    CommandData commandData();

    void handle(SlashCommandInteractionEvent event);
}
