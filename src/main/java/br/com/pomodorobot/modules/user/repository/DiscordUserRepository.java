package br.com.pomodorobot.modules.user.repository;

import br.com.pomodorobot.modules.user.domain.DiscordUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscordUserRepository extends JpaRepository<DiscordUser, String> {
}
