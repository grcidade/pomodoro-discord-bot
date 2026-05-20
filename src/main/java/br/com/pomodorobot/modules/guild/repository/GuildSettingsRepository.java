package br.com.pomodorobot.modules.guild.repository;

import br.com.pomodorobot.modules.guild.domain.GuildSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuildSettingsRepository extends JpaRepository<GuildSettings, String> {
}
