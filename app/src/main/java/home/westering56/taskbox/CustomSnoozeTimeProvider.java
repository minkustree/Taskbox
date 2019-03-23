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
    private HashMap<String, Object> mCustom;
    private SimpleAdapter mAdapter;

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
        mCustom = new HashMap<String, Object>() {{
            put(SNOOZE_TIME_NAME, "Custom");
            put(SNOOZE_TIME_ADJUSTER, CustomAdjuster); // sentinel no-op value for custom adjuster
            put(SNOOZE_TIME_EXAMPLE, "");
        }};
        times.add(mCustom);
        return times;
    }

    public SpinnerAdapter getDefaultAdapter(@NonNull Context context) {
        synchronized (this) {
            if (mAdapter == null) {
                mAdapter = new SimpleAdapter(context.getApplicationContext(),
                        mSnoozeTimes,
                        R.layout.snooze_time_spinner_dropdown_item,
                        new String[]{SNOOZE_TIME_NAME, SNOOZE_TIME_EXAMPLE},
                        new int[]{
                                R.id.snooze_time_spinner_item_title,
                                R.id.snooze_time_spinner_item_details});
            }
        }
        return mAdapter;
    }

    public static LocalTime getLocalTimeAtPosition(AdapterView<?> parent, int position) {
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
}
