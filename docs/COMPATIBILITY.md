# API Compatibility

ComitasBotJ is still before 1.0, so the plugin API has breaking changes between releases.
Instead of keeping deprecated methods in `comitas-api`, every breaking change is recorded in a
**machine readable changelog**, which lives in the `comitas-compatibility` module and is published
with every release. Three things are built on top of it:

| Part | For | What it does |
|------|-----|--------------|
| [Compatibility layer](#for-server-owners-the-compatibility-layer) | Server owners | Runs plugins built against an older API on the current version, with a warning |
| [Upgrade recipes](#for-plugin-developers-upgrading-your-source-code) | Plugin developers | Upgrades plugin source code to the newest API with [OpenRewrite](https://docs.openrewrite.org/) |
| [API check](#for-comitas-developers-recording-breaking-changes) | Comitas developers | Fails the build, if a breaking change isn't in the changelog |

## For Server Owners: The Compatibility Layer

When a plugin's `api-target` doesn't include the running version, but the plugin was built against an
older version the changelogs cover, it is loaded in **compatibility mode** instead of being rejected:

```text
[WARN]: Plugin OldPlugin was built for API 0.0.16, it runs in compatibility mode on 0.0.17. Ask its developer for an update, the compatibility layer can't fix every change.
[WARN]:   [emulated] Guild#getDefaultChannel() returns a GuildChannel instead of a TextChannel, the default channel can be a voice or forum channel.
[WARN]:   [may misbehave] Integer command arguments deliver Long values instead of Integer values, because Discord integers are 53 bit.
```

* **emulated** changes are handled by the compatibility layer, the plugin sees the old behaviour.
* **may misbehave** changes can't be fixed automatically (e.g. changed behaviour). The plugin uses the affected API,
  it may fail or behave differently.
* Only the changes the plugin actually uses are listed. `--list-plugins` shows such plugins as `COMPAT`.

While a plugin's classes are loaded, the compatibility layer rewrites its calls to changed API methods, before the
[sandbox](../README.md#security) verifies them. Redirected calls go to *shims*, small static methods, which emulate the
old API on top of the current one, and which can only use the public API themselves. Compatibility mode doesn't
give a plugin any additional access.

Set `compatibility-layer=false` in `server.properties` to reject outdated plugins instead.
Plugins built against a version older than the first changelog (currently 0.0.16) are always rejected.

## For Plugin Developers: Upgrading Your Source Code

Every release publishes `cloud.thehsi.ComitasBotJ:comitas-compatibility` with OpenRewrite recipes, which:

* rename classes, methods, fields and enum constants, that were renamed,
* update the `comitas-api` dependency in your `pom.xml`,
* add a `/* TODO Comitas <version>: ... */` comment with migration instructions to calls, which need your attention,
  and mark changed constructors and fields with `/*~~>*/`.

**Maven** (run in your plugin project, replace `0.0.18` with the version you upgrade to):

```bash
mvn org.openrewrite.maven:rewrite-maven-plugin:run -Drewrite.recipeArtifactCoordinates=cloud.thehsi.ComitasBotJ:comitas-compatibility:0.0.18 -Drewrite.activeRecipes=cloud.thehsi.ComitasBotJ.UpgradeToLatest
```

Use the `dryRun` goal instead of `run` to get a patch (`target/rewrite/rewrite.patch`) instead of changed files.

**Gradle**:

```kotlin
plugins {
    id("org.openrewrite.rewrite") version "<latest>"
}

rewrite {
    activeRecipe("cloud.thehsi.ComitasBotJ.UpgradeToLatest")
}

dependencies {
    rewrite("cloud.thehsi.ComitasBotJ:comitas-compatibility:0.0.18")
}
```

```bash
gradle rewriteRun
```

`UpgradeToLatest` upgrades from any older version. To stop at a version, use `cloud.thehsi.ComitasBotJ.UpgradeTo_0_0_17`
(dots replaced by underscores). Run the recipe on code targeting the old version, then fix the marked places,
build, and set `api-target` in `plugin.properties` to the new version.

## For Comitas Developers: Recording Breaking Changes

### The Changelog

Each release has a changelog `comitas-compatibility/src/main/resources/comitas-compatibility/changelog/<version>.json`
with every breaking change since the previous release, listed in `index.txt` next to it:

```json
{
  "schema": 1,
  "from": "0.0.16",
  "to": "0.0.17",
  "changes": [
    {
      "id": "guild-default-channel-type",
      "kind": "METHOD_REPLACED",
      "class": "cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild",
      "name": "getDefaultChannel",
      "descriptor": "()Lcloud/thehsi/ComitasBotJ/API/Discord/Channel/TextChannel;",
      "shim": "V0_0_17#guildGetDefaultChannel",
      "description": "Guild#getDefaultChannel() returns a GuildChannel instead of a TextChannel.",
      "migration": "Check the returned GuildChannel with instanceof TextChannel before using it as a text channel."
    }
  ]
}
```

| Field | Description |
|-------|-------------|
| `id` | Unique id (across all changelogs) |
| `kind` | See below |
| `class` | The (old) class, fully qualified, nested classes with `$` (`...ConsoleCommandRegistry$ConsoleCommand`). Must be in the API package |
| `name` | The (old) member name. Omit for whole classes and constructors |
| `descriptor` | The (old) JVM descriptor, e.g. `(Ljava/lang/String;)V` for methods or `Ljava/lang/String;` for fields |
| `static` | `true` for static methods / fields |
| `newName` | For renames: the new member name, or the new fully qualified class name |
| `shim` | For `*_REPLACED`: `Class#method` in `cloud.thehsi.ComitasBotJ.Compatibility.Shims` |
| `newDescriptor` | For `ABSTRACT_METHOD_CHANGED`: the new method descriptor |
| `arguments` | For `ABSTRACT_METHOD_CHANGED`: for every old parameter, the index of the new parameter passed to it |
| `description` | What changed, shown to server owners |
| `migration` | How plugin developers update their code, used in the upgrade recipes |
| `rewrite` | Optional OpenRewrite recipes, replacing the generated ones (e.g. `ChangeMethodName` for a method, which got a replacement) |

Everything refers to the API **before** the change (the `from` version).

| Kind | Old plugins | Source upgrade |
|------|-------------|----------------|
| `CLASS_RENAMED` | Fixed automatically | Renamed |
| `METHOD_RENAMED` | Fixed automatically (calls and overriding methods) | Renamed |
| `FIELD_RENAMED` | Fixed automatically (incl. enum constants in `switch`) | Renamed (static fields) |
| `METHOD_REPLACED` | Calls go to the shim | `TODO` comment |
| `CONSTRUCTOR_REPLACED` | `new` goes to the shim | Marked |
| `FIELD_REPLACED` | Reads go to the shim, writes fail | Marked |
| `ABSTRACT_METHOD_CHANGED` | Plugin classes implementing the old signature get a bridge method with the new one | Reported by the compiler |
| `NOTE` | Warning, if the plugin uses it | `TODO` comment / marked |

`ABSTRACT_METHOD_CHANGED` example (0.0.18): old plugins implement `execute(String[] args)`, they get a generated
`execute(String[] args, String alias, ConsoleCommand command)`, which calls `execute(args)`:

```json
{
  "id": "console-command-executor-alias-and-command",
  "kind": "ABSTRACT_METHOD_CHANGED",
  "class": "cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor",
  "name": "execute",
  "descriptor": "([Ljava/lang/String;)V",
  "newDescriptor": "([Ljava/lang/String;Ljava/lang/String;Lcloud/thehsi/ComitasBotJ/API/Console/ConsoleCommandRegistry$ConsoleCommand;)V",
  "arguments": [0],
  "description": "ConsoleCommandExecutor#execute receives the used alias and the ConsoleCommand as well."
}
```

### Shims

A shim emulates one old method on top of the current API. It is a `public static` method in a class per release in
`comitas-compatibility/src/main/java/cloud/thehsi/ComitasBotJ/Compatibility/Shims/` (e.g. `V0_0_17`), and its signature is
derived from the change:

* `METHOD_REPLACED`: the receiver (unless `static`), then the original arguments, returns the original return type
* `CONSTRUCTOR_REPLACED`: the original arguments, returns the class
* `FIELD_REPLACED`: the receiver (unless `static`), returns the field type

Plugins can call shims, so they may only use the public API, the module doesn't depend on `comitas-core` for that reason.
Shims are compiled against the current API, so a later breaking change, which affects a shim, is a compile error
and gets fixed in the shim.

### Release Checklist

1. Bump `comitas.version` and set `comitas.previous.version` to the last release in the root `pom.xml`.
2. Add the changelog for the new version (with `"changes": []` if nothing broke) and list it in `index.txt`.
3. Run `mvn -pl comitas-compatibility -am verify`. `ApiCompatibilityTest` downloads the last released `comitas-api`
   from Maven Central and fails for every removed or changed class, method or field, which no changelog entry covers.
   It prints a skeleton entry for each one.

The same check is available as a command (after a build with tests, which downloads the baseline jar):

```bash
mvn -q -pl comitas-compatibility exec:java -Dexec.mainClass=cloud.thehsi.ComitasBotJ.Compatibility.Tools.ChangelogTool -Dexec.args="diff comitas-compatibility/target/baseline/comitas-api-baseline.jar comitas-api/target/classes 0.0.17 0.0.18"
```

With `-Dexec.args="markdown"` it prints all changelogs as a Markdown table, e.g. for release notes.

The API check only finds *binary* breaks. Changed behaviour behind the same signature (a method returning `null` now,
`getReactors()` returning users instead of members) has to be added by hand, as a `NOTE` or, if it can be emulated,
as a `METHOD_REPLACED` with the same descriptor.

### Limitations

* An abstract method, which plugins implement, can only change by adding or dropping parameters
  (`ABSTRACT_METHOD_CHANGED`, e.g. `ConsoleCommandExecutor#execute` in 0.0.18), not by changing parameter or return types.
  Prefer `default` methods when adding methods to interfaces plugins implement.
* Reflection and `Enum.valueOf` with old names aren't rewritten.
* Writes to replaced fields can't be redirected.
