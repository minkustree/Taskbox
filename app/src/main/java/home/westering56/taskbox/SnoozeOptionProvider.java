package home.westering56.taskbox;

import android.content.Context;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import home.westering56.taskbox.formatter.SnoozeTimeFormatter;

import static home.westering56.taskbox.Adjusters.AfternoonAdjuster;
import static home.westering56.taskbox.Adjusters.EveningAdjuster;
import static home.westering56.taskbox.Adjusters.MorningAdjuster;
import static home.westering56.taskbox.Adjusters.NextAfternoon;
import static home.westering56.taskbox.Adjusters.NextMorning;
import static home.westering56.taskbox.Adjusters.NextEvening;
import static home.westering56.taskbox.Adjusters.StartOfWeekAdjuster;
import static home.westering56.taskbox.Adjusters.TomorrowMorningAdjuster;
import static home.westering56.taskbox.Adjusters.WeekendAdjuster;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;

/**
 * Provides relevant snooze option choices.
 */
public class SnoozeOptionProvider {
    private static final String SNOOZE_OPTION_TITLE = "option_title";
    private static final String SNOOZE_OPTION_INSTANT = "option_instant";
    private static final String SNOOZE_OPTION_ICON = "option_icon";

    private final List<Map<String, Object>> snoozeOptions;
    private SimpleAdapter mAdapter;

    /**
     * Obtains a new adapter with current snooze time data. Snooze time values to choose from
     * are calculated when this is called, so get a new one each time you need an up-to-date set
     * of snooze time options.
     */
    public static SimpleAdapter newAdapter(@NonNull Context context) {
        return new SnoozeOptionProvider().getAdapter(context);
    }

    private SnoozeOptionProvider() {
        snoozeOptions = initSnoozeOptions();
    }


    private ArrayList<Map<String, Object>> initSnoozeOptions() {
        ArrayList<Map<String, Object>> options = new ArrayList<>();
        options.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "Tomorrow Morning");
            put(SNOOZE_OPTION_INSTANT, TomorrowMorningAdjuster.adjustInto(LocalDateTime.now()));
            put(SNOOZE_OPTION_ICON, R.drawable.ic_morning_24dp);
        }});
        options.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "This Afternoon");
            put(SNOOZE_OPTION_INSTANT, AfternoonAdjuster.adjustInto(LocalDateTime.now()));
            put(SNOOZE_OPTION_ICON, R.drawable.ic_restaurant_black_24dp);
        }});
        options.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "This Evening");
            put(SNOOZE_OPTION_INSTANT, EveningAdjuster.adjustInto(LocalDateTime.now()));
            put(SNOOZE_OPTION_ICON, R.drawable.ic_hot_tub_black_24dp);
        }});
        options.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "Next Week");
            put(SNOOZE_OPTION_INSTANT, StartOfWeekAdjuster.adjustInto(LocalDateTime.now()));
            put(SNOOZE_OPTION_ICON, R.drawable.ic_next_week_black_24dp);
        }});
        options.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "This Weekend");
            put(SNOOZE_OPTION_INSTANT, WeekendAdjuster.adjustInto(LocalDateTime.now()));
            put(SNOOZE_OPTION_ICON, R.drawable.ic_weekend_black_24dp);
        }});
        options.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "In 30 seconds");
            put(SNOOZE_OPTION_INSTANT, LocalDateTime.now().plusSeconds(30));
            put(SNOOZE_OPTION_ICON, R.drawable.ic_schedule_black_24dp);
        }});
        return options;
    }

    private SimpleAdapter getAdapter(@NonNull Context context) {
        synchronized (this) {
            if (mAdapter == null) {
                mAdapter = new SimpleAdapter(
                        context,
                        snoozeOptions,
                        R.layout.snooze_option_item,
                        new String[]{SNOOZE_OPTION_TITLE, SNOOZE_OPTION_INSTANT, SNOOZE_OPTION_ICON},
                        new int[]{R.id.snooze_option_item_title, R.id.snooze_option_item_detail, R.id.snooze_option_item_ic});
                mAdapter.setViewBinder((view, data, textRepresentation) -> {
                    if (data instanceof Temporal) {
                        ((TextView)view).setText(
                                SnoozeTimeFormatter.format(view.getContext(), (Temporal) data));
                        return true;
                    }
                    return false;
                });
            }
        }
        return mAdapter;
    }

    public static LocalDateTime getDateTimeAtPosition(@NonNull AdapterView<?> parent, int position) {
        //noinspection unchecked
        Map<String, Object> item = (Map<String, Object>) parent.getItemAtPosition(position);
        return (LocalDateTime) item.get(SNOOZE_OPTION_INSTANT);
    }

    /**
     * Get the list of snooze options that should be shown for a given date
     * @param date
     * @return
     */
    public static List<LocalDateTime> getOptionsForDate(LocalDateTime date) {
        ArrayList<LocalDateTime> results = new ArrayList<>();
        results.add(date.with(NextMorning));
        results.add(date.with(NextAfternoon));
        results.add(date.with(NextEvening));
        results.add(date.with(WeekendAdjuster));
        results.add(date.with(StartOfWeekAdjuster));
        Collections.sort(results);
        return results;
    }


    public static CharSequence getLabelForOptionDateTime(LocalDateTime now, LocalDateTime target) {
        StringBuilder sb = new StringBuilder();

        // Exception for weekend
        if (now.with(WeekendAdjuster).equals(target)) {
            if (now.getDayOfWeek() == SATURDAY || now.getDayOfWeek() == SUNDAY) {
                // if we're before 9am on Sat, then it's "This Weekend"
                if (now.toLocalDate().equals(target.toLocalDate()) && now.isBefore(target)) {
                    return "This Weekend";
                }
                else { // if we're after 9am on Sat, Sun then it's "Next Weekend"
                    return "Next Weekend";
                }
            }
            // For every other day of the week, it's this weekend
            return "This Weekend";
        }

        // Exception for Next Week
        if (now.with(StartOfWeekAdjuster).equals(target)) {
            // pre 9am on Monday
            if (now.getDayOfWeek() == MONDAY &&
                    now.toLocalDate().equals(target.toLocalDate()) &&
                    now.isBefore(target)) {
                return "This Week";
            } else {
                return "Next Week";
            }
        }
        // Date part
        final LocalDate nowDate = now.toLocalDate();
        final LocalDate targetDate = target.toLocalDate();

        // Today if it's the same day, tomorrow if it's the day after.
        if (nowDate.equals(targetDate)) {
            sb.append("This");
        } else if (nowDate.plusDays(1).equals(targetDate)) {
            sb.append("Tomorrow");
        } else {
            sb.append(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(targetDate));
        }

        sb.append(" ");

        // Time part
        final LocalTime targetTime = target.toLocalTime();

        if (targetTime.with(MorningAdjuster).equals(targetTime)) {
            sb.append("Morning");
        } else if (targetTime.with(AfternoonAdjuster).equals(targetTime)) {
            sb.append("Afternoon");
        } else if (targetTime.with(EveningAdjuster).equals(targetTime)) {
            sb.append("Evening");
        } else {
            sb.append(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(targetTime));
        }
        return sb.toString();
    }

}
