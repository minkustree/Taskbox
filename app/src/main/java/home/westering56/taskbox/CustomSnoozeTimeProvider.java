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
import static home.westering56.taskbox.Adjusters.EveningAdjuster;
import static home.westering56.taskbox.Adjusters.MorningAdjuster;

/**
 * Provides relevant snooze option choices.
 */
public class CustomSnoozeTimeProvider {
    private static CustomSnoozeTimeProvider sProvider;

    private final List<Map<String, Object>> snoozeTimes;

    private static final String SNOOZE_TIME_NAME = "time_name";
    private static final String SNOOZE_TIME_ADJUSTER = "time_adjuster";

    public static CustomSnoozeTimeProvider getInstance() {
        synchronized (CustomSnoozeTimeProvider.class) {
            if (sProvider == null) {
                sProvider = new CustomSnoozeTimeProvider();
            }
        }
        return sProvider;
    }
    private CustomSnoozeTimeProvider() {
        snoozeTimes = initSnoozeTimes();
    }

    private ArrayList<Map<String, Object>> initSnoozeTimes() {
        ArrayList<Map<String, Object>> times = new ArrayList<>();
        times.add(new HashMap<String, Object>() {{
            put(SNOOZE_TIME_NAME, "Morning");
            put(SNOOZE_TIME_ADJUSTER, MorningAdjuster);
        }});
        times.add(new HashMap<String, Object>() {{
            put(SNOOZE_TIME_NAME, "Afternoon");
            put(SNOOZE_TIME_ADJUSTER, AfternoonAdjuster);
        }});
        times.add(new HashMap<String, Object>() {{
            put(SNOOZE_TIME_NAME, "Evening");
            put(SNOOZE_TIME_ADJUSTER, EveningAdjuster);
        }});
//        times.add(new HashMap<String, Object>() {{
//            put(SNOOZE_TIME_NAME, "Custom");
//            put(SNOOZE_TIME_ADJUSTER, null);
//        }});
        return times;
    }

    public SpinnerAdapter newAdapter(@NonNull Context context) {
        return new SimpleAdapter(context,
                snoozeTimes,
                android.R.layout.simple_spinner_dropdown_item,
                new String[] { SNOOZE_TIME_NAME },
                new int[] { android.R.id.text1 });
    }

    public static LocalTime getLocalTimeAtPosition(AdapterView<?> parent, int position) {
        // TODO: If this is a 'custom' entry, pop up a time picker and return result somehow
        //noinspection unchecked
        Map<String, Object> item = (Map<String, Object>)parent.getItemAtPosition(position);
        TemporalAdjuster adjuster = (TemporalAdjuster)item.get(SNOOZE_TIME_ADJUSTER);
        return LocalTime.now().with(adjuster);
    }
}
