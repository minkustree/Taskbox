package home.westering56.taskbox;

import android.content.Context;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import home.westering56.taskbox.formatter.SnoozeTimeFormatter;

import static home.westering56.taskbox.Adjusters.AfternoonAdjuster;
import static home.westering56.taskbox.Adjusters.EveningAdjuster;
import static home.westering56.taskbox.Adjusters.MorningAdjuster;
import static home.westering56.taskbox.Adjusters.NextAfternoon;
import static home.westering56.taskbox.Adjusters.NextMorning;
import static home.westering56.taskbox.Adjusters.NextEvening;
import static home.westering56.taskbox.Adjusters.NextWeekNotTomorrowMorningAdjuster;
import static home.westering56.taskbox.Adjusters.StartOfWeekAdjuster;
import static home.westering56.taskbox.Adjusters.WeekendAdjuster;
import static home.westering56.taskbox.Adjusters.WeekendNotTomorrowMorningAdjuster;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.temporal.ChronoUnit.WEEKS;

/**
 * Provides relevant snooze option choices.
 */
public class SnoozeOptionProvider {

    @VisibleForTesting static final @DrawableRes int MORNING_ID = R.drawable.ic_morning_24dp;
    @VisibleForTesting static final @DrawableRes int AFTERNOON_ID = R.drawable.ic_restaurant_black_24dp;
    @VisibleForTesting static final @DrawableRes int EVENING_ID = R.drawable.ic_hot_tub_black_24dp;
    @VisibleForTesting static final @DrawableRes int WEEKEND_ID = R.drawable.ic_weekend_black_24dp;
    @VisibleForTesting static final @DrawableRes int NEXT_WEEK_ID = R.drawable.ic_next_week_black_24dp;

    @VisibleForTesting static final String SNOOZE_OPTION_TITLE = "option_title";
    @VisibleForTesting static final String SNOOZE_OPTION_DATETIME = "option_datetime";
    @VisibleForTesting static final String SNOOZE_OPTION_ICON = "option_icon";

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
        snoozeOptions = initSnoozeOptions(LocalDateTime.now());
    }


    private ArrayList<Map<String, Object>> initSnoozeOptions(LocalDateTime now) {
        ArrayList<Map<String, Object>> optionAsMaps = new ArrayList<>();

        for (SnoozeOption option: getSnoozeOptionsForDateTime(now)) {
            optionAsMaps.add(option.asMap());
        }
//        optionAsMaps.add(new HashMap<String, Object>() {{
//            put(SNOOZE_OPTION_TITLE, "Tomorrow Morning");
//            put(SNOOZE_OPTION_DATETIME, NextMorning.adjustInto(LocalDateTime.now()));
//            put(SNOOZE_OPTION_ICON, R.drawable.ic_morning_24dp);
//        }});
//        optionAsMaps.add(new HashMap<String, Object>() {{
//            put(SNOOZE_OPTION_TITLE, "This Afternoon");
//            put(SNOOZE_OPTION_DATETIME, NextAfternoon.adjustInto(LocalDateTime.now()));
//            put(SNOOZE_OPTION_ICON, R.drawable.ic_restaurant_black_24dp);
//        }});
//        optionAsMaps.add(new HashMap<String, Object>() {{
//            put(SNOOZE_OPTION_TITLE, "This Evening");
//            put(SNOOZE_OPTION_DATETIME, NextEvening.adjustInto(LocalDateTime.now()));
//            put(SNOOZE_OPTION_ICON, R.drawable.ic_hot_tub_black_24dp);
//        }});
//        optionAsMaps.add(new HashMap<String, Object>() {{
//            put(SNOOZE_OPTION_TITLE, "Next Week");
//            put(SNOOZE_OPTION_DATETIME, NextWeekNotTomorrowMorningAdjuster.adjustInto(LocalDateTime.now()));
//            put(SNOOZE_OPTION_ICON, R.drawable.ic_next_week_black_24dp);
//        }});
//        optionAsMaps.add(new HashMap<String, Object>() {{
//            put(SNOOZE_OPTION_TITLE, "This Weekend");
//            put(SNOOZE_OPTION_DATETIME, WeekendNotTomorrowMorningAdjuster.adjustInto(LocalDateTime.now()));
//            put(SNOOZE_OPTION_ICON, R.drawable.ic_weekend_black_24dp);
//        }});
//        optionAsMaps.add(new HashMap<String, Object>() {{
//            put(SNOOZE_OPTION_TITLE, "In 30 seconds");
//            put(SNOOZE_OPTION_DATETIME, LocalDateTime.now().plusSeconds(30));
//            put(SNOOZE_OPTION_ICON, R.drawable.ic_schedule_black_24dp);
//        }});
        return optionAsMaps;
    }

    private SimpleAdapter getAdapter(@NonNull Context context) {
        synchronized (this) {
            if (mAdapter == null) {
                mAdapter = new SimpleAdapter(
                        context,
                        snoozeOptions,
                        R.layout.snooze_option_item,
                        new String[]{SNOOZE_OPTION_TITLE, SNOOZE_OPTION_DATETIME, SNOOZE_OPTION_ICON},
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
        return (LocalDateTime) item.get(SNOOZE_OPTION_DATETIME);
    }

    static class SnoozeOption {
        static final DateTimeFormatter sToStringFormatter = DateTimeFormatter.ofPattern("E dd-M-yyyy hh:mm");

        final LocalDateTime dateTime;
        final CharSequence label;
        final @DrawableRes int drawableId;

        SnoozeOption(@NonNull LocalDateTime dateTime, @NonNull CharSequence label, @DrawableRes int drawableId) {
            this.dateTime = dateTime;
            this.label = label;
            this.drawableId = drawableId;
        }

        static SnoozeOption of(@NonNull LocalDateTime now, @NonNull TemporalAdjuster adjuster, @DrawableRes int drawableId) {
            final LocalDateTime target = now.with(adjuster);
            return new SnoozeOption(target, getLabelForAdjuster(now, adjuster), drawableId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SnoozeOption that = (SnoozeOption) o;
            return drawableId == that.drawableId &&
                    dateTime.equals(that.dateTime) &&
                    label.equals(that.label);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dateTime, label, drawableId);
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return "SnoozeOption{" +
                    "dateTime=" + sToStringFormatter.format(dateTime) +
                    ", label=" + label +
                    ", drawableId=" + drawableId +
                    '}';
        }

        LocalDateTime getDateTime() {
            return dateTime;
        }

        Map<String, Object> asMap() {
            Map<String, Object> result = new HashMap<>();
            result.put(SNOOZE_OPTION_TITLE, label);
            result.put(SNOOZE_OPTION_DATETIME, dateTime);
            result.put(SNOOZE_OPTION_ICON, drawableId);
            return Collections.unmodifiableMap(result);
        }

    }

    static List<SnoozeOption> getSnoozeOptionsForDateTime(LocalDateTime dateTime) {
        ArrayList<SnoozeOption> results = new ArrayList<>();
        results.add(SnoozeOption.of(dateTime, NextMorning, R.drawable.ic_morning_24dp));
        results.add(SnoozeOption.of(dateTime, NextAfternoon, R.drawable.ic_restaurant_black_24dp));
        results.add(SnoozeOption.of(dateTime, NextEvening, R.drawable.ic_hot_tub_black_24dp));
        results.add(SnoozeOption.of(dateTime, WeekendNotTomorrowMorningAdjuster, R.drawable.ic_weekend_black_24dp));
        results.add(SnoozeOption.of(dateTime, NextWeekNotTomorrowMorningAdjuster, R.drawable.ic_next_week_black_24dp));
        Comparator<SnoozeOption> comparator = Comparator.comparing(SnoozeOption::getDateTime);
        Collections.sort(results, comparator);
        return results;
    }

    private static boolean isWeekday(LocalDateTime date) {
        return date.getDayOfWeek().getValue() <= FRIDAY.getValue();
    }

    private static CharSequence getLabelForAdjuster(@NonNull LocalDateTime now, @NonNull TemporalAdjuster adjuster) {
        final LocalDateTime target = now.with(adjuster);
        StringBuilder sb = new StringBuilder();

        if (adjuster == WeekendAdjuster || adjuster == WeekendNotTomorrowMorningAdjuster) {
            sb.append(getLabelForWeekend(now, target));
        }
        if (adjuster == StartOfWeekAdjuster || adjuster == NextWeekNotTomorrowMorningAdjuster) {
            sb.append(getLabelForStartOfWeek());
        }
        if (sb.length() == 0) {
            sb.append(getLabelForOptionDateTime(now, target));
        }
        return sb.toString();
    }

    private static CharSequence getLabelForWeekend(@NonNull LocalDateTime now, @NonNull LocalDateTime target) {
        // For weekdays the coming weekend is always 'this weekend', any weekend after is 'next weekend'
        // If we're before 9am on Saturday, it's also 'this weekend'
        // otherwise, it's 'next weekend'
        if ((isWeekday(now) && WEEKS.between(now, target) < 1) ||
                (now.toLocalDate().equals(target.toLocalDate()) && now.isBefore(target))) {
            return "This Weekend";
        } else {
            return "Next Weekend";
        }
    }

    private static CharSequence getLabelForStartOfWeek() {
        // For the start of the week, it's almost always 'Next Week'
        return "Next Week";
    }


    private static CharSequence getLabelForOptionDateTime(LocalDateTime now, LocalDateTime target) {
        StringBuilder sb = new StringBuilder();

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
