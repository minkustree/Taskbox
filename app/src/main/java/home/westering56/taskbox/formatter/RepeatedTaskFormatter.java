package home.westering56.taskbox.formatter;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import org.dmfs.rfc5545.Weekday;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import home.westering56.taskbox.R;

import static home.westering56.taskbox.RecurrencePickerHelper.getWeekOrdinalForDate;
import static home.westering56.taskbox.RecurrencePickerHelper.weekdayFromDate;
import static org.dmfs.rfc5545.recur.RecurrenceRule.Part.BYMONTHDAY;

public class RepeatedTaskFormatter {

    public static String format(@NonNull Context context, @NonNull RecurrenceRule rule) {
        StringBuilder sb = new StringBuilder("Every ");
        final Resources res = context.getResources();
        int interval = rule.getInterval();
        if (interval > 1) sb.append(interval).append(" ");
        switch (rule.getFreq()) {
            case YEARLY:
                sb.append(res.getQuantityString(R.plurals.repeat_year_label, interval));
                break;
            case MONTHLY:
                sb.append(res.getQuantityString(R.plurals.repeat_month_label, interval));
                appendMonthlyParts(context.getResources(), sb, rule);
                break;
            case WEEKLY:
                sb.append(res.getQuantityString(R.plurals.repeat_week_label, interval));
                appendByWeekdayParts(context.getResources(), sb, rule.getByDayPart());
                break;
            case DAILY:
                sb.append(res.getQuantityString(R.plurals.repeat_day_label, interval));
                break;
            default:
                throw new IllegalArgumentException("Only rules with yearly - daily frequencies are supported");
        }
        return sb.toString();
    }

    private static void appendMonthlyParts(@NonNull Resources resources, @NonNull StringBuilder sb, @NonNull final RecurrenceRule rule) {
        if (rule.hasPart(BYMONTHDAY)) { // every month on the 30th or last
            final int dayValue = rule.getByPart(BYMONTHDAY).get(0);
            if (dayValue > 0) { // every month on the 30th
                sb.append(" on the ").append(ordinalToAbbrevString(dayValue));
            } else { // every month on the last day of the month (assume any -ve is 'last')
                sb.append(" on the last day of the month");
            }
        } else if (rule.hasPart(RecurrenceRule.Part.BYDAY)) { // every month on the third Thursday
            final RecurrenceRule.WeekdayNum weekdayNum = rule.getByDayPart().get(0);
            sb.append(" on the ").append(ordinalToString(resources, weekdayNum.pos)).append(" ");
            sb.append(longStringFor(resources, weekdayNum.weekday));
        }
    }

    @NonNull
    private static String longStringFor(@NonNull Resources res, @NonNull Weekday weekday) {
        return res.getStringArray(R.array.repeat_weekday_long)[weekday.ordinal()];
    }

    @NonNull
    private static String shortStringFor(@NonNull Resources res, @NonNull Weekday weekday) {
        return res.getStringArray(R.array.repeat_weekday_short)[weekday.ordinal()];
    }

    static void appendByWeekdayParts(Resources res, StringBuilder sb, List<RecurrenceRule.WeekdayNum> byDayPart) {
        if (byDayPart == null || byDayPart.size() == 0) return;
        sb.append(" on ");
        if (byDayPart.size() == 1) {
            sb.append(longStringFor(res, byDayPart.get(0).weekday));
        } else {
            sb.append(byDayPart.stream()
                    .map(weekdayNum -> shortStringFor(res, weekdayNum.weekday))
                    .collect(Collectors.joining(", ")));
        }
    }

    public static String getMonthlyNthDayOfWeekText(@NonNull final Resources resources, @NonNull final LocalDate date) {
        // first week, second week, etc. -1 = 'last week'
        final String ordinalText = ordinalToString(resources, getWeekOrdinalForDate(date));
        // day of week
        final String dowText = resources.getStringArray(R.array.repeat_weekday_long)[weekdayFromDate(date).ordinal()];
        // generate the text
        return resources.getString(R.string.snooze_custom_repeat_monthly_nth_day_of_week, ordinalText, dowText);

    }

    private static String ordinalToString(@NonNull final Resources resources, final int ord) {
        switch (ord) {
            case -1:
                return resources.getString(R.string.ordinal_last);
            case 1:
                return resources.getString(R.string.ordinal_first);
            case 2:
                return resources.getString(R.string.ordinal_second);
            case 3:
                return resources.getString(R.string.ordinal_third);
            case 4:
                return resources.getString(R.string.ordinal_fourth);
            default:
                return ordinalToAbbrevString(ord);
        }
    }

    private static String ordinalToAbbrevString(final int ord) {
        final String pattern;
        switch (ord % 10) {
            case 1: pattern = "%dst"; break;
            case 2: pattern = "%dnd"; break;
            case 3: pattern = "%drd"; break;
            default: pattern = "%dth";
        }
        return String.format(pattern, ord);
    }
}
