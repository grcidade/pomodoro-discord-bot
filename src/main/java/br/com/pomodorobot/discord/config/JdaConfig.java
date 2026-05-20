package br.com.pomodorobot.discord.config;

import br.com.pomodorobot.discord.command.DiscordCommand;
import br.com.pomodorobot.discord.listener.DiscordSlashCommandListener;
import br.com.pomodorobot.modules.pomodoro.command.PomodoroButtonInteractionListener;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JdaConfig {

    private final DiscordProperties discordProperties;
    private final DiscordSlashCommandListener discordSlashCommandListener;
    private final PomodoroButtonInteractionListener pomodoroButtonInteractionListener;
    private final List<DiscordCommand> discordCommands;

    @Bean(destroyMethod = "shutdown")
    public JDA jda() {
        JDA jda = JDABuilder.createDefault(discordProperties.token())
                .addEventListeners(discordSlashCommandListener, pomodoroButtonInteractionListener)
                .build();

        registerCommands(jda);

        return jda;
    }

    private void registerCommands(JDA jda) {
        List<CommandData> commandData = discordCommands.stream()
                .map(DiscordCommand::commandData)
                .toList();

        jda.updateCommands()
                .addCommands(commandData)
                .queue();
    }
}
