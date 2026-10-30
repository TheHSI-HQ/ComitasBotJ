package cloud.thehsi.ComitasBotJ.Discord;

import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InstanceLock {
    private @NotNull
    static final String PREFIX = "cbjlock_";
    private @NotNull
    static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(30);
    private @NotNull
    static final Duration STALE_AFTER = Duration.ofSeconds(90);
    private @NotNull
    static final Path CACHE_DIRECTORY = Path.of(".cache");
    private @NotNull
    static final Path TOKEN_FILE = CACHE_DIRECTORY.resolve("concurrency_token");
    private @NotNull
    static final Path LOCK_FILE = CACHE_DIRECTORY.resolve("instance.lock");
    private static final byte @NotNull [] ICON = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="
    );
    private @NotNull
    static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".InstanceLock");

    private @NotNull
    final String token = loadOrCreateToken();
    private @NotNull
    final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ComitasInstanceLock");
        t.setDaemon(true);
        return t;
    });
    private volatile boolean acquired = false;
    private volatile @Nullable String emojiId;

    private static boolean appearsOnline(@NotNull JDA jda) {
        if (jda.getGuilds().isEmpty()) {
            DebugLogging.basic("Bot isn't in any guild, skipping presence check");
            return false;
        }

        return jda.getGuilds().stream()
                .map(Guild::getSelfMember)
                .map(Member::getOnlineStatus)
                .anyMatch(status -> status == OnlineStatus.ONLINE || status == OnlineStatus.IDLE || status == OnlineStatus.DO_NOT_DISTURB);
    }

    private static void goOnline(@NotNull JDA jda) {
        jda.getPresence().setStatus(OnlineStatus.ONLINE);
    }

    private static void writeLockFile() {
        try {
            Files.createDirectories(CACHE_DIRECTORY);
            Files.writeString(LOCK_FILE, ProcessHandle.current().pid() + "\n" + Instant.now() + "\n");
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't write " + LOCK_FILE, e);
        }
    }

    @NotNull
    private static String loadOrCreateToken() {
        try {
            if (Files.exists(TOKEN_FILE)) {
                String token = Files.readString(TOKEN_FILE).strip();
                if (token.matches("[0-9a-f]{8}")) return token;
            }

            byte[] bytes = new byte[4];
            new SecureRandom().nextBytes(bytes);
            String token = HexFormat.of().formatHex(bytes);

            Files.createDirectories(CACHE_DIRECTORY);
            Files.writeString(TOKEN_FILE, token);
            return token;
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't access " + TOKEN_FILE, e);
        }
    }

    public synchronized boolean acquire(@NotNull JDA jda, boolean allowConcurrent) {
        if (acquired) {
            goOnline(jda);
            return true;
        }

        boolean crashed = Files.exists(LOCK_FILE);
        DebugLogging.basic("Checking for other backends controlling this bot (token {}, previous run crashed: {})", token, crashed);

        List<Marker> markers = jda.retrieveApplicationEmojis().complete().stream()
                .map(Marker::parse)
                .filter(Objects::nonNull)
                .toList();

        Optional<Marker> foreign = markers.stream()
                .filter(marker -> !marker.isOwnedBy(token))
                .filter(Marker::isFresh)
                .findFirst();

        Optional<Marker> own = markers.stream()
                .filter(marker -> marker.isOwnedBy(token))
                .filter(Marker::isFresh)
                .findFirst();

        if (foreign.isPresent()) {
            String reason = "This bot is already controlled by another ComitasBotJ instance (" + foreign.get().token()
                    + ", last seen " + foreign.get().secondsSinceSeen() + "s ago).";
            if (!allowConcurrent)
                return reject(reason, "Stop the other instance, or wait up to " + STALE_AFTER.toSeconds() + "s if it crashed.");
            logger.warn("{} Continuing, as --allow-concurrent-instances is set.", reason);
        }

        if (own.isPresent() && !crashed) {
            String reason = "This bot is already controlled by a ComitasBotJ instance using the same files (last seen " + own.get().secondsSinceSeen() + "s ago).";
            if (!allowConcurrent)
                return reject(reason, "Make sure this directory isn't already running somewhere else.");
            logger.warn("{} Continuing, as --allow-concurrent-instances is set.", reason);
        }

        if (crashed)
            logger.warn("ComitasBotJ wasn't shut down cleanly, skipping presence check and replacing the old instance marker");
        else if (appearsOnline(jda)) {
            String reason = "This bot is already online, it's likely controlled by another backend.";
            if (!allowConcurrent)
                return reject(reason, "Stop the other backend, or wait for the bot to show as offline.");
            logger.warn("{} Continuing, as --allow-concurrent-instances is set.", reason);
        }

        markers.stream()
                .filter(marker -> marker.isOwnedBy(token) || !marker.isFresh())
                .forEach(marker -> marker.emoji().delete().queue(null, e -> DebugLogging.basic("Failed to remove old instance marker {}", marker.emoji().getName())));

        emojiId = jda.createApplicationEmoji(currentName(), Icon.from(ICON)).complete().getId();
        writeLockFile();
        acquired = true;

        heartbeatExecutor.scheduleAtFixedRate(
                this::beat,
                HEARTBEAT_INTERVAL.toSeconds(), HEARTBEAT_INTERVAL.toSeconds(), TimeUnit.SECONDS
        );

        goOnline(jda);
        return true;
    }

    public synchronized void release() {
        heartbeatExecutor.shutdownNow();
        if (!acquired) return;
        acquired = false;

        JDA jda = DiscordAPI.nullableApi();
        String id = emojiId;
        if (jda == null || id == null) return;

        DebugLogging.basic("Removing instance marker");
        try {
            jda.retrieveApplicationEmojiById(id).flatMap(ApplicationEmoji::delete).complete();
            Files.deleteIfExists(LOCK_FILE);
        } catch (Exception e) {
            DebugLogging.basic("Failed to remove instance marker, the next start will replace it");
        }
    }

    private boolean reject(@NotNull String reason, @NotNull String hint) {
        logger.error("""
                {}
                
                Running multiple backends on the same bot causes every event and command to be handled twice.
                {}
                To start anyway, restart with --allow-concurrent-instances.
                """, reason, hint);
        return false;
    }

    private void beat() {
        JDA jda = DiscordAPI.nullableApi();
        String id = emojiId;
        if (jda == null || id == null) return;

        try {
            jda.retrieveApplicationEmojiById(id)
                    .flatMap(emoji -> emoji.getManager().setName(currentName()))
                    .complete();
        } catch (ErrorResponseException e) {
            if (e.getErrorResponse() == ErrorResponse.UNKNOWN_EMOJI) markerLost();
            else logger.warn("Failed to refresh instance marker, retrying on the next heartbeat", e);
        } catch (Exception e) {
            logger.warn("Failed to refresh instance marker, retrying on the next heartbeat", e);
        }
    }

    private void markerLost() {
        acquired = false;
        emojiId = null;
        logger.error("""
                The instance marker of this bot was removed, another ComitasBotJ instance is now controlling this bot.
                
                This happens when this directory was copied while running and the copy got started,
                or when the same directory got started twice. Shutting down.
                """);
        new Thread(() -> System.exit(1), "ComitasInstanceLockExit").start();
    }

    @NotNull
    private String currentName() {
        return PREFIX + token + "_" + Long.toString(Instant.now().getEpochSecond(), 36);
    }

    private record Marker(@NotNull ApplicationEmoji emoji, @NotNull String token, @NotNull Instant lastSeen) {
        @Nullable
        static Marker parse(@NotNull ApplicationEmoji emoji) {
            if (!emoji.getName().startsWith(PREFIX)) return null;

            String[] parts = emoji.getName().substring(PREFIX.length()).split("_");
            if (parts.length != 2) return null;

            try {
                return new Marker(emoji, parts[0], Instant.ofEpochSecond(Long.parseLong(parts[1], 36)));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        boolean isOwnedBy(@NotNull String token) {
            return this.token.equals(token);
        }

        boolean isFresh() {
            return lastSeen.plus(STALE_AFTER).isAfter(Instant.now());
        }

        long secondsSinceSeen() {
            return Duration.between(lastSeen, Instant.now()).toSeconds();
        }
    }
}
