package home.westering56.taskbox;

import android.content.Context;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import static home.westering56.taskbox.Adjusters.AfternoonAdjuster;
import static home.westering56.taskbox.Adjusters.EveningAdjuster;
import static home.westering56.taskbox.Adjusters.StartOfWeekAdjuster;
import static home.westering56.taskbox.Adjusters.TomorrowMorningAdjuster;
import static home.westering56.taskbox.Adjusters.WeekendAdjuster;

/**
 * Provides relevant snooze option choices.
 */
public class SnoozeOptionProvider {
    private static SnoozeOptionProvider sProvider;

    private final List<Map<String, Object>> snoozeData;

    private static final String SNOOZE_OPTION_TITLE = "title";
    private static final String SNOOZE_OPTION_INSTANT = "instant";

    public static SnoozeOptionProvider getInstance() {
        synchronized (SnoozeOptionProvider.class) {
            if (sProvider == null) {
                sProvider = new SnoozeOptionProvider();
            }
        }
        return sProvider;
    }
    private SnoozeOptionProvider() {
        snoozeData = initSnoozeOptions();
    }

    private ArrayList<Map<String, Object>> initSnoozeOptions() {
        ArrayList<Map<String, Object>> options = new ArrayList<>();
        options.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "Tomorrow Morning");
            put(SNOOZE_OPTION_INSTANT, TomorrowMorningAdjuster.adjustInto(LocalDateTime.now()));
        }});
        options.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "This Afternoon");
            put(SNOOZE_OPTION_INSTANT, AfternoonAdjuster.adjustInto(LocalDateTime.now()));
        }});
        options.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "This Evening");
            put(SNOOZE_OPTION_INSTANT, EveningAdjuster.adjustInto(LocalDateTime.now()));
        }});
        options.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "Next Week");
            put(SNOOZE_OPTION_INSTANT, StartOfWeekAdjuster.adjustInto(LocalDateTime.now()));
        }});
        options.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "This Weekend");
            put(SNOOZE_OPTION_INSTANT, WeekendAdjuster.adjustInto(LocalDateTime.now()));
        }});
        options.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "In a minute");
            put(SNOOZE_OPTION_INSTANT, LocalDateTime.now().plusMinutes(1));
        }});
        options.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "In 30 seconds");
            put(SNOOZE_OPTION_INSTANT, LocalDateTime.now().plusSeconds(30));
        }});
        return options;
    }

    public SimpleAdapter newAdapter(@NonNull Context context) {
        final SimpleAdapter adapter = new SimpleAdapter(
                context,
                snoozeData,
                R.layout.snooze_option_item,
                new String[] {SNOOZE_OPTION_TITLE, SNOOZE_OPTION_INSTANT},
                new int[] {R.id.snooze_option_item_title, R.id.snooze_option_item_detail});
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (data instanceof Temporal) {
                    adapter.setViewText((TextView)view, SnoozeTimeFormatter.format(view.getContext(), (Temporal)data).toString());
                    return true;
                }
                return false;
            }
        });
        return adapter;
    }

    public LocalDateTime getOptionDateTime(@NonNull SimpleAdapter adapter, int position) {
        //noinspection unchecked
        Map<String, Object> item = (Map<String, Object>)adapter.getItem(position);
        return (LocalDateTime) item.get(SNOOZE_OPTION_INSTANT);

    }
}
