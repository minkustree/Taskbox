package home.westering56.taskbox;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

@ContentView(R.layout.snooze_dialog)
public class SnoozeDialogFragment extends DialogFragment {
    private static final String TAG = "SnoozeDialog";


    public interface SnoozeOptionListener {
        void onSnoozeOptionSelected(String title, LocalDateTime snoozeUntil);
    }

    private final List<Map<String, Object>> snoozeData;
    private static final String SNOOZE_OPTION_TITLE = "title";
    private static final String SNOOZE_OPTION_INSTANT = "instant";

    private static final TemporalAdjuster WeekendAdjuster = new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
            return temporal.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).with(MorningAdjuster);
        }
    };
    private static final TemporalAdjuster StartOfWeekAdjuster = new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
            return temporal.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).with(MorningAdjuster);
        }
    };
    private static final TemporalAdjuster TopOfTheHourAdjuster = new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
            return temporal
                    .with(ChronoField.MINUTE_OF_HOUR, 0)
                    .with(ChronoField.SECOND_OF_MINUTE, 0)
                    .with(ChronoField.NANO_OF_SECOND, 0);
        }
    };
    private static final TemporalAdjuster EveningAdjuster = new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
            return temporal.with(ChronoField.HOUR_OF_DAY, 18).with(TopOfTheHourAdjuster);
        }
    };
    private static final TemporalAdjuster MorningAdjuster = new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
            return temporal.with(ChronoField.HOUR_OF_DAY, 9).with(TopOfTheHourAdjuster);
        }
    };
    private static final TemporalAdjuster TomorrowMorningAdjuster = new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
            return temporal.plus(1, ChronoUnit.DAYS).with(MorningAdjuster);
        }
    };
    private static final TemporalAdjuster AfternoonAdjuster = new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
            return temporal.with(ChronoField.HOUR_OF_DAY, 13).with(TopOfTheHourAdjuster);
        }
    };
    private static final TemporalAdjuster OneMinuteAdjuster = new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
            return temporal.plus(1, ChronoUnit.MINUTES);
        }
    };

    private SnoozeOptionListener mSnoozeOptionListener;



    public SnoozeDialogFragment() {
        snoozeData = new ArrayList<>();

        snoozeData.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "Tomorrow Morning");
            put(SNOOZE_OPTION_INSTANT, TomorrowMorningAdjuster.adjustInto(LocalDateTime.now()));
        }});
        snoozeData.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "This Afternoon");
            put(SNOOZE_OPTION_INSTANT, AfternoonAdjuster.adjustInto(LocalDateTime.now()));
        }});
        snoozeData.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "This Evening");
            put(SNOOZE_OPTION_INSTANT, EveningAdjuster.adjustInto(LocalDateTime.now()));
        }});
        snoozeData.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "Next Week");
            put(SNOOZE_OPTION_INSTANT, StartOfWeekAdjuster.adjustInto(LocalDateTime.now()));
        }});
        snoozeData.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "This Weekend");
            put(SNOOZE_OPTION_INSTANT, WeekendAdjuster.adjustInto(LocalDateTime.now()));
        }});
        snoozeData.add(new HashMap<String, Object>() {{
            put(SNOOZE_OPTION_TITLE, "In a minute");
            put(SNOOZE_OPTION_INSTANT, OneMinuteAdjuster.adjustInto(LocalDateTime.now()));
        }});
    }

    @SuppressWarnings("WeakerAccess")
    public void setSnoozeOptionListener(SnoozeOptionListener listener) {
        mSnoozeOptionListener = listener;
    }

    public static SnoozeDialogFragment newInstance(SnoozeOptionListener snoozeOptionListener) {
        SnoozeDialogFragment fragment = new SnoozeDialogFragment();
        fragment.setSnoozeOptionListener(snoozeOptionListener);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");

        final SimpleAdapter adapter = new SimpleAdapter(
                requireContext(),
                snoozeData,
                R.layout.snooze_option_item,
                new String[] {SNOOZE_OPTION_TITLE, SNOOZE_OPTION_INSTANT},
                new int[] {R.id.snooze_option_item_title, R.id.snooze_option_item_detail});

        final GridView content = view.findViewById(R.id.snooze_dialog_content);
        content.setAdapter(adapter);
        content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //noinspection unchecked
                @SuppressWarnings("unchecked")
                Map<String, Object> item = (Map<String, Object>)adapter.getItem(position);
                if (mSnoozeOptionListener != null) {
                    mSnoozeOptionListener.onSnoozeOptionSelected(
                            (String) item.get(SNOOZE_OPTION_TITLE),
                            (LocalDateTime) item.get(SNOOZE_OPTION_INSTANT));
                }
            }
        });
    }
}
