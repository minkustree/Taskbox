package home.westering56.taskbox.formatter;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.util.List;
import java.util.stream.Collectors;

import home.westering56.taskbox.R;

public class RepeatedTaskFormatter {

    public static String format(@NonNull Context context, @NonNull RecurrenceRule rule) {
        StringBuilder sb = new StringBuilder("Every ");
        final Resources res = context.getResources();
        int interval = rule.getInterval();
        if (interval > 1) sb.append(interval);
        switch (rule.getFreq()) {
            case YEARLY:
                sb.append(res.getQuantityString(R.plurals.repeat_year_label, interval));
                break;
            case MONTHLY:
                sb.append(res.getQuantityString(R.plurals.repeat_month_label, interval));
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

    public static void appendByWeekdayParts(Resources res, StringBuilder sb, List<RecurrenceRule.WeekdayNum> byDayPart) {
        if (byDayPart == null || byDayPart.size() == 0) return;
        sb.append("on ");
        if (byDayPart.size() == 1) {
            int index = byDayPart.get(0).weekday.ordinal();
            sb.append(res.getStringArray(R.array.repeat_weekday_long)[index]);
        } else {
            final String[] weekdayShort = res.getStringArray(R.array.repeat_weekday_short);
            sb.append(byDayPart.stream()
                    .map(weekdayNum -> weekdayShort[weekdayNum.weekday.ordinal()])
                    .collect(Collectors.joining(", ")));

        }
    }
}
