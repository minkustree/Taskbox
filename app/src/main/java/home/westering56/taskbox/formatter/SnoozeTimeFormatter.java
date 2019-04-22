package home.westering56.taskbox.formatter;

import android.content.Context;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

import androidx.annotation.NonNull;

import home.westering56.taskbox.R;
import home.westering56.taskbox.data.room.Task;

public class SnoozeTimeFormatter {
    private static final DateTimeFormatter sDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    private static final DateTimeFormatter sThisYearDateFormatter = DateTimeFormatter.ofPattern("eee dd MMM");
    private static final DateTimeFormatter sTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

    public static CharSequence format(final Context context, Temporal target) {
        return formatDate(context, LocalDate.from(target)) + ", " + formatTime(LocalTime.from(target));
    }

    /** Converts the instant to a local date time in the default time zone, and then formats it */
    private static CharSequence formatInstant(@NonNull final Context context, Instant instant) {
        return format(context, LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }

    public static CharSequence formatDate(final Context context, final LocalDate target) {
        LocalDate today = LocalDate.now();
        if (target.equals(today)) {
            return context.getString(R.string.snooze_time_format_today);
        } else if (target.equals(today.plus(1, ChronoUnit.DAYS))) {
            return context.getString(R.string.snooze_time_format_tomorrow);
        } else if (Year.from(target).equals(Year.from(today))) {
            return target.format(sThisYearDateFormatter);
        } else {
            return target.format(sDateFormatter);
        }
    }

    public static CharSequence formatTime(final LocalTime target) {
        return target.format(sTimeFormatter);
    }

    public static CharSequence formatStatusLine(@NonNull Context context, @NonNull Task task) {
        CharSequence snoozeTime = SnoozeTimeFormatter.formatInstant(context, task.snoozeUntil);
        return context.getString(R.string.task_detail_snoozed_until, snoozeTime);
    }

    public static CharSequence formatAdapterLine(@NonNull Context context, Instant snoozeUntil) {
        CharSequence snoozeTime = SnoozeTimeFormatter.formatInstant(context, snoozeUntil);
        return context.getString(R.string.task_detail_snoozed_until, snoozeTime);
    }
}
