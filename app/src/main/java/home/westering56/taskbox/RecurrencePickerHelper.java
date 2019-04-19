package home.westering56.taskbox;

import androidx.annotation.NonNull;

import org.dmfs.rfc5545.Weekday;

import java.time.LocalDate;
import java.util.Objects;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

public class RecurrencePickerHelper {
    private static boolean isInLastWeekOfTheMonth(@NonNull final LocalDate date) {
        Objects.requireNonNull(date);
        return !date.plusWeeks(1).getMonth().equals(date.getMonth());
    }

    /**
     * The is the date first (1), second (2), third (3), fourth (4) or last (-1) week of the month?
     */
    public static int getWeekOrdinalForDate(@NonNull LocalDate date) {
        if (isInLastWeekOfTheMonth(date)) return -1;
        else {
            return 1 + ((date.getDayOfMonth() - 1) / 7);
        }
    }

    public static String getStringForOrdinal(final int ord) {
        switch (ord) {
            case -2:
                return "second-to-last";
            case -1:
                return "last";
            case 1:
                return "first";
            case 2:
                return "second";
            case 3:
                return "third";
            case 4:
                return "fourth";
            default:
                return Integer.toString(ord);
        }
    }

    public static Weekday weekdayFromDate(@NonNull LocalDate date) {
        // convert from mon-start, 1-based (mon = 1, sun = 7) to sun-start, 0-based (sun = 0, mon = 1)
        return Weekday.values()[date.getDayOfWeek().getValue() % 7];
    }

    public static boolean isLastDayOfTheMonth(@NonNull final LocalDate date) {
        return date.with(lastDayOfMonth()).equals(date);
    }
}
