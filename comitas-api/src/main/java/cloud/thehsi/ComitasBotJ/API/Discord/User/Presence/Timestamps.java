package cloud.thehsi.ComitasBotJ.API.Discord.User.Presence;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Objects;

public record Timestamps(long start, long end) {
    @Nullable
    public Instant getStartTime() {
        return this.start <= 0L ? null : Instant.ofEpochMilli(this.start);
    }

    @Nullable
    public Instant getEndTime() {
        return this.end <= 0L ? null : Instant.ofEpochMilli(this.end);
    }

    public long getRemainingTime(@NotNull TemporalUnit unit) {
        Instant end = this.getEndTime();
        return end != null ? Instant.now().until(end, unit) : -1L;
    }

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

    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Timestamps(long tStart, long tEnd))) {
            return false;
        } else {
            return this.start == tStart && this.end == tEnd;
        }
    }

    public int hashCode() {
        return Objects.hash(this.start, this.end);
    }
}
