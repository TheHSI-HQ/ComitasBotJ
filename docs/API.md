# ComitasBotJ Plugin Development (API Docs)

ComitasBotJ plugins are Java projects that extend the functionality of a running ComitasBotJ instance. A plugin can respond to events, register commands, interact with the bot API, and provide additional functionality.

> [!IMPORTANT]
> ComitasBotJ requires Java 21 or newer.

> [!NOTE]
> This documentation is a work in progress.

ComitasBotJ plugins are Java projects that extend the functionality of a running ComitasBotJ instance. Plugins can respond to events, register commands, interact with the bot API, and add new functionality.

This guide walks you through creating your first ComitasBotJ plugin using IntelliJ IDEA.

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

* `onEnable()` — called when the plugin is enabled.
* `onDisable()` — called when the plugin is disabled.

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

### Events and Listeners

Now that you know how the plugins work and how to install them, let's get the Bot to respond to all messages containing the word `Banana`

1. Create a new class named `Listeners`.

2. Implement the `Listener` interface:

   Add `implements Listener` after the class name.

   If `Listener` is marked as an error, press **Alt+Enter** and click on **Import Class**

   ```java
   class Listeners implements Listener {
   ```

3. Create a new function, let's name it `onMessageSent`, annotate it with `@EventHandler` and give an argument of type `MessageSentEvent`

   ```java
   @EventHandler
   private void onMessageSent(MessageSentEvent event) {
   }
   ```
   
4. Add a check, to prevent the Bot from responding to its own messages

   This prevents the bot from responding to its own messages. Without this check, the bot could potentially trigger its own listener repeatedly.
   ```java
   if (Comitas.getBot().isMe(event.getAuthor())) return;
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
       private void onMessageSent(MessageSentEvent event) {
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

[^1]: We are not affiliated with JetBrains. We use IntelliJ IDEA because, in our opinion, it provides the easiest development experience for this project.
