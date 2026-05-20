package br.com.pomodorobot.core.config;

import lombok.Builder;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Builder
@ConfigurationProperties(prefix = "pomodoro.scheduler")
public record PomodoroSchedulerProperties(
        long dueSessionCheckRateMs
) {
}
