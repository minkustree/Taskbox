package home.westering56.taskbox;

import android.content.Context;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.time.LocalTime;
import java.time.temporal.TemporalAdjuster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import home.westering56.taskbox.formatter.SnoozeTimeFormatter;
import home.westering56.taskbox.widget.CustomSpinnerAdapter;

import static home.westering56.taskbox.Adjusters.AfternoonAdjuster;
import static home.westering56.taskbox.Adjusters.EveningAdjuster;
import static home.westering56.taskbox.Adjusters.MorningAdjuster;

/**
 * Provides relevant snooze time option choices (e.g. morning, afternoon, eve, custom, etc.)
 * <p>
 * This class expects to be re-created by an activity or fragment when configurations change. It
 * therefore persists no state in memory that can't be recreated.
 */
public class SnoozeTimeAdapterFactory {

    private final SimpleAdapter mAdapter;

    private static final String SNOOZE_TIME_NAME = "time_name";
    private static final String SNOOZE_TIME_ADJUSTER = "time_adjuster";
    private static final String SNOOZE_TIME_EXAMPLE = "time_example";
    private static final String SNOOZE_TIME_REFERENCE = "time_reference";

    private SnoozeTimeAdapterFactory(@NonNull Context context) {
        List<Map<String, Object>> snoozeTimes = initSnoozeTimes(context);
        mAdapter = new SimpleAdapter(context.getApplicationContext(),
                snoozeTimes, R.layout.snooze_time_spinner_item,
                new String[]{SNOOZE_TIME_NAME, SNOOZE_TIME_EXAMPLE},
                new int[]{R.id.snooze_time_spinner_item_title, R.id.snooze_time_spinner_item_details});
        mAdapter.setDropDownViewResource(R.layout.snooze_time_spinner_dropdown_item);
    }

    public static CustomSpinnerAdapter<LocalTime> buildAdapter(@NonNull Context context) {
        SnoozeTimeAdapterFactory provider = new SnoozeTimeAdapterFactory(context);

        CustomSpinnerAdapter<LocalTime> adapter = new CustomSpinnerAdapter<>(provider.getAdapter());
        adapter.setViewBinder(new CustomSpinnerAdapter.ViewBinder<LocalTime>() {
            @Override
            public boolean bindCustomValueView(@NonNull View v, LocalTime customValue) {
                final TextView title = v.findViewById(R.id.snooze_time_spinner_item_title);
                final TextView details = v.findViewById(R.id.snooze_time_spinner_item_details);
                title.setText(R.string.snooze_time_picker_custom_value);
                details.setText(SnoozeTimeFormatter.formatTime(customValue));
                return true;
            }

            @Override
            public boolean bindPickerView(@NonNull View v, String customPickerValue) {
                final TextView title = v.findViewById(R.id.snooze_time_spinner_item_title);
                final TextView details = v.findViewById(R.id.snooze_time_spinner_item_details);
                title.setText(customPickerValue);
                details.setText("");
                return true;
            }
        });
        // extracts the reference time for comparison purposes
        adapter.setGetValueFromItemFn(input -> {
            //noinspection unchecked cast
            final Map<String, Object> itemAsMap = (Map<String, Object>) input;
            return (LocalTime) itemAsMap.get(SNOOZE_TIME_REFERENCE);
        });
        return adapter;
    }

    private ArrayList<Map<String, Object>> initSnoozeTimes(@NonNull final Context context) {
        ArrayList<Map<String, Object>> times = new ArrayList<>();
        final LocalTime now = LocalTime.now();
        times.add(new HashMap<String, Object>() {{
            put(SNOOZE_TIME_NAME, context.getString(R.string.snooze_time_picker_morning));
            put(SNOOZE_TIME_ADJUSTER, MorningAdjuster);
        }});
        times.add(new HashMap<String, Object>() {{
            put(SNOOZE_TIME_NAME, context.getString(R.string.snooze_time_picker_afternoon));
            put(SNOOZE_TIME_ADJUSTER, AfternoonAdjuster);
        }});
        times.add(new HashMap<String, Object>() {{
            put(SNOOZE_TIME_NAME, context.getString(R.string.snooze_time_picker_evening));
            put(SNOOZE_TIME_ADJUSTER, EveningAdjuster);
        }});

        for (Map<String, Object> time : times) {
            LocalTime ref = now.with((TemporalAdjuster) time.get(SNOOZE_TIME_ADJUSTER));
            time.put(SNOOZE_TIME_REFERENCE, ref);
            time.put(SNOOZE_TIME_EXAMPLE, SnoozeTimeFormatter.formatTime(ref));
        }

        return times;
    }


    private SpinnerAdapter getAdapter() {
        return mAdapter;
    }




}
