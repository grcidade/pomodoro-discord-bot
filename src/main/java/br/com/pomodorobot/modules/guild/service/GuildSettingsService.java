package br.com.pomodorobot.modules.guild.service;

import br.com.pomodorobot.modules.guild.domain.GuildSettings;
import br.com.pomodorobot.modules.guild.dto.GuildSettingsRequest;
import br.com.pomodorobot.modules.guild.dto.GuildSettingsResponse;
import br.com.pomodorobot.modules.guild.mapper.GuildSettingsMapper;
import br.com.pomodorobot.modules.guild.repository.GuildSettingsRepository;
import br.com.pomodorobot.shared.validation.PomodoroValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuildSettingsService {

    private final GuildSettingsRepository guildSettingsRepository;

    @Transactional
    public GuildSettings getOrCreate(String guildId) {
        return guildSettingsRepository.findById(guildId)
                .orElseGet(() -> guildSettingsRepository.save(GuildSettings.createDefault(guildId)));
    }

    @Transactional
    public GuildSettingsResponse update(GuildSettingsRequest request) {
        GuildSettings settings = getOrCreate(request.guildId());

        applyFocusMinutes(settings, request.defaultFocusMinutes());
        applyShortBreakMinutes(settings, request.defaultShortBreakMinutes());
        applyLongBreakMinutes(settings, request.defaultLongBreakMinutes());
        applyCycles(settings, request.defaultCycles());
        applyNotificationChannel(settings, request.notificationChannelId());

        GuildSettings savedSettings = guildSettingsRepository.save(settings);
        return GuildSettingsMapper.toResponse(savedSettings);
    }

    private void applyFocusMinutes(GuildSettings settings, Integer value) {
        if (value == null) {
            return;
        }

        PomodoroValidation.validateMinutes("focus minutes", value);
        settings.setDefaultFocusMinutes(value);
    }

    private void applyShortBreakMinutes(GuildSettings settings, Integer value) {
        if (value == null) {
            return;
        }

        PomodoroValidation.validateMinutes("short break minutes", value);
        settings.setDefaultShortBreakMinutes(value);
    }

    private void applyLongBreakMinutes(GuildSettings settings, Integer value) {
        if (value == null) {
            return;
        }

        PomodoroValidation.validateMinutes("long break minutes", value);
        settings.setDefaultLongBreakMinutes(value);
    }

    private void applyCycles(GuildSettings settings, Integer value) {
        if (value == null) {
            return;
        }

        PomodoroValidation.validateCycles(value);
        settings.setDefaultCycles(value);
    }

    private void applyNotificationChannel(GuildSettings settings, String channelId) {
        if (channelId == null || channelId.isBlank()) {
            return;
        }

        settings.setNotificationChannelId(channelId);
    }
}
