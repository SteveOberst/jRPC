package net.sxlver.jrpc.core.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtil {
    public static String logTimeFromMillis(final long millis) {
        return DateTimeFormatter.ofPattern("HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(millis));
    }

    public static String formatDuration(final long millis) {
        final Duration duration = Duration.ofMillis(millis);
        final long days = duration.toDays();
        final long hours = duration.toHours() - duration.toDays() * 24L;
        final long minutes = duration.toMinutes() - duration.toHours() * 60L;
        final long seconds = duration.getSeconds() - duration.toMinutes() * 60L;
        final StringBuilder stringBuilder = new StringBuilder();
        append(stringBuilder, days, "d");
        append(stringBuilder, hours, "h");
        append(stringBuilder, minutes, "m");
        append(stringBuilder, seconds, "s");
        return stringBuilder.toString().endsWith(" ") ? stringBuilder.deleteCharAt(stringBuilder.toString().length() - 1).toString() : stringBuilder.toString();
    }

    private static void append(final StringBuilder builder, final long time, final String prefix) {
        if (time != 0L) {
            builder.append(time).append(prefix).append(" ");
        }
    }
}
