package br.com.pomodorobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan
public class PomodoroBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(PomodoroBotApplication.class, args);
    }
}
