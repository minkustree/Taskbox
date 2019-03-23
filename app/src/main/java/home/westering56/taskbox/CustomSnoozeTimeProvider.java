package home.westering56.taskbox;

import android.content.Context;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.SpinnerAdapter;

import java.time.LocalTime;
import java.time.temporal.TemporalAdjuster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import static home.westering56.taskbox.Adjusters.AfternoonAdjuster;
import static home.westering56.taskbox.Adjusters.CustomAdjuster;
import static home.westering56.taskbox.Adjusters.EveningAdjuster;
import static home.westering56.taskbox.Adjusters.MorningAdjuster;

/**
 * Provides relevant snooze option choices.
 */
public class CustomSnoozeTimeProvider {
    private static CustomSnoozeTimeProvider sProvider;

    private final List<Map<String, Object>> mSnoozeTimes;

    private static final String SNOOZE_TIME_NAME = "time_name";
    private static final String SNOOZE_TIME_ADJUSTER = "time_adjuster";
    private static final String SNOOZE_TIME_EXAMPLE = "time_example";

    public static CustomSnoozeTimeProvider getInstance() {
        synchronized (CustomSnoozeTimeProvider.class) {
            if (sProvider == null) {
                sProvider = new CustomSnoozeTimeProvider();
            }
        }
        return sProvider;
    }
    private CustomSnoozeTimeProvider() {
        mSnoozeTimes = initSnoozeTimes();
    }

    private ArrayList<Map<String, Object>> initSnoozeTimes() {
        ArrayList<Map<String, Object>> times = new ArrayList<>();
        final LocalTime now = LocalTime.now();
        times.add(new HashMap<String, Object>() {{
            put(SNOOZE_TIME_NAME, "Morning");
            put(SNOOZE_TIME_ADJUSTER, MorningAdjuster);
            put(SNOOZE_TIME_EXAMPLE, SnoozeTimeFormatter.formatTime(now.with(MorningAdjuster)));
        }});
        times.add(new HashMap<String, Object>() {{
            put(SNOOZE_TIME_NAME, "Afternoon");
            put(SNOOZE_TIME_ADJUSTER, AfternoonAdjuster);
            put(SNOOZE_TIME_EXAMPLE, SnoozeTimeFormatter.formatTime(now.with(AfternoonAdjuster)));
        }});
        times.add(new HashMap<String, Object>() {{
            put(SNOOZE_TIME_NAME, "Evening");
            put(SNOOZE_TIME_ADJUSTER, EveningAdjuster);
            put(SNOOZE_TIME_EXAMPLE, SnoozeTimeFormatter.formatTime(now.with(EveningAdjuster)));
        }});
        times.add(new HashMap<String, Object>() {{
            put(SNOOZE_TIME_NAME, "Custom");
            put(SNOOZE_TIME_ADJUSTER, CustomAdjuster); // sentinel no-op value for custom adjuster
            put(SNOOZE_TIME_EXAMPLE, "");
        }});
        return times;
    }

    public SpinnerAdapter newAdapter(@NonNull Context context) {
        return new SimpleAdapter(context,
                mSnoozeTimes,
                R.layout.snooze_time_spinner_dropdown_item,
                new String[]{SNOOZE_TIME_NAME, SNOOZE_TIME_EXAMPLE},
                new int[]{R.id.snooze_time_spinner_item_title, R.id.snooze_time_spinner_item_details});
    }

    public static LocalTime getLocalTimeAtPosition(AdapterView<?> parent, int position) {
        // TODO: If this is a 'custom' entry, pop up a time picker and return result somehow
        //noinspection unchecked
        Map<String, Object> item = (Map<String, Object>)parent.getItemAtPosition(position);
        TemporalAdjuster adjuster = (TemporalAdjuster)item.get(SNOOZE_TIME_ADJUSTER);
        return LocalTime.now().with(adjuster);
    }

    /**
     * Return true if the specified position represents a 'custom' time, to be further set by
     * the user.
     */
    public static boolean isCustomPosition(AdapterView<?> parent, int position) {
        //noinspection unchecked
        Map<String, Object> item = (Map<String, Object>)parent.getItemAtPosition(position);
        TemporalAdjuster adjuster = (TemporalAdjuster)item.get(SNOOZE_TIME_ADJUSTER);
        // TemporalAdjuster doesn't override equals(), so testing for instance equivalence will do
        return adjuster == CustomAdjuster;
    }
}
