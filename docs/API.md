<!-- @formatter:off -->
# ComitasBotJ Plugin Development (API Docs)

ComitasBotJ plugins are Java projects that extend the functionality of a running ComitasBotJ instance. A plugin can respond to events, register commands, interact with the bot API, and provide additional functionality.

> [!IMPORTANT]
> ComitasBotJ requires Java 21 or newer.

> [!NOTE]
> This documentation is a work in progress.

This guide walks you through creating your first ComitasBotJ plugin using IntelliJ IDEA, then covers the rest of the API in reference form: events, messages, components, embeds, attachments, reactions & emoji, buttons, commands (static, console and dynamic), the scheduler, persistent data and the plugin manager.

## Table of Contents

* [Prerequisites](#prerequisites)
* [Setting Up Your Development Environment](#setting-up-your-development-environment)
* [Events and Listeners](#events-and-listeners)
* [The Comitas Entry Point](#the-comitas-entry-point)
* [Messages](#messages)
* [Components & Text Styling](#components--text-styling)
* [Embeds](#embeds)
* [Attachments & Uploads](#attachments--uploads)
* [Reactions & Emoji](#reactions--emoji)
* [Buttons & Action Rows](#buttons--action-rows)
* [Commands](#commands)
* [Console Commands](#console-commands)
* [Scheduler](#scheduler)
* [Persistent Data Storage](#persistent-data-storage)
* [Sandbox & Permissions](#sandbox--permissions)
* [Plugin Manager & Cross-Plugin Communication](#plugin-manager--cross-plugin-communication)

## Prerequisites

Before you begin, make sure you have:

* IntelliJ IDEA installed.
* A ComitasBotJ instance available for testing.
* The ComitasBotJ Dev Plugin for IntelliJ IDEA installed.
* A compatible Java development environment.

## Setting Up Your Development Environment

This guide uses [IntelliJ IDEA](https://www.jetbrains.com/idea/download/)[^1] as the development environment.

You will also need the [ComitasBotJ Dev Plugin for IntelliJ IDEA](https://github.com/TheHSI-HQ/ComitasBotJ-IdeaPlugin/releases/latest/).

### Installing the Development Plugin

1. Download the [latest release of the ComitasBotJ Dev Plugin](https://github.com/TheHSI-HQ/ComitasBotJ-IdeaPlugin/releases/latest/) for IntelliJ IDEA.

2. Open IntelliJ IDEA and navigate to **Plugins**.

3. Open the **hamburger menu** in the top-right corner and select **Install Plugin from Disk...**.

4. Select the downloaded `ComitasBotJ-IdeaPlugin.jar` file.

### Creating a ComitasBotJ Plugin Project

1. Create a new project in IntelliJ IDEA.

2. Select **ComitasBotJ Plugin** as the project type.

3. Enter the following project details:

    * **Name**
    * **Group ID**
    * **Version**
    * **API Version**

   For the **API Version**, use the [latest available ComitasBotJ release](https://github.com/TheHSI-HQ/ComitasBotJ/releases/latest).

4. Once the project has been created, open the main plugin class.

The project should contain the plugin lifecycle methods:

* `onLoad()` — *optional*, called as soon as the plugin is loaded, before the bot is connected to Discord.
* `onEnable()` — called once the bot is connected to Discord. The whole API is available from here on, so this is where commands and listeners are registered.
* `onDisable()` — called when the plugin is disabled (only if it was enabled before).

Override `onLoad()` only for setup that has to happen before the bot connects, e.g. registering console commands or reading config files:

```java
@Override
public void onLoad() {
    // Discord isn't connected yet, Comitas.getBot() and command registration aren't available
}
```

### Hello World

To add a log message when your plugin is enabled:

1. Open the `onEnable()` function.

2. Add the following line:

   ```java
   getLogger().info("Hello World!");
   ```

3. Your `onEnable()` function should look similar to this:

   ```java
   @Override
   public void onEnable() {
       getLogger().info("Hello World!");
   }
   ```

4. Click the green **Run** button in the top-right corner of IntelliJ IDEA to compile the plugin.

5. Once the build has completed, locate the generated plugin file in:

   ```text
   <project-root-directory>/target
   ```

6. Copy the generated plugin file into the `plugins` directory of your ComitasBotJ instance.

7. Restart the Bot.

8. Once the Bot has started, you should see the following message in the log:

   ```text
   Hello World!
   ```

## Events and Listeners

Now that you know how the plugins work and how to install them, let's get the Bot to respond to all messages containing the word `Banana`

1. Create a new class named `Listeners`.

2. Implement the `Listener` interface:

   Add `implements Listener` after the class name.

   If `Listener` is marked as an error, press **Alt+Enter** and click on **Import Class**

   ```java
   class Listeners implements Listener {
   ```

3. Create a new function, let's name it `onMessageReceived`, annotate it with `@EventHandler` and give an argument of type `MessageReceivedEvent`

   ```java
   @EventHandler
   private void onMessageReceived(MessageReceivedEvent event) {
   }
   ```

4. Add a check, to prevent the Bot from responding to its own messages

   This prevents the bot from responding to its own messages. Without this check, the bot could potentially trigger its own listener repeatedly.
   ```java
   if (Comitas.getBot().isMeOrNull(event.getAuthor())) return;
   ```

5. Check if the message contains the word `Banana` and respond with `Who said banana?`

   `toLowerCase()` makes the check case-insensitive, so `banana`, `Banana`, and `BANANA` all match.
   ```java
   if (event.getRawContent().toLowerCase().contains("banana")) {
        event.reply(
                Component.text("Who said banana?")
        );
    }
   ```

6. Register the Listener, open the Main.java file and add this to the onEnable function
   ```java
   Comitas.getPluginManager().registerEvents(this, new Listeners());
   ```

   The final `Listeners` class should look something like this:
   ```java
   import cloud.thehsi.ComitasBotJ.API.Event.Listener;
   import cloud.thehsi.ComitasBotJ.API.Event.EventHandler;
   import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageReceivedEvent;
   import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;


   public class Listeners implements Listener {
       @EventHandler
       private void onMessageReceived(MessageReceivedEvent event) {
           if (Comitas.getBot().isMeOrNull(event.getAuthor())) {
                return;
           }

           if (event.getRawContent().toLowerCase().contains("banana")) {
               event.reply(
                       Component.text("Who said banana?")
               );
           }
       }
   }
   ```

   And a `Main.java` the looks something like:
   ```java
   import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
   import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

   public class Main extends Plugin {
       @Override
       public void onEnable() {
           Comitas.getPluginManager().registerEvents(this, new Listeners());
       }

       @Override
       public void onDisable() {
           // Your code here
       }
   }
   ```

7. Recompile the plugin, move it into the `plugins` directory and restart the Bot.

8. Once the Bot has started, send a new message in any channel in the discord server with `banana` in it, you should see a response like this:

   ```text
   Who said banana?
   ```

### Available Events

A `Listener` class can hold as many `@EventHandler` methods as you want, each one listening for a single event type. Every event lives in `cloud.thehsi.ComitasBotJ.API.Event.Events`:

| Event | Fired when... |
|---|---|
| `BotUpdatePresenceEvent` | The bot's own presence (status/activity) is updated. |
| `MessageReceivedEvent` | Any message is sent in a channel the bot can see. |
| `MessageEditedEvent` | A message is edited. |
| `MessageDeletedEvent` | A message is deleted. |
| `ReactionUpdatedEvent` | A reaction on a message is added, removed, or cleared. |
| `UserJoinGuildEvent` | A member joins a guild. |
| `UserLeaveGuildEvent` | A member leaves a guild. |
| `UserKickedEvent` | A member is kicked. |
| `UserBannedEvent` | A member is banned. |
| `UserUnbannedEvent` | A user is unbanned. |
| `UserRoleAddedEvent` | A role is added to a member. |
| `UserRoleRemovedEvent` | A role is removed from a member. |
| `UserChangeUserNameEvent` | A user changes their username. |
| `UserChangeGlobalDisplayNameEvent` | A user changes their global display name. |
| `UserChangeGuildDisplayNameEvent` | A member changes their guild-specific display name (nickname). |
| `PackageSentEvent<T>` | A plugin broadcasts a `Package` (see [Plugin Manager & Cross-Plugin Communication](#plugin-manager--cross-plugin-communication)). |

Every event implements the marker interface `Event`, which exposes `getOrigin()` — either `EventOrigin.EXTERNAL` (caused by Discord/Comitas) or `EventOrigin.UNDO` (caused by undoing another event, see below).

### Event Priority

`@EventHandler` accepts an optional `priority`, controlling the order in which listeners across all plugins are called for the same event:

```java
@EventHandler(priority = EventPriority.HIGH)
public void onMessage(MessageReceivedEvent event) {
    // ...
}
```

Priorities run in this order: `LOWEST` → `LOW` → `NORMAL` (default) → `HIGH` → `HIGHEST` → `MONITOR`. Use `LOWEST` if you want other plugins to be able to override your changes, and `MONITOR` for pure observation — handlers at `MONITOR` priority should not modify the event.

### Undoable Events

Some events (member bans, role changes, reaction updates, etc.) implement `UndoableEvent`, letting you veto the action that already happened by instructing Comitas to immediately reverse it:

```java
@EventHandler
public void onRoleAdded(UserRoleAddedEvent event) {
    if (event.getRole().getName().equals("Muted")) {
        event.setUndo(true); // or event.undo();
    }
}
```

* `willUndo()` — whether the event is currently marked to be undone.
* `setUndo(boolean)` — mark/unmark the event for undoing.
* `undo()` — mark the event for undoing (shorthand for `setUndo(true)`).

When an undo is executed, Comitas typically fires a corresponding follow-up event with `getOrigin() == EventOrigin.UNDO`, so make sure your handlers don't get stuck in a loop reacting to their own undos.

`MessageReceivedEvent` and `MessageEditedEvent` offer the same pattern for deletion instead of undoing: `markedForDeletion()`, `setDelete(boolean)` and `deleteMessage()`.

```java
@EventHandler
public void onMessage(MessageReceivedEvent event) {
    if (event.getRawContent().contains("secret-link.invalid")) {
        event.deleteMessage();
    }
}
```

## The Comitas Entry Point

`cloud.thehsi.ComitasBotJ.API.Bot.Comitas` is the static entry point into everything the API offers. All of its accessors are static and usable from anywhere in your plugin once the bot has started (i.e. from `onEnable()` on, not in `onLoad()`):

```java
Comitas.getBot();                     // The running Bot
Comitas.getPluginManager();           // PluginManager — events, metadata, packages, persistent storage
Comitas.getCommandRegistry();         // CommandRegistry — register slash commands
Comitas.getConsoleCommandRegistry();  // ConsoleCommandRegistry — register console commands
Comitas.getScheduler();               // Scheduler — async & repeating tasks
Comitas.getServerVersion();           // The running Comitas server version
Comitas.shutdown();                   // Shuts down the bot
```

`Bot` (via `Comitas.getBot()`) extends `User` and adds bot-specific helpers such as `isMe(User)` / `isMeOrNull(User)` — always use these instead of comparing IDs manually when guarding against your own messages in a listener.

## Messages

### Sending

Any `MessageChannel` (e.g. a `TextChannel`, `ThreadChannel`, or a DM channel) can send messages:

```java
MyMessage sent = channel.sendMessage(Component.text("Hello there!"));
```

For anything beyond plain text/embeds use `MessageData`, the shared builder behind every message-sending and message-editing method in the API:

```java
MessageData data = new MessageData(Component.text("Here's your file:"))
        .addAttachment(AttachmentUpload.from(Path.of("report.pdf")))
        .addEmbed(myEmbed)
        .addActionRowComponent(myButton);

channel.sendMessage(data);
```

`MessageData` has convenience constructors for the common cases: `new MessageData(Component)`, `new MessageData(Embed...)`, `new MessageData(AttachmentUpload...)`, or combinations thereof.

### Replying

Inside events like `MessageReceivedEvent`/`MessageEditedEvent`, or on any `Message`, you can reply directly in the same channel without looking the channel up yourself:

```java
@EventHandler
public void onMessage(MessageReceivedEvent event) {
    event.reply(Component.text("Got it!"));
    // or with full MessageData:
    event.reply(new MessageData(Component.text("Got it, with an embed!"), myEmbed));
}
```

Command executions and button presses use `RepliableInteraction` instead, which additionally supports ephemeral replies (visible only to the invoking user) and throws `InteractionAlreadyUsedException` if you try to reply to the same interaction twice:

```java
commandRanContext.reply(Component.text("Visible to everyone"));
commandRanContext.replyEphemeral(Component.text("Only you can see this"));
```

### Editing & Deleting

Messages the bot itself sent are returned as `MyMessage`, a `Message` subtype that can be mutated after the fact:

```java
MyMessage sent = channel.sendMessage(Component.text("Loading..."));

// later...
sent.setContent(Component.text("Done!"));
// or replace embeds/attachments/components entirely:
sent.setMessageData(new MessageData(Component.text("Done!"), resultEmbed));
```

Any `Message` — yours or someone else's — can be deleted and queried:

```java
message.delete();
message.isDeleted();
```

To intercept edits made by users before you act on them, listen for `MessageEditedEvent`, which exposes both the new `getRawContent()`/`getContent()` and the full `getMessage()`, plus the same `markedForDeletion()`/`deleteMessage()` moderation hooks as `MessageReceivedEvent`.

### Forwarding, Replies & History

```java
message.isReply();            // Is this message a reply?
message.getRepliedMessage();  // The message it replies to, if any
message.isForwarded();        // Is this message a forward?
message.forward(otherChannel); // Forward this message into another channel

MessageHistory history = channel.getMessageHistory();
List<Message> lastTen = history.retrieve(10);

Message byId = channel.getMessageById(123456789L);
```

## Components & Text Styling

Discord messages are built as a `Component` tree instead of raw markdown strings. `Component.text(...)` auto-escapes markdown special characters (`*_~|` etc.) so user-provided content can't break your formatting; use `Component.raw(...)` when you want markdown to pass through untouched (e.g. content you've built yourself).

```java
Component message = Component.text("Welcome, ")
        .append(member.mention())
        .append(Component.text("!", Style.BOLD))
        .append(Component.newLine())
        .append(Component.text("Rules:", Style.SMALL_HEADER))
        .append(Component.text("Be nice.", Style.BULLET_POINTS))
        .append(Component.link("https://example.com", "Read more"))
        .append(Component.timestamp(Instant.now(), TimeStampFormat.RELATIVE));
```

Useful factory methods on `Component`:

* `Component.text(String, Style...)` — escaped text, optionally styled.
* `Component.raw(String)` — unescaped text.
* `Component.link(String url)` / `Component.link(String url, String label)` — a hyperlink.
* `Component.timestamp(Instant, TimeStampFormat)` — a Discord-rendered, localized timestamp.
* `Component.newLine()` — a line break.
* `Component.of(Object)` — calls `toString()` and wraps it as a raw component.
* `Component.empty()` — an empty component (default content of `MessageData`).

`Style` flags can be combined with `.add(Style)`/`.remove(Style)` or passed as varargs to `Component.text(...)`: `BOLD`, `ITALIC`, `UNDERLINE`, `STRIKETHROUGH`, `BIG_HEADER`, `SMALL_HEADER`, `SMALLER_HEADER`, `SUBTEXT`, `QUOTE`, `BULLET_POINTS`, `CODE`, `CODE_BLOCK`, `SPOILER`.

## Embeds

Embeds are built with `EmbedBuilder` and attached via `MessageData`:

```java
Embed embed = new EmbedBuilder()
        .setTitle(new EmbedTitle("Server Stats", "https://example.com"))
        .setDescription(Component.text("A quick overview of this server."))
        .setAuthor(new EmbedAuthor(bot.getDisplayName(), "https://example.com", "https://example.com/logo.png"))
        .setColor(new Color(151, 45, 231))
        .addField(new EmbedField(Component.text("Members"), Component.of(guild.getMemberCount())))
        .addField(new EmbedField(Component.text("Created by"), Component.link("https://github.com/TheHSI-HQ/"), false))
        .setFooter(new EmbedFooter("Powered by ComitasBotJ"))
        .setThumbnail("https://example.com/thumb.png")
        .setImage("https://example.com/banner.png")
        .setTimestamp(Instant.now())
        .build();

channel.sendMessage(new MessageData(Component.empty(), embed));
// or, shorter:
channel.sendMessage(embed.asMessageData());
```

Setter overloads accept either a plain `String` (`setTitle("...")`, `setAuthor("...")`, `setFooter("...")`) or the richer `EmbedTitle`/`EmbedAuthor`/`EmbedFooter` objects when you also need a URL/image. `EmbedField`'s third constructor argument controls whether the field renders inline (defaults to `true`).

## Attachments & Uploads

To send a new file, build an `AttachmentUpload` from a local file path or raw bytes, then add it to `MessageData`:

```java
AttachmentUpload fromDisk = AttachmentUpload.from(Path.of("logs/latest.log"));
AttachmentUpload fromMemory = AttachmentUpload.from("hello.txt", "Hello, world!".getBytes());

fromDisk.setSpoiler(true).setDescription("Latest server log");

channel.sendMessage(new MessageData(Component.text("Here's the log:"))
        .addAttachment(fromDisk)
        .addAttachment(fromMemory));
```

### Voice Messages

The easiest way to send a voice message is `AttachmentUpload.makeVoiceMessage(...)`, which takes an Ogg Opus file (as a `Path` or its bytes), reads its duration and generates a waveform for it:

```java
channel.sendMessage(AttachmentUpload.makeVoiceMessage(Path.of("greeting.ogg")).asMessageData());
channel.sendMessage(AttachmentUpload.makeVoiceMessage(oggBytes).asMessageData()); // named voice-message.ogg
```

The generated waveform is approximated from the bitrate of the audio rather than decoded, which is close enough for Discord to draw speech and silence. A file that isn't Ogg Opus throws an `IllegalArgumentException`; convert other formats first, e.g. `ffmpeg -i input.mp3 -c:a libopus output.ogg`.

For full control, any `AttachmentUpload` can be turned into a voice message with `asVoiceMessage(...)`. Discord expects the audio to be Ogg Opus (`audio/ogg`). The waveform is a low-resolution amplitude sampling (1-256 bytes, each 0-255) that Discord draws as the bars of the voice message; use the `Duration`-only overload if you don't have one and a flat waveform is used instead:

```java
AttachmentUpload voice = AttachmentUpload.from(Path.of("greeting.ogg"))
        .asVoiceMessage("audio/ogg", waveform, Duration.ofMillis(4200));

// or, without a waveform:
AttachmentUpload.from(Path.of("greeting.ogg")).asVoiceMessage(Duration.ofMillis(4200));

channel.sendMessage(voice.asMessageData());
```

A voice message has to be sent on its own: the `MessageData` may not contain any content, embeds, action row components or other attachments, and voice messages cannot be added to an existing message by editing it. Violating either throws an `IllegalArgumentException`.

Received voice messages can be detected with `Message#isVoiceMessage()`. The audio is the message's only attachment, which exposes the voice message data:

```java
if (message.isVoiceMessage()) {
    MessageAttachment voice = message.getAttachments().getFirst();
    Duration duration = voice.getDuration(); // null on non voice message attachments
    byte[] waveform = voice.getWaveform();   // null on non voice message attachments
    String contentType = voice.getContentType(); // e.g. "audio/ogg"
    voice.getContent().thenAccept(bytes -> { /* the Ogg Opus audio */ });
}
```

Attachments already on a message (yours or someone else's) are exposed as `MessageAttachment`s via `message.getAttachments()`. Both `Attachment` and `MessageAttachment` let you read the file back:

```java
for (MessageAttachment attachment : message.getAttachments()) {
    String fileName = attachment.getFileName();
    attachment.getContent().thenAccept(bytes -> {
        // process the downloaded file, off the calling thread's critical path
    });
}
```

`getHash()` and `getContent()` return `CompletableFuture`s since fetching attachment bytes requires a network round-trip. Any `Attachment`/`AttachmentUpload` can also be converted straight into a sendable `MessageData` via `asMessageData()` — handy for quickly re-sending/forwarding a file.

## Reactions & Emoji

### Emoji

```java
Emoji thumbsUp = Emojis.THUMBSUP();           // One of hundreds of built-in Unicode presets
Emoji custom = Emoji.fromId(123456789L);      // A custom guild emoji by id

Component.text("Nice! ").append(thumbsUp);    // Emoji can be appended into any Component
```

`Emojis` (in `cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emojis`) ships a static method per standard Unicode emoji (`WAVE()`, `THUMBSUP()`, `HUNDRED()`, `TRIANGULAR_FLAG_ON_POST()`, ...) so you rarely need to fall back to `Emoji.fromUnicode(String)`/hardcoded Unicode escapes yourself — reach for `Emoji.fromUnicode(...)`/`Emoji.fromId(...)` only when there's no matching `Emojis` preset (e.g. a custom guild emoji).

### Reacting

```java
message.react(Emojis.THUMBSUP());
message.unreact(Emojis.THUMBSUP());

for (Reaction reaction : message.getReactions()) {
    reaction.getEmoji();
    reaction.getCount();
    reaction.haveIReacted();
    reaction.getReactors();         // Everyone who reacted, as Users
    reaction.getReactorMemberss();  // Same, as Members (null in DMs)
}
```

A `Reaction` can also act on itself: `react()`/`unreact()` toggle the bot's own reaction, `clear()` removes the reaction entirely, and `removeReaction(Member)` strips a specific member's reaction (useful for moderation).

### Listening for Reaction Changes

```java
@EventHandler
public void onReaction(ReactionUpdatedEvent event) {
    Reaction reaction = event.reaction();
    ReactionAction action = event.reactionAction();

    Emoji flag = Emojis.TRIANGULAR_FLAG_ON_POST();
    if (action.isIncrease() && reaction.getEmoji().getName().equals(flag.getName())) {
        event.message().getChannel().sendMessage(
                Component.text("Message flagged by ").append(event.member().mention())
        );
    }

    if (action.isRemoved()) {
        // the reaction group disappeared entirely (count hit zero)
    }
}
```

`ReactionUpdatedEvent` is also an `UndoableEvent` — call `event.setUndo(true)` to automatically remove the reaction that just caused the event (e.g. to enforce a single-reaction-per-poll rule).

## Buttons & Action Rows

Buttons are created via static factories on `Button` and attached to a message through `MessageData#addActionRowComponent`:

```java
Button confirm = Button.success("confirm", "Confirm", ctx -> {
    ctx.reply(Component.text("Confirmed by ").append(ctx.getUser().mention()), true);
    ctx.getMessage().setContent(Component.text("✅ Confirmed"));
});

Button cancel = Button.danger("cancel", "Cancel", ctx -> ctx.replyEphemeral(Component.text("Cancelled.")));
Button docs = Button.link("https://example.com/docs", "Docs");

channel.sendMessage(new MessageData(Component.text("Proceed?"))
        .addActionRowComponent(confirm)
        .addActionRowComponent(cancel)
        .addActionRowComponent(docs));
```

Available styles: `primary`, `secondary`, `success`, `danger` (each take either a `String` label or an `Emoji`, plus a callback), and `link` (no callback — it just opens a URL). Button ids must be ≤ `Button.ID_MAX_LENGTH` (100) characters and labels ≤ `Button.LABEL_MAX_LENGTH` (80).

The callback receives a `ButtonPressedContext`, a `RepliableInteraction` that additionally exposes `getButtonId()` and `getMessage()` (the `MyMessage` the button is attached to, so you can edit it in place as shown above).

## Commands

ComitasBotJ supports three ways to register commands: **static** slash commands (compile-time, type-safe, preferred), **dynamic** slash commands (built at runtime), and **console** commands (typed into the bot's console, not Discord).

### Static (Annotated) Commands

Implement `CommandSupplier` and annotate a method with `@Command`; each `@CommandOption`-annotated parameter becomes a command argument, type-mapped automatically from the Java parameter type:

```java
public class ExampleCommand implements CommandSupplier {
    @Command(
            name = "example",
            description = "An example command",
            commandContextType = { CommandContextType.GUILD, CommandContextType.BOT_DM },
            requiredPermissions = { Permission.ADMINISTRATOR }
    )
    public void exampleCommand(
            @NotNull CommandRanContext ctx,
            @CommandOption(name = "target", description = "The target user") @NotNull User target,
            @CommandOption(name = "message", description = "An optional message", required = false) @Nullable String message
    ) {
        ctx.replyEphemeral(Component.text("Ok, telling ").append(Component.text(target.getDisplayName())));

        ctx.getChannel().sendMessage(
                message != null
                        ? target.mention().append(Component.text(", you've got a message: ")).append(Component.text(message, Style.CODE))
                        : Component.text("Hi ").append(target.mention())
        );
    }
}
```

Register it once in `onEnable()` (commands need the bot connection to be registered with Discord, which is guaranteed there):

```java
@Override
public void onEnable() {
    Comitas.getCommandRegistry().register(new ExampleCommand());
}
```

Commands that are no longer registered by any plugin are removed from Discord automatically once all plugins are enabled.

Notes on `@Command`:

* `commandType` controls installability — `GUILD_INSTALL` (default), `USER_INSTALL`, or both.
* `commandContextType` controls where it can be run — `GUILD` (default), `BOT_DM`, `PRIVATE_CHANNEL`.
* `requiredPermissions` restricts who can see/run the command; leave it empty to allow everyone.
* Optional arguments (`required = false` on `@CommandOption`) must use a `@Nullable`/boxed parameter type and will be `null` if omitted.

### Dynamic Commands

Use `DynamicCommandBuilder` when the set of commands (or their arguments) isn't known until runtime — e.g. generated from a config file:

```java
DynamicCommandBuilder sayCommand = new DynamicCommandBuilder(
        "say", "Make the bot say something",
        (ctx, args) -> {
            CommandArgument<String> text = args.get("text", CommandArgumentType.STRING);
            ctx.getChannel().sendMessage(Component.raw(String.valueOf(text.getValue())));
            ctx.acknowledge();
        }
).addArgument("text", "What to say", CommandArgumentType.STRING)
 .setRequiredPermissions(Permission.MANAGE_MESSAGES);

Comitas.getCommandRegistry().register(sayCommand);
```

`CommandArgumentType` ships typed constants: `STRING`, `NUMBER` (`Long`), `DOUBLE`, `BOOLEAN`, `USER`, `CHANNEL`, `ROLE`, `ATTACHMENT`. Use `CommandArgumentProvider#has(identifier)` to check presence before reading an optional argument.

### Permissions

Both command flavors take `Permission[]` to gate access — e.g. `Permission.ADMINISTRATOR`, `Permission.MANAGE_MESSAGES`, `Permission.BAN_MEMBERS`, `Permission.KICK_MEMBERS`. Leave the array empty to allow anyone to use the command.

## Console Commands

Console commands run only from the server operator's console/terminal, not from Discord — useful for admin tooling that shouldn't be exposed to guild members:

```java
public class ReloadConsoleCommand extends ConsoleCommandExecutor {
    @Override
    public void execute(@NotNull String[] args) {
        getConsoleLogger().info("Reloading plugins...");
        Comitas.getPluginManager().reloadSoft();
    }
}
```

```java
Comitas.getConsoleCommandRegistry().register(
        new ReloadConsoleCommand(),
        "Reloads all plugins",
        "reload", "rl" // aliases
);
```

`ConsoleCommandRegistry#registeredCommands()` lists every console command currently registered, across all plugins, as `ConsoleCommand` records. For colored console output outside of a command (e.g. in `getLogger()` calls that bypass SLF4J formatting), `ConsoleColor` provides ANSI constants (`ConsoleColor.RED`, `ConsoleColor.BOLD`, `ConsoleColor.BG_BLUE`, ...) plus `ConsoleColor.of(Color)` for arbitrary RGB colors.

## Scheduler

Use `Comitas.getScheduler()` instead of spawning raw threads, so tasks are tracked and can be cancelled when your plugin is disabled/reloaded:

```java
Scheduler scheduler = Comitas.getScheduler();

// Fire-and-forget, off the calling thread
scheduler.runTaskAsynchronously(this, () -> getLogger().info("Running in the background"));

// Once, after a delay
Task reminder = scheduler.runTaskLaterAsynchronously(this, () -> {
    channel.sendMessage(Component.text("5 minutes are up!"));
}, 5, TimeUnit.MINUTES);

// Repeating, with an initial delay then a fixed interval
Task heartbeat = scheduler.runTaskTimerAsynchronously(this, () -> {
    getLogger().info("Still alive");
}, 0, 1, TimeUnit.MINUTES);

// Later...
heartbeat.cancel();
heartbeat.isCancelled();
```

`this` in these calls refers to your `Plugin` instance (e.g. your `Main` class extending `Plugin`) — the scheduler uses it to track which plugin owns each `Task`, via `Task#getOwner()`.

## Persistent Data Storage

`PersistentDataStorage` persists simple key/value data across bot restarts, scoped per-plugin. Access it either statically from within a `Plugin` subclass, or via the `PluginManager`:

```java
PersistentDataStorage storage = getPersistentDataStorage(); // from within your Plugin class
// or: Comitas.getPluginManager().getPersistentDataStorage();

storage.set("welcome-message", PersistentDataTypes.STRING, "Hello!");

String message = storage.get("welcome-message", PersistentDataTypes.STRING, "Hi there!"); // with fallback

if (storage.has("welcome-message", PersistentDataTypes.STRING)) {
    storage.remove("welcome-message");
}
```

`PersistentDataTypes` provides: `STRING`, `INTEGER`, `LONG`, `DOUBLE`, `BYTE`, `SHORT`, `BOOLEAN`, and array variants of each (`STRING_ARRAY`, `INTEGER_ARRAY`, ...). `PersistentDataTypes.get(String name)` resolves a type by its registered name if you need to persist the type alongside the key dynamically.

Custom `PersistentDataType`s must serialize to plain data: strings, numbers, booleans, arrays, lists and maps.
Anything else is rejected with an `IllegalArgumentException`.

## Sandbox & Permissions

Plugins run in a sandbox. Each class is verified before it is loaded, so only the Comitas API, your own classes and a
safe subset of the JDK can be used. Forbidden calls (like `System.exit`, `Runtime.exec`, `java.io.File`, reflection on
classes that aren't yours, `MethodHandles`, ...) throw a `SecurityException` when they are reached, so unused code paths
of bundled libraries don't stop your plugin from loading. Bundling JDA is not allowed.

Some capabilities have to be requested in `plugin.properties` and granted by the server owner, who is asked on install:

```properties
permissions=network,file_storage
permission.network.reason=Fetches forecasts from api.weather.example
# Without it, the plugin isn't loaded
permission.network.required=true
permission.file_storage.reason=Caches downloaded radar images
```

| Permission     | `PluginPermission` | Allows                                                                              |
|----------------|--------------------|-------------------------------------------------------------------------------------|
| `network`      | `NETWORK`          | `java.net.http.HttpClient`, sockets, `java.net.URL` (only `http` and `https`), JDBC |
| `file_storage` | `FILE_STORAGE`     | Writing to your own folder, see below                                               |

Mark a permission as `required`, if your plugin can't work without it, it is then only loaded if the permission was
granted. Check optional ones with `hasPermission(PluginPermission.FILE_STORAGE)` and degrade gracefully, the owner may
deny them.

### Files

Plugins can't access the host's file system, each plugin has its own, isolated one. Its root `/` is the folder
`plugins/<Plugin Name>/`, which is always readable, but only writable with `file_storage`.
`Path.of`, `Paths.get` and `FileSystems.getDefault()` resolve in it, as does `getDataFolder()`:

```java
Path cache = Path.of("/cache/radar.png"); // same as getDataFolder().resolve("cache/radar.png")
Files.createDirectories(cache.getParent());
Files.write(cache, bytes);
```

`java.io.File`, `FileInputStream` & co., `Path.of(URI)` and `Path#toFile()` are not available, use `java.nio.file.Files`.
Paths can't leave the file system, `..` stops at its root.

Server owners can make additional directories available as mount points (`/mnt/<name>`). List them with `getMounts()`,
get one with `getMount("name")` (`null` if it doesn't exist) or ask for one while running:

```java
requestMount("music", "Plays your music collection", true).thenAccept(music -> {
    if (music == null) return; // denied, or no console to ask
    try (var files = Files.list(music)) { /* ... */ }
});
```

The owner is asked `[Y] Yes / [N] No / [I] Never` and enters the directory. A granted mount point is saved and is
available right away and after restarts. After `Never`, requests for that name complete with `null` without asking.
Mount points may be read-only, writing to them throws an `AccessDeniedException`.

Resources from your own jar can be read with `getClass().getResourceAsStream("/config.yml")`.

## Plugin Manager & Cross-Plugin Communication

`PluginManager` (via `Comitas.getPluginManager()`) is the hub for plugin introspection, event registration and inter-plugin messaging:

```java
PluginManager pm = Comitas.getPluginManager();

pm.countPlugins();
pm.getAllPluginMetadata();          // name, version, jarName, targetAPI, uuid, consoleCommandPrefix per plugin
pm.lookupPlugin(somePluginInstance);
pm.reloadSoft();                    // reload plugins, onLoad() and onEnable() run right after each other
pm.reloadHard();                    // reload plugins, config and the bot connection itself, onEnable() runs once reconnected
pm.registerEvents(this, listener);  // same call used for your Main class's own listeners
```

### Sending Packages

Plugins can talk to each other without a hard compile-time dependency by broadcasting a `Package` — any class implementing the single-method `Package` interface — and having other plugins listen for it with `PackageSentEvent<T>`:

```java
public record EconomyBalanceRequest(long userId) implements Package {
    @Override
    public @NotNull String getIdentifier() {
        return "economy:balance-request";
    }
}
```

```java
// Plugin A: broadcasts the package
Comitas.getPluginManager().sendPackage(new EconomyBalanceRequest(user.getId()));
```

```java
// Plugin B: listens for it
@EventHandler
public void onBalanceRequest(PackageSentEvent<EconomyBalanceRequest> event) {
    EconomyBalanceRequest request = event.getPackage();
    getLogger().info("Balance requested for user {}", request.userId());
}
```

Packages are mutable objects shared by reference across every listening plugin, so a plugin further down the call chain can read (and, if the package type exposes setters, modify) the same instance other plugins received — keep that in mind when designing a package's fields for multi-plugin integrations.

[^1]: We are not affiliated with JetBrains. We use IntelliJ IDEA because, in our opinion, it provides the easiest development experience for this project.
<!-- @formatter:on -->
