package home.westering56.taskbox;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.time.LocalTime;
import java.time.temporal.TemporalAdjuster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import androidx.annotation.NonNull;
import home.westering56.taskbox.widget.CustomSpinnerAdapter;

import static home.westering56.taskbox.Adjusters.AfternoonAdjuster;
import static home.westering56.taskbox.Adjusters.EveningAdjuster;
import static home.westering56.taskbox.Adjusters.MorningAdjuster;

/**
 * Provides relevant snooze option choices.
 * <p>
 * This class expects to be re-created by an activity or fragment when configurations change. It
 * therefore persists no state in memory that can't be recreated.
 */
public class CustomSnoozeOptionProvider {

    private final SimpleAdapter mAdapter;

//    private HashMap<String, Object> mCustom;

    private static final String SNOOZE_TIME_NAME = "time_name";
    private static final String SNOOZE_TIME_ADJUSTER = "time_adjuster";
    private static final String SNOOZE_TIME_EXAMPLE = "time_example";

    private CustomSnoozeOptionProvider(@NonNull Context context) {
        List<Map<String, Object>> snoozeTimes = initSnoozeTimes(context);
        mAdapter = new SimpleAdapter(context.getApplicationContext(),
                snoozeTimes, R.layout.snooze_time_spinner_item,
                new String[]{SNOOZE_TIME_NAME, SNOOZE_TIME_EXAMPLE},
                new int[]{R.id.snooze_time_spinner_item_title, R.id.snooze_time_spinner_item_details});
        mAdapter.setDropDownViewResource(R.layout.snooze_time_spinner_dropdown_item);
    }

    public static CustomSpinnerAdapter buildAdapter(@NonNull Context context) {
        CustomSnoozeOptionProvider provider = new CustomSnoozeOptionProvider(context);

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(context, provider.getAdapter());
        adapter.setViewBinder(new CustomSpinnerAdapter.ViewBinder() {
            @Override
            public boolean bindCustomValueView(@NonNull View v, Object customValue) {
                final TextView title = v.findViewById(R.id.snooze_time_spinner_item_title);
                final TextView details = v.findViewById(R.id.snooze_time_spinner_item_details);
                title.setText(R.string.snooze_time_picker_custom_value);
                details.setText(customValue.toString());
                return true;
            }

            @Override
            public boolean bindPickerView(@NonNull View v, Object customValue) {
                final TextView title = v.findViewById(R.id.snooze_time_spinner_item_title);
                final TextView details = v.findViewById(R.id.snooze_time_spinner_item_details);
                title.setText(customValue.toString());
                details.setText("");
                return true;
            }
        });
        return adapter;
    }

    private ArrayList<Map<String, Object>> initSnoozeTimes(@NonNull final Context context) {
        ArrayList<Map<String, Object>> times = new ArrayList<>();
        final LocalTime now = LocalTime.now();
        times.add(new HashMap<String, Object>() {{
            put(SNOOZE_TIME_NAME, context.getString(R.string.snooze_time_picker_morning));
            put(SNOOZE_TIME_ADJUSTER, MorningAdjuster);
            put(SNOOZE_TIME_EXAMPLE, SnoozeTimeFormatter.formatTime(now.with(MorningAdjuster)));
        }});
        times.add(new HashMap<String, Object>() {{
            put(SNOOZE_TIME_NAME, context.getString(R.string.snooze_time_picker_afternoon));
            put(SNOOZE_TIME_ADJUSTER, AfternoonAdjuster);
            put(SNOOZE_TIME_EXAMPLE, SnoozeTimeFormatter.formatTime(now.with(AfternoonAdjuster)));
        }});
        times.add(new HashMap<String, Object>() {{
            put(SNOOZE_TIME_NAME, context.getString(R.string.snooze_time_picker_evening));
            put(SNOOZE_TIME_ADJUSTER, EveningAdjuster);
            put(SNOOZE_TIME_EXAMPLE, SnoozeTimeFormatter.formatTime(now.with(EveningAdjuster)));
        }});
//        mCustom = new HashMap<String, Object>() {{
//            put(SNOOZE_TIME_NAME, "Custom");
//            put(SNOOZE_TIME_ADJUSTER, CustomAdjuster); // sentinel no-op value for custom adjuster
//            put(SNOOZE_TIME_EXAMPLE, "");
//        }};
//        times.add(mCustom);
        return times;
    }

    // TODO: Move this into the adapter so we can use vanilla positionOf

    public static BiPredicate<LocalTime, Object> isTimeEqualToItem() {
        return new BiPredicate<LocalTime, Object>() {
            @Override
            public boolean test(LocalTime localTime, Object adapterItem) {
                try {
                    return getTimeAdjustedByItem(localTime, adapterItem).equals(localTime);
                } catch (ClassCastException e) {
                    Log.e("CustomSnoozeOptionProvider", "Unexpected class passed to isTimeEqualToItem", e);
                    return false;
                }
            }
        };
    }

    private static LocalTime getTimeAdjustedByItem(@NonNull LocalTime time, @NonNull Object item)  {
        //noinspection unchecked - class cast exception should be propagated
        final Map<String, Object> itemAsMap = (Map<String, Object>) item;
        final TemporalAdjuster adjuster = (TemporalAdjuster) itemAsMap.get(SNOOZE_TIME_ADJUSTER);
        return time.with(adjuster);
    }

    public static LocalTime getLocalTimeAtPosition(AdapterView<?> parent, int position) {
        return getTimeAdjustedByItem(LocalTime.now(), parent.getItemAtPosition(position));
    }

    private SpinnerAdapter getAdapter() {
        return mAdapter;
    }




}
