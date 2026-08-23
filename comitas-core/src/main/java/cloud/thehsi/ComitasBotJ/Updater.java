package cloud.thehsi.ComitasBotJ;

import cloud.thehsi.ComitasBotJ.Bot.InternalComitas;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Updater {
    @NotNull
    static final String GITHUB_REPO = "TheHSI-HQ/ComitasBotJ";
    @NotNull
    static final ExecutorService executorService = Executors.newCachedThreadPool();

    private @NotNull
    static final DecimalFormat DF = new DecimalFormat("0.00",
            DecimalFormatSymbols.getInstance(Locale.ROOT));

    private static void log(@NotNull String message, @Nullable Object... args) {
        for (Object arg : args) {
            message = message.replaceFirst("\\{}", java.util.regex.Matcher.quoteReplacement(String.valueOf(arg)));
        }
        System.out.println(message);
    }

    private static void logError(@NotNull String message, @NotNull Throwable throwable) {
        System.err.println(message);
        throwable.printStackTrace(System.err);
    }

    private static byte[] downloadFile(@NotNull URL url) throws IOException {
        URLConnection connection = url.openConnection();
        long totalBytes = connection.getContentLengthLong();

        try (ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
             ByteArrayOutputStream out = new ByteArrayOutputStream(
                     totalBytes > 0 && totalBytes <= Integer.MAX_VALUE
                             ? (int) totalBytes
                             : 8192)) {

            ByteBuffer buffer = ByteBuffer.allocateDirect(64 * 1024);

            long downloaded = 0;
            long lastDownloaded = 0;
            long lastReport = System.currentTimeMillis();

            while (rbc.read(buffer) != -1) {
                buffer.flip();

                while (buffer.hasRemaining()) {
                    int remaining = buffer.remaining();
                    byte[] chunk = new byte[remaining];
                    buffer.get(chunk);
                    out.write(chunk);

                    downloaded += remaining;
                }

                buffer.clear();

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

    @NotNull
    private static String getLatestRelease() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/" + GITHUB_REPO + "/releases/latest"))
                    .header("User-Agent", "ComitasBotJ")
                    .header("Accept", "application/vnd.github+json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

            return json.get("tag_name").getAsString();
        }
    }

    @NotNull
    static CompletableFuture<byte[]> downloadNewestResource() {
        CompletableFuture<byte[]> future = new CompletableFuture<>();

        executorService.submit(() -> {
            try {
                log("Fetching latest Version...");
                String latest = getLatestRelease();

                log("Downloading release {}...", latest);
                URL downloadUrl = URI.create("https://github.com/" + GITHUB_REPO + "/releases/download/" + latest + "/comitas-core-" + latest.replaceFirst("v", "") + ".jar").toURL();

                future.complete(downloadFile(downloadUrl));
            } catch (Exception e) {
                logError("Exception whilst downloading latest resource", e);
                future.complete(null);
            }
        });

        return future;
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

        downloadNewestResource().whenCompleteAsync((bytes, throwable) -> {
            if (throwable != null) {
                logError("Download failed", throwable);
                return;
            }

            InternalComitas.addShutdownCall(() -> {
                try {
                    Files.write(
                            jarPath,
                            bytes,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE
                    );
                } catch (IOException e) {
                    logError("Failed to write update", e);
                }
            });

            System.exit(0);
        });
    }
}
