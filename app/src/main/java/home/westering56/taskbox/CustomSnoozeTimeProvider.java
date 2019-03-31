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
 * <p>
 * This class expects to be re-created by an activity or fragment when configurations change. It
 * therefore persists no state in memory that can't be recreated.
 */
public class CustomSnoozeTimeProvider {

    private final List<Map<String, Object>> mSnoozeTimes;
    private final SimpleAdapter mAdapter;

    private HashMap<String, Object> mCustom;

    private static final String SNOOZE_TIME_NAME = "time_name";
    private static final String SNOOZE_TIME_ADJUSTER = "time_adjuster";
    private static final String SNOOZE_TIME_EXAMPLE = "time_example";

    public CustomSnoozeTimeProvider(@NonNull Context context) {
        mSnoozeTimes = initSnoozeTimes();
        mAdapter = new SimpleAdapter(context.getApplicationContext(),
                mSnoozeTimes, R.layout.snooze_time_spinner_dropdown_item,
                new String[]{SNOOZE_TIME_NAME, SNOOZE_TIME_EXAMPLE},
                new int[]{R.id.snooze_time_spinner_item_title, R.id.snooze_time_spinner_item_details});
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
        mCustom = new HashMap<String, Object>() {{
            put(SNOOZE_TIME_NAME, "Custom");
            put(SNOOZE_TIME_ADJUSTER, CustomAdjuster); // sentinel no-op value for custom adjuster
            put(SNOOZE_TIME_EXAMPLE, "");
        }};
        times.add(mCustom);
        return times;
    }

    public SpinnerAdapter getAdapter() {
        return mAdapter;
    }

    public static LocalTime getLocalTimeAtPosition(AdapterView<?> parent, int position) {
        //noinspection unchecked
        Map<String, Object> item = (Map<String, Object>) parent.getItemAtPosition(position);
        TemporalAdjuster adjuster = (TemporalAdjuster) item.get(SNOOZE_TIME_ADJUSTER);
        return LocalTime.now().with(adjuster);
    }

    /**
     * Return true if the specified position represents a 'custom' time, to be further set by
     * the user.
     */
    public static boolean isCustomPosition(AdapterView<?> parent, int position) {
        //noinspection unchecked
        Map<String, Object> item = (Map<String, Object>) parent.getItemAtPosition(position);
        TemporalAdjuster adjuster = (TemporalAdjuster) item.get(SNOOZE_TIME_ADJUSTER);
        // TemporalAdjuster doesn't override equals(), so testing for instance equivalence will do
        return adjuster == CustomAdjuster;
    }

    public void updateCustomTimeExample(@NonNull LocalTime lastCustomTime) {
        mCustom.put(SNOOZE_TIME_EXAMPLE, SnoozeTimeFormatter.formatTime(lastCustomTime));
        mAdapter.notifyDataSetChanged();
    }

    public void clearCustomTimeExample() {
        mCustom.put(SNOOZE_TIME_EXAMPLE, "");
        mAdapter.notifyDataSetChanged();
    }

    public int getCustomPosition() {
        return mSnoozeTimes.indexOf(mCustom);
    }

    public int getPositionForTime(LocalTime time) {
        for (int i = 0; i < mSnoozeTimes.size(); i++) {
            TemporalAdjuster adjuster = (TemporalAdjuster) mSnoozeTimes.get(i).get(SNOOZE_TIME_ADJUSTER);
            // This works for custom too, as CustomAdjuster performs no adjustment!
            // Only works if custom is the last item in the list, though.
            if (time.with(adjuster).equals(time)) {
                return i;
            }
        }
        // We should not have arrived here - custom entry should have been found and returned above.
        return getCustomPosition();
    }
}
