# ComitasBotJ

> [!IMPORTANT]
> ComitasBotJ is still in early development, if you're missing any feature, please open an issue

A modular Discord bot framework for replacing multiple single-purpose bots with one extensible instance.

The continuation of [ComitasBot](https://git.thehsi.cloud/TheHSI/ComitasBot)

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

Download the latest comitas-core-*.jar from the [Releases Tab](https://github.com/TheHSI-HQ/ComitasBotJ/releases)

Start it using

```bash
java -jar comitas-core-<version>.jar
```

### Option 2. Build from Source

0. Prerequirements:

- Java 21 or newer
- Maven
- Git

1. Clone the Repo

```bash
git clone https://github.com/TheHSI-HQ/ComitasBotJ
cd ComitasBotJ
```

2. Install the API

```bash
mvn -pl comitas-api -am clean install
```

3. Build ComitasBotJ (comitas-core)

```bash
mvn -pl comitas-core -am clean install
cd target
```

4. Start the Server

```bash
java -jar comitas-core-<version>.jar`
```

## Configuration

### server.properties

The server.properties file, is a file that gets automatically created on first startup. It is used to configure the server in a persistent way.

| Property          | Type    | Default     | Description                                                                                               |
|-------------------|---------|-------------|-----------------------------------------------------------------------------------------------------------|
| enabled           | boolean | true        | Enables or disables the bot                                                                               |
| load-plugins      | boolean | true        | Enables or Disables the loading of plugins                                                                |
| allowed-plugins   | string  | *           | A comma separated list of plugin names or uuids,<br/>plugins not matching any of them, will not be loaded |
| bot-activity-name | string  | ComitasBotJ | The name of the activity, the bot will be shown as actively doing                                         |

## Project Structure
```ansii
ComitasBotJ
├── comitas-api # Public API for plugin developers
├── comitas-core # Main bot runtime
└── example-plugin # A example plugin
```

## Plugin Development

ComitasBotJ provides an API for creating custom plugins.

Plugins can:
- Register commands
- Listen to Discord events
- Store persistent data
- Extend bot functionality

See the [API documentation](api.md) for more information.

## Contributing

Pull requests and plugin contributions[^1] are welcome.

## License

See `LICENSE` for details.


[^1]: [Plugin repository](https://github.com/TheHSI-HQ/ComitasBotJPlugins)