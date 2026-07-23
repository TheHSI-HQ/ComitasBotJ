# ComitasBotJ

A Plugin Based Discord Bot. For those Server with 10 Bots all doing only one thing.

The continuation of [ComitasBot](https://git.thehsi.cloud/TheHSI/ComitasBot)

## Installation

### Option 1. Download the Precompiled JAR

Download the latest comitas-core-*.jar from the [Releases Tab](https://github.com/TheHSI-HQ/ComitasBotJ/releases)

Start it using

```bash
java -jar comitas-core-<version>.jar`
```

### Option 2. Build from Source

0. Prerequirements:

- Java 21+
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
cd ..
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