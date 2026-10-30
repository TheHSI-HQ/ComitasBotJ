package cloud.thehsi.ComitasBotJ.App;

import cloud.thehsi.ComitasBotJ.Bot.InternalComitas;
import cloud.thehsi.ComitasBotJ.Main;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipFile;

public class Updater {
    @NotNull
    static final String GITHUB_REPO = "TheHSI-HQ/ComitasBotJ";
    private static final long MAX_DOWNLOAD_SIZE = 512L * 1024 * 1024;
    @NotNull
    static final ExecutorService executorService = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "ComitasUpdater");
        t.setDaemon(true);
        return t;
    });

    @NotNull
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static @NotNull
    final ReleaseCache releaseCache = new ReleaseCache();

    /**
     * @param version  The release tag, like {@code v0.0.17}
     * @param download The core jar of the release
     * @param sha256   The hex SHA-256 of the core jar, as computed by GitHub
     */
    record Release(@NotNull String version, @NotNull URI download, @NotNull String sha256) {
    }

    @NotNull
    private static Release getLatestRelease() throws IOException, InterruptedException {
        synchronized (releaseCache) {
            if (releaseCache.isValid() && releaseCache.release != null) {
                if (DebugLogging.isBasicEnabled())
                    log("Using cached release version: {}", releaseCache.release.version());
                return releaseCache.release;
            }
        }

        IOException lastException = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.github.com/repos/" + GITHUB_REPO + "/releases/latest"))
                        .header("User-Agent", "ComitasBotJ")
                        .header("Accept", "application/vnd.github+json")
                        .timeout(Duration.ofSeconds(10))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new IOException("GitHub API returned status " + response.statusCode());
                }

                Release release = parseRelease(JsonParser.parseString(response.body()).getAsJsonObject());

                synchronized (releaseCache) {
                    releaseCache.release = release;
                    releaseCache.cachedTime = System.currentTimeMillis();
                }

                return release;

            } catch (IOException e) {
                lastException = e;
                if (attempt < 2) {
                    long backoffMs = 1000 * (long) Math.pow(2, attempt);
                    log("Retry {} for getLatestRelease, backing off {}ms", attempt + 1, backoffMs);
                    Thread.sleep(backoffMs);
                }
            } catch (RuntimeException e) {
                throw new IOException("Unexpected response of the GitHub API", e);
            }
        }

        throw lastException;
    }

    @NotNull
    static Release parseRelease(@NotNull JsonObject json) throws IOException {
        String version = json.get("tag_name").getAsString();
        String assetName = "comitas-core-" + version.replaceFirst("^v", "") + ".jar";

        for (JsonElement element : json.getAsJsonArray("assets")) {
            JsonObject asset = element.getAsJsonObject();
            if (!asset.get("name").getAsString().equals(assetName)) continue;

            JsonElement digest = asset.get("digest");
            if (digest == null || digest.isJsonNull() || !digest.getAsString().startsWith("sha256:"))
                throw new IOException("Release " + version + " doesn't provide a SHA-256 digest for " + assetName
                        + ", refusing to install an unverified update");

            return new Release(version, URI.create(asset.get("browser_download_url").getAsString()),
                    digest.getAsString().substring("sha256:".length()).toLowerCase(Locale.ROOT));
        }

        throw new IOException("Release " + version + " doesn't contain " + assetName);
    }

    private @NotNull
    static final DecimalFormat DF = new DecimalFormat("0.00",
            DecimalFormatSymbols.getInstance(Locale.ROOT));

    private static void log(@NotNull String message, @Nullable Object... args) {
        for (Object arg : args) {
            message = message.replaceFirst("\\{}", java.util.regex.Matcher.quoteReplacement(String.valueOf(arg)));
        }
        //noinspection JavaPrintToLogpoint
        System.out.println(message);
    }

    private static void logError(@NotNull String message, @NotNull Throwable throwable) {
        System.err.println(message);
        throwable.printStackTrace(System.err);
    }

    private static byte[] downloadFile(@NotNull URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("User-Agent", "ComitasBotJ")
                .timeout(Duration.ofMinutes(10))
                .build();
        HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            response.body().close();
            throw new IOException("Download returned status " + response.statusCode());
        }
        long totalBytes = response.headers().firstValueAsLong("Content-Length").orElse(-1);
        if (totalBytes > MAX_DOWNLOAD_SIZE) throw new IOException("The download is too large (" + totalBytes + " bytes)");

        try (InputStream in = response.body();
             ByteArrayOutputStream out = new ByteArrayOutputStream(
                     totalBytes > 0 ? (int) totalBytes : 8192)) {

            byte[] buffer = new byte[64 * 1024];

            long downloaded = 0;
            long lastDownloaded = 0;
            long lastReport = System.currentTimeMillis();

            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                downloaded += read;
                if (downloaded > MAX_DOWNLOAD_SIZE) throw new IOException("The download is too large");

                long now = System.currentTimeMillis();
                long elapsed = now - lastReport;

                if (elapsed >= 1000) {
                    double speedMBps = (downloaded - lastDownloaded)
                            / 1024.0 / 1024.0
                            / (elapsed / 1000.0);

                    if (totalBytes > 0) {
                        double percent = downloaded * 100.0 / totalBytes;

                        long remainingBytes = totalBytes - downloaded;
                        long etaSeconds = speedMBps > 0
                                ? (long) (remainingBytes / (speedMBps * 1024 * 1024))
                                : -1;

                        log(
                                "Downloaded {}% ({} / {} MB) | {} MB/s | ETA {}",
                                DF.format(percent),
                                DF.format(downloaded / 1024.0 / 1024.0),
                                DF.format(totalBytes / 1024.0 / 1024.0),
                                DF.format(speedMBps),
                                formatDuration(etaSeconds)
                        );
                    } else {
                        log(
                                "Downloaded {} MB | {} MB/s",
                                DF.format(downloaded / 1024.0 / 1024.0),
                                DF.format(speedMBps)
                        );
                    }

                    lastDownloaded = downloaded;
                    lastReport = now;
                }
            }

            log("Download complete ({} MB)",
                    DF.format(downloaded / 1024.0 / 1024.0));

            return out.toByteArray();
        }
    }

    @NotNull
    private static String formatDuration(long seconds) {
        if (seconds < 0) {
            return "--:--";
        }

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, secs);
        }

        return String.format(Locale.ROOT, "%02d:%02d", minutes, secs);
    }

    private static class ReleaseCache {
        static final long TTL_MS = 60_000;  // 1 minute
        @Nullable Release release = null;
        long cachedTime = 0L;

        boolean isValid() {
            return (System.currentTimeMillis() - cachedTime) < TTL_MS;
        }
    }

    @NotNull
    static String sha256(byte @NotNull [] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Makes sure the file is a Comitas core jar, before it replaces the running one.
     */
    static void verifyJar(@NotNull Path jar) throws IOException {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            if (zip.getEntry("cloud/thehsi/ComitasBotJ/Main.class") == null || zip.getEntry("version.properties") == null)
                throw new IOException("The downloaded file isn't a ComitasBotJ core jar");
        }
    }

    /**
     * Downloads the latest release next to the running jar, it replaces the running jar on shutdown.
     *
     * @return The staged update, or null if there is nothing to install
     */
    @Nullable
    static Path stageUpdate(@NotNull Path jarPath) throws IOException, InterruptedException {
        log("Fetching latest Version...");
        Release latest = getLatestRelease();

        if (latest.version().replaceFirst("^v", "").equals(Main.getServerVersion())) {
            log("ComitasBotJ {} is already up to date", Main.getServerVersion());
            return null;
        }

        log("Downloading release {}...", latest.version());
        byte[] bytes = downloadFile(latest.download());

        String hash = sha256(bytes);
        if (!hash.equals(latest.sha256()))
            throw new IOException("The SHA-256 of the download (" + hash + ") doesn't match the release (" + latest.sha256() + ")");

        Path staged = jarPath.resolveSibling(jarPath.getFileName() + ".update");
        Files.write(staged, bytes);
        try {
            verifyJar(staged);
        } catch (IOException e) {
            Files.deleteIfExists(staged);
            throw e;
        }
        return staged;
    }

    /**
     * Replaces the running jar, keeping the old one as {@code .bak}.
     */
    static void install(@NotNull Path staged, @NotNull Path jarPath) {
        Path backup = jarPath.resolveSibling(jarPath.getFileName() + ".bak");
        try {
            Files.copy(jarPath, backup, StandardCopyOption.REPLACE_EXISTING);
            try {
                Files.move(staged, jarPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(staged, jarPath, StandardCopyOption.REPLACE_EXISTING);
            }
            log("Update installed, the previous version was kept as {}", backup);
        } catch (IOException e) {
            // E.g. on Windows, where the running jar is locked
            logError("Failed to install the update. The old version is still in place, replace " + jarPath
                    + " with " + staged + " manually.", e);
        }
    }

    public static void update() {
        Path jarPath;
        try {
            jarPath = Paths.get(
                    Main.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );
        } catch (Exception e) {
            logError("Unable to determine JAR path", e);
            return;
        }

        if (!Files.isRegularFile(jarPath) || !jarPath.getFileName().toString().endsWith(".jar")) {
            log("ComitasBotJ isn't running from a jar ({}), it can't be updated in place", jarPath);
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                return stageUpdate(jarPath);
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Update interrupted", e);
            }
        }, executorService).whenComplete((staged, throwable) -> {
            if (throwable != null) {
                logError("Update failed, ComitasBotJ keeps running", throwable);
                return;
            }
            if (staged == null) return;

            InternalComitas.addShutdownCall(() -> install(staged, jarPath));
            log("Update downloaded, restarting...");
            System.exit(0);
        });
    }
}
