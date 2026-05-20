package br.com.pomodorobot.modules.pomodoro.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface PomodoroSubcommandHandler {

    String subcommandName();

    void handle(SlashCommandInteractionEvent event);
}
