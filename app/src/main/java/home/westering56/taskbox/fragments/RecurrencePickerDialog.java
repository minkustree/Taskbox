package home.westering56.taskbox.fragments;

import android.content.DialogInterface;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import org.dmfs.rfc5545.Weekday;
import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRule.WeekdayNum;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import home.westering56.taskbox.R;

import static home.westering56.taskbox.RecurrencePickerHelper.getStringForOrdinal;
import static home.westering56.taskbox.RecurrencePickerHelper.getWeekOrdinalForDate;
import static home.westering56.taskbox.RecurrencePickerHelper.isLastDayOfTheMonth;
import static home.westering56.taskbox.RecurrencePickerHelper.weekdayFromDate;
import static org.dmfs.rfc5545.recur.RecurrenceRule.Part.BYDAY;
import static org.dmfs.rfc5545.recur.RecurrenceRule.Part.BYMONTHDAY;

class RecurrencePickerDialog extends DialogFragment implements AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "RecurrencePickerDialog";
    private static final String EXTRA_RULE_STR = "EXTRA_RULE_STR";
    private static final String EXTRA_START_DATE = "EXTRA_START_DATE";

//    public static void test(OnRecurrencePickListener listener, FragmentManager fragmentManager) {
//        RecurrencePickerDialog dlg = RecurrencePickerDialog.newInstance(listener, null, LocalDate.now());
//        dlg.show(fragmentManager, "recurrence_picker");
//    }

    public interface OnRecurrencePickListener {
        void onRecurrencePicked(@NonNull RecurrenceRule rule);

        void onRecurrencePickerCancelled();
    }

    /*
     * Data Model for this dialog
     */
    static class RecurrencePickerViewModel extends ViewModel {

        static class Factory implements ViewModelProvider.Factory {
            private final RecurrenceRule factoryRule;
            private final LocalDate startDate;

            Factory(RecurrenceRule rule, LocalDate startDate) {
                this.factoryRule = rule;
                this.startDate = startDate;
            }

            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (!modelClass.isAssignableFrom(RecurrencePickerViewModel.class)) {
                    throw new IllegalArgumentException("Unexpected model class " + modelClass);
                }
                //noinspection unchecked
                return (T) new RecurrencePickerViewModel(factoryRule, startDate);
            }
        }

        private final RecurrenceRule mRule;
        private final LocalDate mStartDate;

        RecurrencePickerViewModel(RecurrenceRule rule, LocalDate startDate) {
            this.mRule = rule;
            this.mStartDate = startDate;
        }

        RecurrenceRule getRule() {
            return mRule;
        }

        LocalDate getStartDate() {
            return mStartDate;
        }
    }

    private RecurrencePickerViewModel mModel;
    private OnRecurrencePickListener mListener;

    private Spinner mFrequencySpinner;
    private EditText mIntervalValue;
    private View mWeeklyView;
    private RadioGroup mMonthlyGroup;
    private ToggleButton[] mWeekdayButtons;
    private RadioButton mMonthlySameDayRadio;
    private RadioButton mMonthlyNthDayOfWeekRadio;
    private RadioButton mMonthlyLastDayRadio;

    /**
     * Create a new instance of the fragment, displaying the specified {@link RecurrenceRule}.
     * If there is no <tt>rule</tt> to display, pass null for <tt>rule</tt> and a default daily rule
     * will be shown.
     * Uses the supplied listener when returning the result.
     */
    @NonNull
    static RecurrencePickerDialog newInstance(@NonNull OnRecurrencePickListener listener,
                                              @Nullable RecurrenceRule rule,
                                              @NonNull LocalDate startDate) {
        Bundle args = new Bundle();
        args.putString(EXTRA_RULE_STR, rule == null ? null : rule.toString());
        args.putSerializable(EXTRA_START_DATE, startDate);
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

        mMonthlyGroup = view.findViewById(R.id.snooze_custom_repeat_dialog_monthly_group);
        mMonthlySameDayRadio = view.findViewById(R.id.snooze_custom_monthly_same_day_radio);
        mMonthlyNthDayOfWeekRadio = view.findViewById(R.id.snooze_custom_monthly_nth_day_of_week_radio);
        mMonthlyLastDayRadio = view.findViewById(R.id.snooze_custom_monthly_last_day_radio);
        mMonthlyGroup.setOnCheckedChangeListener(this);

        Button doneButton = view.findViewById(R.id.snooze_custom_repeat_dialog_button_done);
        doneButton.setOnClickListener(viewIgnored -> {
            Log.d(TAG, "Picker is done. Returning rule " + mModel.getRule());
            mListener.onRecurrencePicked(mModel.getRule());
            dismiss();
        });

        updateDialog();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        mListener.onRecurrencePickerCancelled();
    }

    /*
     * Initialisation and updating fields
     */
    private void initModelFromArguments() {
        Bundle args = Objects.requireNonNull(getArguments());
        final String ruleString = args.getString(EXTRA_RULE_STR);
        final LocalDate startDate = (LocalDate) args.getSerializable(EXTRA_START_DATE);
        RecurrenceRule rule = null;
        if (ruleString != null) {
            try {
                rule = new RecurrenceRule(ruleString);
            } catch (InvalidRecurrenceRuleException e) {
                Log.w(TAG, "Unable to parse recurrence rule string " + ruleString);
            }
        }
        if (rule == null) {
            Log.d(TAG, "Defaulting to daily recurrence rule");
            rule = new RecurrenceRule(Freq.DAILY);
        }
        mModel = ViewModelProviders.of(this, new RecurrencePickerViewModel.Factory(rule, startDate))
                .get(RecurrencePickerViewModel.class);
    }

    private void updateDialog() {
        RecurrenceRule rule = mModel.getRule();
        LocalDate startDate = mModel.getStartDate();
        Log.d(TAG, "Updating dialog to match rule: " + rule.toString());
        mFrequencySpinner.setSelection(freqToPosition(rule.getFreq()));

        // only adjust the value if it's different, else we have to reposition cursors & other faff
        final String intervalValueStr = Integer.toString(rule.getInterval());
        if (!intervalValueStr.equals(mIntervalValue.getText().toString())) {
            mIntervalValue.setText(intervalValueStr);
        }

        mWeeklyView.setVisibility(rule.getFreq() == Freq.WEEKLY ? View.VISIBLE : View.GONE);
        mMonthlyGroup.setVisibility(rule.getFreq() == Freq.MONTHLY ? View.VISIBLE : View.GONE);

        if (rule.getFreq() == Freq.WEEKLY) {
            List<WeekdayNum> byDayPart = rule.getByDayPart();
            if (byDayPart == null) byDayPart = Collections.emptyList();
            boolean[] shouldBeChecked = new boolean[7]; // defaults to false on allocation
            for (WeekdayNum weekdayNum : byDayPart) {
                shouldBeChecked[weekdayNum.weekday.ordinal()] = true;
            }
            for (int i = 0; i < mWeekdayButtons.length; i++) {
                mWeekdayButtons[i].setChecked(shouldBeChecked[i]);
            }
        }
        if (rule.getFreq() == Freq.MONTHLY) {
            // show the 'last day' option only if startDate is the last day of the month
            mMonthlyLastDayRadio.setVisibility(isLastDayOfTheMonth(startDate) ? View.VISIBLE : View.GONE);
            // monthly on every 1st/2nd/../last <weekday> text always reflects the start date
            updateMonthlyRuleTextFromModel();
            updateSelectedMonthRuleFromModel();
            if (mMonthlyGroup.getCheckedRadioButtonId() == -1) { // nothing is checked
                // default to the first item in the list
                Log.d(TAG, "No meaningful monthly rule set, choosing defaults");
                mMonthlySameDayRadio.setChecked(true); // have the handler set the model value, etc.
            }
        }
    }

    /*
     * Frequency-related methods
     */

    /**
     * OnItemSelectedListener, handles item selected for:
     * <li>mFreqSpinner</li>
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == mFrequencySpinner) {
            // Setting 'silent'=true throws away other recurrence parts that don't fit, keeps the rest.
            mModel.getRule().setFreq(positionToFreq(position), true);
            // TODO: throw away parts of the rule that don't fit the new frequency
        }
        updateDialog();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { /* no-op */ }

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

    /*
     * Weekly related methods
     */

    /**
     * On Check changed listener, handles clicks for each of the weekday toggle buttons
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Weekday button isChecked status changed, rechecking all weekday state.");
        mModel.getRule().setByDayPart(buildByDayPart());
        Log.d(TAG, "Updated rule's 'byDay' part. New rule: " + mModel.getRule());
    }

    private List<WeekdayNum> buildByDayPart() {
        ArrayList<WeekdayNum> result = new ArrayList<>();
        for (int i = 0; i < mWeekdayButtons.length; i++) {
            if (mWeekdayButtons[i].isChecked()) {
                result.add(new WeekdayNum(0, Weekday.values()[i]));
            }
        }
        return result;
    }


    /*
     * Monthly related methods
     */

    /**
     * Sets the 'selected' value of the monthly radio button group from the model, or un-checks
     * any selected radio in the mMonthlyGroup if we don't know how to display the rule in the model
     */
    private void updateSelectedMonthRuleFromModel() {
        final RecurrenceRule rule = mModel.getRule();
        if (rule.hasPart(BYMONTHDAY)) { // could be either same day (+ve) or last day (-ve)
            final int dayValue = rule.getByPart(BYMONTHDAY).get(0);
            if (dayValue < -1 || dayValue > 31 || dayValue == 0) {
                throw new IllegalStateException("Monthly recurrence rules must have BYMONTHDAY values in the set {-1, 1, .., 31}, not " + dayValue);
            }
            if (dayValue > 0) {
                mMonthlyGroup.check(R.id.snooze_custom_monthly_same_day_radio);
            } else {
                mMonthlyGroup.check(R.id.snooze_custom_monthly_last_day_radio);
            }
        } else if (rule.hasPart(RecurrenceRule.Part.BYDAY)) {
            mMonthlyGroup.check(R.id.snooze_custom_monthly_nth_day_of_week_radio);
        } else {
            // don't know how to display this rule - ensure that nothing is checked
            mMonthlyGroup.clearCheck();
        }
    }

    private void updateMonthlyRuleTextFromModel() {
        final LocalDate date = mModel.getStartDate();
        // first week, second week, etc. -1 = 'last week'
        final String ordinalText = getStringForOrdinal(getWeekOrdinalForDate(date));

        // day of week
        final String dowText = getResources().getStringArray(R.array.repeat_weekday_long)[weekdayFromDate(date).ordinal()];

        // set the text
        final String text = getString(R.string.snooze_custom_repeat_monthly_nth_day_of_week, ordinalText, dowText);
        mMonthlyNthDayOfWeekRadio.setText(text);
    }

    /**
     * <p>Called when the checked radio button has changed. When the
     * selection is cleared, checkedId is -1.</p>
     *
     * @param group     the group in which the checked radio button has changed
     * @param checkedId the unique identifier of the newly checked radio button
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group != mMonthlyGroup) return;
        final LocalDate date = mModel.getStartDate();

        switch (checkedId) {
            case R.id.snooze_custom_monthly_same_day_radio: {
                checkedClearModelRuleByPart(BYDAY);
                checkedSetModelRuleByPart(BYMONTHDAY, date.getDayOfMonth());
                break;
            }
            case R.id.snooze_custom_monthly_last_day_radio: {
                checkedClearModelRuleByPart(BYDAY);
                checkedSetModelRuleByPart(BYMONTHDAY, -1); // -1 is the last day of month
                break;
            }
            case R.id.snooze_custom_monthly_nth_day_of_week_radio: {
                checkedClearModelRuleByPart(BYMONTHDAY);
                WeekdayNum weekdayNum = new WeekdayNum(getWeekOrdinalForDate(date), weekdayFromDate(date));
                mModel.getRule().setByDayPart(Collections.singletonList(weekdayNum));
                break;
            }
        }
        Log.d(TAG, "Updated model. Rule is now " + mModel.getRule());
    }

    private void checkedClearModelRuleByPart(RecurrenceRule.Part part) {
        checkedSetModelRuleByPart(part, (Integer[]) null);
    }

    /**
     * @throws IllegalStateException if the new model would be be illegal
     */
    private void checkedSetModelRuleByPart(RecurrenceRule.Part part, Integer... values) throws IllegalStateException {
        try {
            mModel.getRule().setByPart(part, values);
        } catch (InvalidRecurrenceRuleException e) {
            Log.e(TAG, "Error setting recurrence rule part. Data model now in bad state.", e);
            throw new IllegalStateException(e);
        }
    }

}
