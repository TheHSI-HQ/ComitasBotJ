package cloud.thehsi.ComitasBotJ.Discord.Message.Attachment;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the duration and an approximated waveform from an Ogg Opus file, without decoding the audio.
 * <p>
 * The waveform is based on the bitrate of the Opus packets: Opus spends more bytes on loud / complex
 * audio and next to none on silence, which makes the bitrate a good enough stand-in for the amplitude.
 */
public class OggOpusAnalyzer {
    private static final int OPUS_SAMPLE_RATE = 48000;
    private static final int MAX_WAVEFORM_LENGTH = 256;

    private OggOpusAnalyzer() {}

    public record Result(@NotNull Duration duration, byte[] waveform) {}

    private record Packet(int size, double durationMs) {}

    @NotNull
    public static Result analyze(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        List<byte[]> packets = new ArrayList<>();
        ByteArrayOutputStream currentPacket = new ByteArrayOutputStream();
        long lastGranule = -1;
        Integer serial = null;
        int pageStart = 0;

        while (data.length - pageStart >= 27) {
            if (!"OggS".equals(new String(data, pageStart, 4, StandardCharsets.US_ASCII)))
                throw new IllegalArgumentException("Voice message must be an Ogg Opus file (invalid Ogg page)");

            long granule = buffer.getLong(pageStart + 6);
            int pageSerial = buffer.getInt(pageStart + 14);
            int segmentCount = data[pageStart + 26] & 0xFF;
            int segmentTableStart = pageStart + 27;
            int bodyStart = segmentTableStart + segmentCount;

            if (bodyStart > data.length)
                throw new IllegalArgumentException("Voice message must be an Ogg Opus file (truncated Ogg page)");

            int bodyLength = 0;
            for (int i = 0; i < segmentCount; i++)
                bodyLength += data[segmentTableStart + i] & 0xFF;

            if (bodyStart + bodyLength > data.length)
                throw new IllegalArgumentException("Voice message must be an Ogg Opus file (truncated Ogg page)");

            if (serial == null)
                serial = pageSerial;

            // Only the first logical stream is analyzed
            if (pageSerial == serial) {
                if (granule != -1)
                    lastGranule = granule;

                int offset = bodyStart;
                for (int i = 0; i < segmentCount; i++) {
                    int lacing = data[segmentTableStart + i] & 0xFF;
                    currentPacket.write(data, offset, lacing);
                    offset += lacing;

                    if (lacing < 255) {
                        packets.add(currentPacket.toByteArray());
                        currentPacket.reset();
                    }
                }
            }

            pageStart = bodyStart + bodyLength;
        }

        if (packets.size() < 2 || !startsWith(packets.get(0), "OpusHead") || packets.get(0).length < 19)
            throw new IllegalArgumentException("Voice message must be an Ogg Opus file (missing OpusHead)");

        int preSkip = ByteBuffer.wrap(packets.get(0)).order(ByteOrder.LITTLE_ENDIAN).getShort(10) & 0xFFFF;
        long samples = lastGranule - preSkip;

        if (samples <= 0)
            throw new IllegalArgumentException("Voice message does not contain any audio");

        // Packet 0 is OpusHead, packet 1 is OpusTags, everything after is audio
        List<Packet> audioPackets = packets.subList(2, packets.size()).stream()
                .filter(e -> e.length > 0)
                .map(e -> new Packet(e.length, packetDurationMs(e)))
                .toList();

        return new Result(
                Duration.ofNanos(samples * 1_000_000_000L / OPUS_SAMPLE_RATE),
                waveform(audioPackets)
        );
    }

    private static boolean startsWith(byte[] packet, @NotNull String magic) {
        return packet.length >= magic.length()
                && magic.equals(new String(packet, 0, magic.length(), StandardCharsets.US_ASCII));
    }

    /**
     * Calculates the duration of an Opus packet from its TOC byte, see RFC 6716 section 3.1
     */
    private static double packetDurationMs(byte[] packet) {
        int toc = packet[0] & 0xFF;
        int config = toc >> 3;

        double frameMs;
        if (config < 12)
            frameMs = new double[]{10, 20, 40, 60}[config % 4];
        else if (config < 16)
            frameMs = new double[]{10, 20}[config % 2];
        else
            frameMs = new double[]{2.5, 5, 10, 20}[config % 4];

        int frameCount = switch (toc & 0b11) {
            case 0 -> 1;
            case 1, 2 -> 2;
            default -> packet.length > 1 ? packet[1] & 0x3F : 1;
        };

        return frameMs * Math.max(frameCount, 1);
    }

    private static byte[] waveform(@NotNull List<Packet> packets) {
        if (packets.isEmpty())
            return new byte[]{0};

        int length = Math.min(MAX_WAVEFORM_LENGTH, packets.size());
        double totalMs = packets.stream().mapToDouble(Packet::durationMs).sum();

        double[] bytes = new double[length];
        double[] milliseconds = new double[length];

        double elapsedMs = 0;
        for (Packet packet : packets) {
            int bucket = (int) Math.min(length - 1, elapsedMs / totalMs * length);
            bytes[bucket] += packet.size();
            milliseconds[bucket] += packet.durationMs();
            elapsedMs += packet.durationMs();
        }

        double[] bitrates = new double[length];
        double max = 0;
        for (int i = 0; i < length; i++) {
            if (milliseconds[i] > 0)
                bitrates[i] = bytes[i] / milliseconds[i];
            else // Bucket fell between two packets, continue the previous one
                bitrates[i] = i > 0 ? bitrates[i - 1] : 0;

            max = Math.max(max, bitrates[i]);
        }

        // Scaled against the peak only, so silence (next to no bytes) stays close to 0
        byte[] waveform = new byte[length];
        for (int i = 0; i < length; i++)
            waveform[i] = (byte) (max == 0 ? 0 : Math.round(bitrates[i] / max * 255));

        return waveform;
    }
}
