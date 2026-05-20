package br.com.pomodorobot.modules.user.service;

import br.com.pomodorobot.modules.user.domain.DiscordUser;
import br.com.pomodorobot.modules.user.dto.DiscordUserRequest;
import br.com.pomodorobot.modules.user.dto.DiscordUserResponse;
import br.com.pomodorobot.modules.user.mapper.DiscordUserMapper;
import br.com.pomodorobot.modules.user.repository.DiscordUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiscordUserService {

    private final DiscordUserRepository discordUserRepository;

    @Transactional
    public DiscordUserResponse upsert(DiscordUserRequest request) {
        DiscordUser discordUser = discordUserRepository.findById(request.userId())
                .orElseGet(() -> DiscordUser.builder()
                        .userId(request.userId())
                        .usernameSnapshot(request.username())
                        .build());

        discordUser.setUsernameSnapshot(request.username());

        DiscordUser savedUser = discordUserRepository.save(discordUser);
        return DiscordUserMapper.toResponse(savedUser);
    }
}
