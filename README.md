# AntiGhostPlugin

Server-side companion plugin for the [AntiGhost](https://modrinth.com/mod/antighost) mod.  
Requires **Spigot 1.20** or newer.

## Download

- [GitHub](https://github.com/VidTu/AntiGhostPlugin/releases)

## Building

You will need:

- Java JDK 17 or higher. (e.g. [Temurin](https://adoptium.net/))
- Some amount of RAM.
- A bit of storage.

How to:

- Ensure your JDK is set up properly. (i.e. JDK path is in `JAVA_HOME` environment variable)
- Clone this repo or download it. (e.g. via `git clone https://github.com/VidTu/AntiGhostPlugin`)
- Open the terminal (command prompt) in the repository folder.
- Run `./gradlew build`. (`gradlew build` for command prompt)
- Grab JAR from `build/libs/`.

## License

This project is licensed under [MIT License](https://github.com/VidTu/AntiGhostPlugin/blob/main/LICENSE).

## Config

```yaml
# Available modes:
# ENABLED - Uses default vanilla strategy.
# DISABLED - Disables the mod.
# CUSTOM - Uses server resend strategy via custom payloads with configurable radius.
mode: ENABLED

# Radius for CUSTOM mode.
# Setting to 0 will disable sending block updates, but will allow other plugins receive block update requests.
radius: 4

# Rate-limit for CUSTOM mode in milliseconds.
# Setting to 0 will disable it.
rateLimit: 1000
```

## API

See [PlayerAntiGhostRegisterEvent](src/main/java/ru/vidtu/antighostplugin/events/PlayerAntiGhostRegisterEvent.java)
and [PlayerAntiGhostRequestEvent](src/main/java/ru/vidtu/antighostplugin/events/PlayerAntiGhostRequestEvent.java).