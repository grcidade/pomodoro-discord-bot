package br.com.pomodorobot.modules.pomodoro.command;

import br.com.pomodorobot.core.exception.BusinessException;
import br.com.pomodorobot.modules.guild.dto.GuildSettingsRequest;
import br.com.pomodorobot.modules.guild.dto.GuildSettingsResponse;
import br.com.pomodorobot.modules.guild.service.GuildSettingsService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConfigPomodoroCommandHandler implements PomodoroSubcommandHandler {

    private final GuildSettingsService guildSettingsService;

    @Override
    public String subcommandName() {
        return "config";
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        ensureCanManageServer(event.getMember());

        GuildSettingsRequest request = GuildSettingsRequest.builder()
                .guildId(PomodoroCommandSupport.guildId(event))
                .defaultFocusMinutes(PomodoroCommandSupport.integerOption(event, "focus_minutes"))
                .defaultShortBreakMinutes(PomodoroCommandSupport.integerOption(event, "short_break_minutes"))
                .defaultLongBreakMinutes(PomodoroCommandSupport.integerOption(event, "long_break_minutes"))
                .defaultCycles(PomodoroCommandSupport.integerOption(event, "cycles"))
                .notificationChannelId(PomodoroCommandSupport.channelId(event))
                .build();

        GuildSettingsResponse response = guildSettingsService.update(request);

        event.replyEmbeds(PomodoroEmbedFactory.settingsUpdated(response))
                .setEphemeral(true)
                .queue();
    }

    private void ensureCanManageServer(Member member) {
        if (member == null || !member.hasPermission(Permission.MANAGE_SERVER)) {
            throw new BusinessException("You need Manage Server permission to update Pomodoro settings.");
        }
    }
}
