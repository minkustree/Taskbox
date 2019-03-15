package home.westering56.taskbox;

import android.content.Context;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

class SnoozeTimeFormatter {
    private static final DateTimeFormatter sDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    private static final DateTimeFormatter sThisYearDateFormatter = DateTimeFormatter.ofPattern("eee dd MMM");
    private static final DateTimeFormatter sTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

    public static CharSequence format(final Context context, Temporal target) {
        return formatDate(context, LocalDate.from(target)) + ", " + formatTime(LocalTime.from(target));
    }

    private static CharSequence formatDate(final Context context, final LocalDate target) {
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

    private static CharSequence formatTime(final LocalTime target) {
        return target.format(sTimeFormatter);
    }
}
