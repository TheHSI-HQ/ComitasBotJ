package cloud.thehsi.ComitasBotJ.API.Discord.User.Presence;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Objects;

/**
 * Represents a pair of two timestamps in unix mills
 *
 * @param start Start time in unix mills
 * @param end   End time in unix mills
 */
public record Timestamps(long start, long end) {
    /**
     * @return The start time as an {@link Instant}
     */
    @Nullable
    public Instant getStartTime() {
        return this.start <= 0L ? null : Instant.ofEpochMilli(this.start);
    }

    /**
     * @return The end time as an {@link Instant}
     */
    @Nullable
    public Instant getEndTime() {
        return this.end <= 0L ? null : Instant.ofEpochMilli(this.end);
    }

    /**
     * @param unit The unit to respond in
     * @return The remaining time in {@code unit}
     */
    public long getRemainingTime(@NotNull TemporalUnit unit) {
        Instant end = this.getEndTime();
        return end != null ? Instant.now().until(end, unit) : -1L;
    }

    /**
     * @param unit The unit to respond in
     * @return The elapsed time in {@code unit}
     */
    public long getElapsedTime(@NotNull TemporalUnit unit) {
        Instant start = this.getStartTime();
        return start != null ? start.until(Instant.now(), unit) : -1L;
    }

    @Override
    public @NotNull String toString() {
        return "Timestamps{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Timestamps(long tStart, long tEnd))) {
            return false;
        } else {
            return this.start == tStart && this.end == tEnd;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.start, this.end);
    }
}
