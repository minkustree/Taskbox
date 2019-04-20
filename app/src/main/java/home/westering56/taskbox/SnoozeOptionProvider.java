package home.westering56.taskbox;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import home.westering56.taskbox.formatter.SnoozeTimeFormatter;

import static home.westering56.taskbox.Adjusters.AfternoonAdjuster;
import static home.westering56.taskbox.Adjusters.EveningAdjuster;
import static home.westering56.taskbox.Adjusters.StartOfWeekAdjuster;
import static home.westering56.taskbox.Adjusters.TomorrowMorningAdjuster;
import static home.westering56.taskbox.Adjusters.WeekendAdjuster;

/**
 * Provides relevant snooze option choices.
 */
@SuppressWarnings("WeakerAccess")
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
                mAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                    @Override
                    public boolean setViewValue(View view, Object data, String textRepresentation) {
                        if (data instanceof Temporal) {
                            ((TextView)view).setText(
                                    SnoozeTimeFormatter.format(view.getContext(), (Temporal) data));
                            return true;
                        }
                        return false;
                    }
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

}
