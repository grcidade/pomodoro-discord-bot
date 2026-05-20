package br.com.pomodorobot.modules.pomodoro.command;

import br.com.pomodorobot.core.exception.BusinessException;
import br.com.pomodorobot.discord.command.DiscordCommand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PomodoroDiscordCommand implements DiscordCommand {

    private static final String COMMAND_NAME = "pomodoro";

    private final List<PomodoroSubcommandHandler> subcommandHandlers;

    @Override
    public String name() {
        return COMMAND_NAME;
    }

    @Override
    public CommandData commandData() {
        return Commands.slash(COMMAND_NAME, "Manage Pomodoro study sessions.")
                .addSubcommands(
                        startCommand(),
                        simpleCommand("pause", "Pause your active Pomodoro session."),
                        simpleCommand("resume", "Resume your paused Pomodoro session."),
                        simpleCommand("stop", "Cancel your active Pomodoro session."),
                        simpleCommand("status", "Show your active Pomodoro session."),
                        configCommand(),
                        simpleCommand("stats", "Show your Pomodoro stats.")
                );
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String subcommandName = event.getSubcommandName();
        if (subcommandName == null) {
            throw new BusinessException("Missing Pomodoro subcommand.");
        }

        PomodoroSubcommandHandler handler = findHandler(subcommandName);
        handler.handle(event);
    }

    private PomodoroSubcommandHandler findHandler(String subcommandName) {
        return subcommandHandlers.stream()
                .filter(handler -> handler.subcommandName().equals(subcommandName))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Unsupported Pomodoro subcommand."));
    }

    private SubcommandData startCommand() {
        return new SubcommandData("start", "Start a Pomodoro session.")
                .addOption(OptionType.INTEGER, "focus_minutes", "Focus duration in minutes.", false)
                .addOption(OptionType.INTEGER, "short_break_minutes", "Short break duration in minutes.", false)
                .addOption(OptionType.INTEGER, "long_break_minutes", "Long break duration in minutes.", false)
                .addOption(OptionType.INTEGER, "cycles", "Number of focus cycles.", false);
    }

    private SubcommandData configCommand() {
        return new SubcommandData("config", "Update this server Pomodoro defaults.")
                .addOption(OptionType.INTEGER, "focus_minutes", "Default focus duration in minutes.", false)
                .addOption(OptionType.INTEGER, "short_break_minutes", "Default short break duration in minutes.", false)
                .addOption(OptionType.INTEGER, "long_break_minutes", "Default long break duration in minutes.", false)
                .addOption(OptionType.INTEGER, "cycles", "Default number of focus cycles.", false);
    }

    private SubcommandData simpleCommand(String name, String description) {
        return new SubcommandData(name, description);
    }
}
