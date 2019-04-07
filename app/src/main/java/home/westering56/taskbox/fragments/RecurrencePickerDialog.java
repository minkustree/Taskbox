package home.westering56.taskbox.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

import org.dmfs.rfc5545.Weekday;
import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import home.westering56.taskbox.R;

public class RecurrencePickerDialog extends DialogFragment implements AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "RecurrencePickerDialog";

    public static void test(OnRecurrencePickListener listener, FragmentManager fragmentManager) {
        RecurrencePickerDialog dlg = RecurrencePickerDialog.newInstance(listener, null);
        dlg.show(fragmentManager, "recurrence_picker");
    }

    public interface OnRecurrencePickListener {
        void onRecurrencePicked(@NonNull RecurrenceRule rule);
    }

    /*
     * Data Model for this dialog
     */
    static class RecurrencePickerViewModel extends ViewModel {
        static class Factory implements ViewModelProvider.Factory {
            private final RecurrenceRule factoryRule;

            Factory(RecurrenceRule rule) {
                factoryRule = rule;
            }

            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (!modelClass.isAssignableFrom(RecurrencePickerViewModel.class)) {
                    throw new IllegalArgumentException("Unexpected model class " + modelClass);
                }
                //noinspection unchecked
                return (T) new RecurrencePickerViewModel(factoryRule);
            }
        }

        private final RecurrenceRule mRule;

        RecurrencePickerViewModel(RecurrenceRule rule) {
            this.mRule = rule;
        }

        RecurrenceRule getRule() {
            return mRule;
        }
    }

    private RecurrencePickerViewModel mModel;
    private OnRecurrencePickListener mListener;

    private Spinner mFrequencySpinner;
    private EditText mIntervalValue;
    private View mWeeklyView;
    private View mMonthlyView;
    private ToggleButton[] mWeekdayButtons;

    /**
     * Create a new instance of the fragment, displaying the specified {@link RecurrenceRule}.
     * If there is no <tt>rule</tt> to display, pass null for <tt>rule</tt> and a default daily rule
     * will be shown.
     * Uses the supplied listener when returning the result.
     */
    @NonNull
    public static RecurrencePickerDialog newInstance(@NonNull OnRecurrencePickListener listener,
                                                     @Nullable RecurrenceRule rule) {
        Bundle args = new Bundle();
        args.putString("EXTRA_RRULE", rule == null ? null : rule.toString());
        RecurrencePickerDialog fragment = new RecurrencePickerDialog();
        fragment.setArguments(args);
        fragment.setOnRecurrencePickListener(listener);
        return fragment;
    }

    private void setOnRecurrencePickListener(@Nullable OnRecurrencePickListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.snooze_custom_repeat_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Populate model from fragment args
        initModelFromArguments();
        mFrequencySpinner = view.findViewById(R.id.snooze_custom_repeat_dialog_freq_spinner);
        ArrayAdapter freqAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.custom_repeat_freq_spinner_values,
                android.R.layout.simple_spinner_item);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFrequencySpinner.setAdapter(freqAdapter);
        mFrequencySpinner.setOnItemSelectedListener(this);

        mIntervalValue = view.findViewById(R.id.snooze_custom_repeat_interval_value);
        mIntervalValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int value;
                try {
                    value = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    value = 1;
                }
                mModel.getRule().setInterval(value);
                Log.d(TAG, "Set interval to " + value + ", rrule is now " + mModel.getRule());
            }
        });

        mWeeklyView = view.findViewById(R.id.snooze_custom_repeat_dialog_weekly_layout);
        mMonthlyView = view.findViewById(R.id.snooze_custom_repeat_dialog_monthly_layout);

        mWeekdayButtons = new ToggleButton[7];
        mWeekdayButtons[Weekday.SU.ordinal()] = view.findViewById(R.id.sun_button);
        mWeekdayButtons[Weekday.MO.ordinal()] = view.findViewById(R.id.mon_button);
        mWeekdayButtons[Weekday.TU.ordinal()] = view.findViewById(R.id.tue_button);
        mWeekdayButtons[Weekday.WE.ordinal()] = view.findViewById(R.id.wed_button);
        mWeekdayButtons[Weekday.TH.ordinal()] = view.findViewById(R.id.thu_button);
        mWeekdayButtons[Weekday.FR.ordinal()] = view.findViewById(R.id.fri_button);
        mWeekdayButtons[Weekday.SA.ordinal()] = view.findViewById(R.id.sat_button);
        for (ToggleButton weekdayButton : mWeekdayButtons) {
            weekdayButton.setOnCheckedChangeListener(this);
        }

        Button doneButton = view.findViewById(R.id.snooze_custom_repeat_dialog_button_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Picker is done. Returning rule " + mModel.getRule());
                mListener.onRecurrencePicked(mModel.getRule());
                dismiss();
            }
        });

        updateDialog();
    }

    private void initModelFromArguments() {
        String rrule = Objects.requireNonNull(getArguments()).getString("EXTRA_RRULE");
        RecurrenceRule rule = null;
        if (rrule != null) {
            try {
                rule = new RecurrenceRule(rrule);
            } catch (InvalidRecurrenceRuleException e) {
                Log.w(TAG, "Unable to parse recurrence rule string " + rrule);
            }
        }
        if (rule == null) {
            Log.d(TAG, "Defaulting to daily recurrence rule");
            rule = new RecurrenceRule(Freq.DAILY);
        }
        mModel = ViewModelProviders.of(this, new RecurrencePickerViewModel.Factory(rule))
                .get(RecurrencePickerViewModel.class);
    }

    private void updateDialog() {
        final RecurrenceRule rule = mModel.getRule();
        Log.d(TAG, "Updating dialog to match rule: " + rule.toString());
        mFrequencySpinner.setSelection(freqToPosition(rule.getFreq()));

        // only adjust the value if it's different, else we have to reposition cursors & other faff
        final String intervalValueStr = Integer.toString(rule.getInterval());
        if (!intervalValueStr.equals(mIntervalValue.getText().toString())) {
            mIntervalValue.setText(intervalValueStr);
        }

        mWeeklyView.setVisibility(rule.getFreq() == Freq.WEEKLY ? View.VISIBLE : View.GONE);
        mMonthlyView.setVisibility(rule.getFreq() == Freq.MONTHLY ? View.VISIBLE : View.GONE);

        if (rule.getFreq() == Freq.WEEKLY) {
            List<RecurrenceRule.WeekdayNum> byDayPart = rule.getByDayPart();
            if (byDayPart == null) byDayPart = Collections.emptyList();
            boolean[] shouldBeChecked = new boolean[7]; // defaults to false on allocation
            for (RecurrenceRule.WeekdayNum weekdayNum : byDayPart) {
                shouldBeChecked[weekdayNum.weekday.ordinal()] = true;
            }
            for (int i = 0; i < mWeekdayButtons.length; i++) {
                mWeekdayButtons[i].setChecked(shouldBeChecked[i]);
            }
        }
    }

    /**
     * OnItemSelectedListener, handles item selected for:
     * <li>mFreqSpinner</li>
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == mFrequencySpinner) {
            // Setting 'silent'=true throws away other recurrence parts that don't fit, keeps the rest.
            mModel.getRule().setFreq(positionToFreq(position), true);
        }
        updateDialog();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { /* no-op */ }

    /**
     * On Check changed listener, handles clicks for each of the weekday toggle buttons
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Weekday button isChecked status changed, rechecking all weekday state.");
        mModel.getRule().setByDayPart(buildByDayPart());
        Log.d(TAG, "Updated rule's 'byDay' part. New rule: " + mModel.getRule());
    }

    private List<RecurrenceRule.WeekdayNum> buildByDayPart() {
        ArrayList<RecurrenceRule.WeekdayNum> result = new ArrayList<>();
        for (int i = 0; i < mWeekdayButtons.length; i++) {
            if (mWeekdayButtons[i].isChecked()) {
                result.add(new RecurrenceRule.WeekdayNum(0, Weekday.values()[i]));
            }
        }
        return result;
    }

    /**
     * Maps from recurrence frequency to mFreqSpinner position
     */
    private static int freqToPosition(@NonNull Freq freq) {
        switch (freq) {
            case DAILY:
                return 0;
            case WEEKLY:
                return 1;
            case MONTHLY:
                return 2;
            case YEARLY:
                return 3;
            default:
                throw new IllegalArgumentException("Unsupported frequency " + freq);
        }
    }

    /**
     * Maps from mFreqSpinnerPosition to recurrence {@link Freq} constant
     */
    private static Freq positionToFreq(int position) {
        switch (position) {
            case 0:
                return Freq.DAILY;
            case 1:
                return Freq.WEEKLY;
            case 2:
                return Freq.MONTHLY;
            case 3:
                return Freq.YEARLY;
            default: {
                throw new IllegalArgumentException("Unknown frequency for position " + position);
            }
        }
    }
}