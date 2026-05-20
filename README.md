# Pomodoro Bot

Discord Pomodoro bot built with Java 21, Spring Boot, JDA, PostgreSQL and Flyway.

## Overview

Pomodoro Bot helps Discord users run focused study sessions directly from slash commands. It stores session state in PostgreSQL, advances phases with a scheduler, updates the main Discord message with interactive controls and sends reminders before a phase ends.

## Project Highlights

- Modular Spring Boot code organized by domain and integration boundaries.
- Discord integration isolated behind command handlers, listeners and notification services.
- PostgreSQL schema managed with Flyway migrations.
- Session lifecycle covered by unit tests for core rules, buttons, embeds and reminders.

## Stack

- Java 21
- Spring Boot 4
- Spring Data JPA
- Flyway
- PostgreSQL
- JDA
- JUnit 5, Mockito and AssertJ
- Docker Compose for local PostgreSQL

## Architecture

- `discord`: JDA configuration, listeners, command routing and outbound notifications.
- `modules/pomodoro`: Pomodoro session lifecycle, reminders, events, DTOs, mappers and Discord command handlers.
- `modules/guild`: server-specific Pomodoro defaults.
- `modules/user`: Discord user snapshot persistence.
- `core`: shared infrastructure such as scheduling, configuration, time provider and exceptions.
- `shared`: enums and validation rules reused across modules.

## Discord Commands

- `/pomodoro start`: starts a Pomodoro session. Optional parameters: `focus_minutes`, `short_break_minutes`, `long_break_minutes`, `cycles`.
- `/pomodoro pause`: pauses the active session.
- `/pomodoro resume`: resumes a paused session.
- `/pomodoro stop`: cancels the active session.
- `/pomodoro status`: shows the current session status.
- `/pomodoro config`: updates server defaults. Requires `Manage Server` permission.
- `/pomodoro stats`: shows completed session count for the user.

Invite the bot with these scopes:

- `bot`
- `applications.commands`

## Local Setup

1. Start PostgreSQL:

```bash
docker compose up -d
```

2. Configure environment variables. You can use `.env.example` as a reference:

```bash
DISCORD_BOT_TOKEN=your-discord-bot-token
DATABASE_URL=jdbc:postgresql://localhost:5432/pomodoro_bot
DATABASE_USERNAME=pomodoro
DATABASE_PASSWORD=pomodoro
```

3. Run the application:

```bash
mvn spring-boot:run
```

## Database

Flyway runs automatically on startup and validates the schema through Hibernate with `ddl-auto: validate`.

The initial migrations create:

- guild settings;
- Discord user snapshots;
- Pomodoro sessions;
- Pomodoro events;
- indexes for active sessions and due phase checks.

## Tests

Run the unit test suite with:

```bash
mvn test
```

The current tests focus on Pomodoro lifecycle rules, reminders, mapper output, Discord embeds and button behavior.

## Configuration

| Variable | Description | Default |
| --- | --- | --- |
| `DISCORD_BOT_TOKEN` | Discord bot token. Required. | none |
| `DATABASE_URL` | PostgreSQL JDBC URL. | `jdbc:postgresql://localhost:5432/pomodoro_bot` |
| `DATABASE_USERNAME` | PostgreSQL username. | `pomodoro` |
| `DATABASE_PASSWORD` | PostgreSQL password. | `pomodoro` |
