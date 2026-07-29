# ComitasBotJ

> [!IMPORTANT]
> ComitasBotJ is still in early development, if you're missing any feature, please open an issue

<div style="text-align: center;">[![Build ComitasBotJ](https://github.com/TheHSI-HQ/ComitasBotJ/actions/workflows/maven-build.yml/badge.svg)](https://github.com/TheHSI-HQ/ComitasBotJ/actions/workflows/maven-build.yml)</div>

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
    java -jar comitas-core-<version>.jar
    ```

6. Configure the Bot

    Before ComitasBotJ can connect to Discord, you must provide a valid Discord bot token.
    
    Open the generated `tokens.secret` file and add your token after `bot=`:
    
    ```properties
    bot=YOUR_DISCORD_BOT_TOKEN
    ```
    
    Save the file and restart ComitasBotJ. Once the bot successfully connects to Discord, type `invite` into the bot's terminal and press **Enter**.
    
    ComitasBotJ will generate an invite link. Open the link in your browser and follow the instructions to invite the bot to your Discord server.


## Configuration

### server.properties

The `server.properties` file is automatically created on the first startup. It is used to persistently configure ComitasBotJ.

| Property            | Type     | Default       | Description                                                                                              |
|---------------------|----------|---------------|----------------------------------------------------------------------------------------------------------|
| `enabled`           | boolean  | `true`        | Enables or disables the bot                                                                              |
| `load-plugins`      | boolean  | `true`        | Enables or disables the loading of plugins                                                               |
| `allowed-plugins`   | string[] | `*`           | A comma-separated list of plugin names or UUIDs. Plugins that do not match any entry will not be loaded. |
| `bot-activity-name` | string   | `ComitasBotJ` | The activity name displayed by the bot.                                                                  |

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
│   └── ...
└── plugins/                   # Directory from which plugins are loaded
    └── ...
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

See the [API documentation](API.md) for more information.

## Contributing

Pull requests and plugin contributions[^1] are welcome.

## License

See [LICENSE](LICENSE) for details.


[^1]: [Plugin repository](https://github.com/TheHSI-HQ/ComitasBotJPlugins)