# ComitasBotJ

> [!IMPORTANT]
> ComitasBotJ is still in early development, if you're missing any feature, please open an issue

[![Build ComitasBotJ](https://github.com/TheHSI-HQ/ComitasBotJ/actions/workflows/maven-build.yml/badge.svg)](https://github.com/TheHSI-HQ/ComitasBotJ/actions/workflows/maven-build.yml)

## What is ComitasBotJ?
ComitasBotJ is a modular Discord bot framework that allows multiple features to run inside a single bot instance. Functionality is provided through independently loadable plugins, making it easy to extend the bot without modifying the core runtime.

Also known as the continuation of [ComitasBot](https://git.thehsi.cloud/TheHSI/ComitasBot).

## Features

- Plugin-based architecture
- Single bot instance replacing multiple single-purpose bots
- Persistent configuration system
- Hot-loadable plugin support
- Java API for developing custom plugins
- Fast startup

## Installation

### Requirements

- Java 21 or newer
- Discord Bot Token
- Internet access to communicate with Discord

### Option 1. Download the Precompiled JAR

Download the latest `comitas-core-*.jar` from the [Releases page](https://github.com/TheHSI-HQ/ComitasBotJ/releases)

Start it using

```bash
java -jar comitas-core-<version>.jar
```

### Option 2. Build from Source

1. Prerequisites:

   - Java 21 or newer
   - Maven
   - Git

2. Clone the Repo

    ```bash
    git clone https://github.com/TheHSI-HQ/ComitasBotJ
    cd ComitasBotJ
    ```

3. Install the API to the local Maven repository

    ```bash
    mvn -pl comitas-api -am clean install
    ```

4. Build ComitasBotJ (comitas-core)

    ```bash
    mvn -pl comitas-core -am clean install
    cd comitas-core
    cd target
    ```

5. Start the Server
    
    ```bash
    java --enable-native-access=ALL-UNNAMED -jar comitas-core-<version>.jar
    ```

6. Configure the Bot

    Before ComitasBotJ can connect to Discord, you must provide a valid Discord bot token.

   **In modern installs, ComitasBotJ will prompt you at first startup once for your bot token, so you can skip ahead**

   If you decide not to use First Time Startup config,
   open the generated `tokens.secret` file and add your token after `bot=`:
    
    ```properties
    bot=YOUR_DISCORD_BOT_TOKEN
    ```
    
    Save the file and restart ComitasBotJ. Once the bot successfully connects to Discord, type `invite` into the bot's terminal and press **Enter**.

   ComitasBotJ will generate an invitation link. Open the link in your browser and follow the instructions to invite the
   bot to your Discord server.

> [!IMPORTANT]
> For production use, add a [permission integer](https://discordapi.com/permissions.html#248896) to the `invite` command (e.g. `invite 248896`)!
>
> Only give the bot permissions it actually needs, in case it gets compromised.

> [!IMPORTANT]
> Due to current limitations, the bot requires all of these intents: `GUILD_MEMBERS`, `MESSAGE_CONTENT`,
> `GUILD_PRESENCES`!

## Security

Plugins run in a sandbox. Every plugin class is verified before it is loaded, and plugins can only use the
Comitas API and a safe subset of the JDK:

- Plugins can never reach the Discord token: core classes, JDA, the token file, environment variables, system
  properties, reflection on foreign classes, method handles, native code, processes, deserialization and JMX are
  unavailable. Plugin jars bundling JDA are refused.
- Everything else needs a permission, which the server owner grants per plugin:

| Permission     | Allows                                                                          |
|----------------|---------------------------------------------------------------------------------|
| `network`      | Outgoing network access (`HttpClient`, sockets, `http(s)://` URLs)              |
| `file_storage` | Writing files in the plugin's own folder, `plugins/<Plugin Name>/` (always readable) |

Each plugin sees its own, fake file system: `/` is `plugins/<Plugin Name>/` and `/mnt/<name>` are its mount points.
Without `file_storage`, the plugin can only read its folder.

When a plugin requesting permissions is installed, ComitasBotJ asks on the console:

```text
Plugin Weather 1.0 (weather.jar) requested:
 - 1. Network Access (required): Fetches forecasts from api.weather.example
 - 2. File Storage: Caches downloaded radar images
What do you grant?
 [A] All / [O] Only Required / [N] None / [1,2] Selected
 [E] All for Every Plugin / [R] Only Required for Every Plugin / [X] None for Every Plugin
```

`A` grants everything, `O` only the permissions the plugin marked as required, `N` nothing and `1,2` single permissions.
`E`, `R` and `X` do the same for every plugin from now on, without asking again (sets `plugin-permission-default` to
`all`, `required` or `none`). A plugin isn't loaded, if one of its required permissions isn't granted. Without an
interactive console (e.g. `--no-cmd` or Docker without a TTY), requested permissions are denied until they are decided.

The answers are stored in `plugins/<Plugin Name>.security.properties`, which can also be edited by hand
(changes apply on the next reload). It can additionally make host directories available to the plugin:

```properties
uuid=d152236f-96df-4be1-8f2b-6738c4194ed7
permission.network=true
permission.file_storage=false
# Visible to the plugin as /mnt/music (use / or \\ as path separator)
mount.music=D:/Music
mount.music.read-only=true
# The plugin may not request /mnt/photos anymore
denied-mount.photos=true
```

Plugins can also request a mount point while running, you're then asked on the console:

```text
Plugin Weather requested a directory as /mnt/music (read-only):
 - Plays your music collection
Do you grant it?
 [Y] Yes / [N] No / [I] Never
```

After `Y`, you enter the directory, which is saved to the security file. `I` saves `denied-mount.<name>=true`.

Directories overlapping the token, `server.properties`, `plugins/`, `plugin_data/`, `logs/` or the Comitas jar can't
be mounted.

> [!WARNING]
> The sandbox protects your token and files, but a plugin can still use the bot through the Comitas API
> (e.g. send messages or kick members, if the bot is allowed to). Only install plugins you trust with that.

## Configuration

### server.properties

The `server.properties` file is automatically created on the first startup. It is used to persistently configure ComitasBotJ.

| Property            | Type     | Default       | Description                                                                                                                                                                 |
|---------------------|----------|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `enabled`           | boolean  | `true`        | Enables or disables the bot                                                                                                                                                 |
| `load-plugins`      | boolean  | `true`        | Enables or disables the loading of plugins                                                                                                                                  |
| `allowed-plugins`   | string[] | `*`           | A comma-separated list of plugin names or UUIDs. Plugins that do not match any entry will not be loaded. Optionally, suffix the plugin name / uuid with `!sha256=FILE_HASH` |
| `bot-activity-name` | string   | `ComitasBotJ` | The activity name displayed by the bot.                                                                                                                                     |
| `plugin-permission-default` | string | `ask` | What new plugins are granted without asking: `ask` (ask on the console), `all` (not recommended), `required` or `none`                                       |

### File Structure

The following files and directories are created and used by ComitasBotJ:

```text
.
├── comitas-core-<version>.jar # The ComitasBotJ executable JAR file
├── server.properties          # The main configuration file
├── tokens.secret              # The file containing the Discord bot token
├── logs/                      # Logs from the last 365 days
│   ├── latest.log             # The current log file
│   └── ...
├── plugin_data/               # Persistent data used by plugins
│   └── <uuid>.dat             # Persistent data storage of a plugin
└── plugins/                   # Directory from which plugins are loaded
    ├── <plugin>.jar
    ├── <Plugin Name>/         # Files of a plugin, its root directory (writable with file_storage)
    └── <Plugin Name>.security.properties # Granted permissions & mount points of a plugin
```

> [!WARNING]
> Do not modify or delete files in `plugin_data/` unless you know exactly what you are doing. Plugins may rely on the data stored in this directory.


## Plugin Development

ComitasBotJ provides an API for creating custom plugins.

Plugins can:
- Register commands
- Listen to Discord events
- Store persistent data
- Extend bot functionality

See the [API documentation](docs/API.md) for more information.

## Contributing

Pull requests and plugin contributions[^1] are welcome.

## License

See [LICENSE](LICENSE) for details.


[^1]: [Plugin repository](https://github.com/TheHSI-HQ/ComitasBotJPlugins)
