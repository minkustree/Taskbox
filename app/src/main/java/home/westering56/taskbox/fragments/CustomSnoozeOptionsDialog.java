package home.westering56.taskbox.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import home.westering56.taskbox.widget.CustomSpinnerAdapter;
import home.westering56.taskbox.CustomSnoozeOptionProvider;
import home.westering56.taskbox.R;
import home.westering56.taskbox.RepeatedTaskAdapterFactory;
import home.westering56.taskbox.RepeatedTaskAdapterFactory.RepetitionOption;
import home.westering56.taskbox.SnoozeTimeFormatter;
import home.westering56.taskbox.TaskData;
import home.westering56.taskbox.data.room.Task;

import static home.westering56.taskbox.MainActivity.EXTRA_TASK_ID;


public class CustomSnoozeOptionsDialog extends DialogFragment
        implements DatePickerFragment.CancellableOnDateSetListener,
        AdapterView.OnItemSelectedListener,
        TimePickerFragment.CancellableOnTimeSetListener,
        DialogInterface.OnClickListener, RecurrencePickerDialog.OnRecurrencePickListener {

    private static final String TAG = "CustomSnoozeTimeDlg";

    /**
     * @param taskId @{@link home.westering56.taskbox.data.room.Task#uid} of the task we're snoozing, or -1 if no task is stored yet.
     */
    static CustomSnoozeOptionsDialog newInstance(@NonNull SnoozeOptionsDialogFragment.SnoozeOptionListener listener, int taskId) {
        CustomSnoozeOptionsDialog fragment = new CustomSnoozeOptionsDialog();
        fragment.setSnoozeOptionListener(listener);
        Bundle args = new Bundle();
        args.putInt(EXTRA_TASK_ID, taskId);
        fragment.setArguments(args);
        return fragment;
    }

    private CustomSnoozeViewModel mModel;
    private CustomSnoozeOptionProvider mCustomSnoozeOptionProvider;
    private SnoozeOptionsDialogFragment.SnoozeOptionListener mSnoozeOptionListener;

    private TextView mDateText;
    private Spinner mTimeSelector;
    private Spinner mRepeatSelector;
    private CustomSpinnerAdapter mRepeatSelectorAdapter;

    private void setSnoozeOptionListener(@Nullable SnoozeOptionsDialogFragment.SnoozeOptionListener listener) {
        mSnoozeOptionListener = listener;
    }

    @Override
    public void onDestroy() {
        mSnoozeOptionListener = null;
        super.onDestroy();
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // fetch the task ID (-1 if there's no task)
        Bundle args = getArguments();
        int taskId = (args != null && args.containsKey(EXTRA_TASK_ID)) ? args.getInt(EXTRA_TASK_ID) : -1;

        // set up the model
        CustomSnoozeViewModel.Factory factory = new CustomSnoozeViewModel.Factory(
                TaskData.getInstance(requireContext()), taskId);
        mModel = ViewModelProviders.of(this, factory).get(CustomSnoozeViewModel.class);

        mCustomSnoozeOptionProvider = new CustomSnoozeOptionProvider(requireContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // It's OK to pass in 'null' as root here, as layout params are replaced by alert dialog
        View view = inflater.inflate(R.layout.snooze_date_time_picker, null, false);

        mDateText = view.findViewById(R.id.snooze_custom_date_selector);

        mTimeSelector = view.findViewById(R.id.snooze_custom_time_selector);
        mTimeSelector.setAdapter(mCustomSnoozeOptionProvider.getAdapter());

        mRepeatSelectorAdapter = RepeatedTaskAdapterFactory.buildAdapter(requireContext());
        mRepeatSelector = view.findViewById(R.id.snooze_custom_repeat_selector);
        mRepeatSelector.setAdapter(mRepeatSelectorAdapter);

        // Must call this before registering onItemSelected listeners, but after setting adapters.
        // Otherwise, listeners either fire as the UI is updated to match the view model or
        // calling Spinner#setSelection doesn't have any effect
        updateUiFromViewModel();

        // Set listeners now that we've updated the selections & states of the UI
        mDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "date picker clicked");
                showDatePickerFragment();
            }
        });
        mTimeSelector.setOnItemSelectedListener(this);
        mRepeatSelector.setOnItemSelectedListener(this);

        // Construct the dialog and continue
        builder.setView(view).setTitle(R.string.snooze_pick_date_time_title)
                .setPositiveButton(R.string.custom_snooze_options_dlg_button_save, this);
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mModel.mDate == null) {
            // Show the date picker when the dialog starts without one set.
            // If the user doesn't commit to a date, #onDatePickerCancel() will cancel this dialog,
            // as without a date there's no point continuing with any other questions.
            showDatePickerFragment();
        }
    }

    /**
     * Called when the dialog's positive button has been clicked to dispatch the
     * chosen custom snooze option result to the waiting listener.
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        Log.d(TAG, "Save button clicked");
        assert mSnoozeOptionListener != null;
        mSnoozeOptionListener.onSnoozeOptionSelected(mModel.toLocalDateTime(), mModel.mRule);
    }

    private void updateUiFromViewModel() {
        if (mModel.mDate != null) {
            mDateText.setText(SnoozeTimeFormatter.formatDate(getContext(), mModel.mDate));
        }
        if (mModel.mTime != null) {
            int position = mCustomSnoozeOptionProvider.getPositionForTime(mModel.mTime);
            // Update the time associated with the custom entry, if we have a custom time
            if (position == mCustomSnoozeOptionProvider.getCustomPosition()) {
                mCustomSnoozeOptionProvider.updateCustomTimeExample(mModel.mTime);
            } else {
                mCustomSnoozeOptionProvider.clearCustomTimeExample();
            }
            // Set the spinner to be the position matching the time data
            mTimeSelector.setSelection(position);
            mModel.mLastTimeSelectedPosition = position;
        }
        // mModel.mRule
        Log.d(TAG, "Determining repeat spinner position for rule: " + mModel.mRule);
        // will create or update the 'existing custom' position if mRule is not already in there
        int position = mRepeatSelectorAdapter.positionOf(RepetitionOption.buildDummyForRule(mModel.mRule));
        if (position == -1) {
            position = mRepeatSelectorAdapter.setCustomValue(RepetitionOption.buildCustomForRule(mModel.mRule));
        }
        Log.d(TAG, "Initialising repeat spinner position to be: " + position);
        mRepeatSelector.setSelection(position);
        mModel.mLastRepeatSelectedPosition = position;

    }

    /*
     * Item selected methods
     */

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, String.format("onItemSelected: parent=%s, view=%s, position=%d, id=%d", parent, view, position, id));
        if (parent == mTimeSelector) {
            onTimeItemSelected(parent, position);
        }
        if (parent == mRepeatSelector) {
            onRepeatOptionSelected(parent, position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { /* no-op */ }

    /*
     * Date picking methods
     */

    private void showDatePickerFragment() {
        DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(this, mModel.mDate);
        assert getFragmentManager() != null;
        datePickerFragment.show(getFragmentManager(), "snooze_date_picker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int zeroBasedMonth, int dayOfMonth) {
        // month arrives zero-based for compatibility with old Calendar#MONTH. Adjust.
        final int month = zeroBasedMonth + 1;
        Log.d(TAG, String.format("Date picked: %4d-%02d-%2d", year, month, dayOfMonth));
        mModel.mDate = LocalDate.of(year, month, dayOfMonth);
        updateUiFromViewModel();
    }

    @Override
    public void onDatePickerCancel() {
        if (mModel.mDate == null) {
            // date picker was cancelled, no date was set, so there's no point in continuing.
            // cancel this dialog and return. User can open us again once they're ready to
            // commit to a date.
            Objects.requireNonNull(getDialog()).cancel();
        }
    }


    /*
     * Repetition picking methods
     *
     * The repetition spinner list looks like:
     *
     * (Existing custom option)
     * No repeat
     * Daily
     * Weekly
     * Monthly
     * Yearly
     * Custom ...
     *
     * Where (Existing custom option) only exists if a custom rule has been set that doesn't match
     * any of the other rules. If the user selects any other standard option, the (Existing custom
     * option) entry disappears. If the user selects Custom .. and selects a new custom option, it
     * sets or replaces the (existing custom option). If the user selects Custom.. and cancels,
     * the original option is restored (even if it's existing custom option).
     */
    private void onRepeatOptionSelected(AdapterView<?> parent, int position) {
        //noinspection unchecked
        if (position == mRepeatSelectorAdapter.getCustomPickPosition()) {
            showRecurrencePickerDialog();
        } else {
            // update the model with the rule from the position we selected
            mModel.mRule = ((RepetitionOption) parent.getItemAtPosition(position)).getRule();
            Log.d(TAG, "Selected repeat pattern: " + mModel.mRule);
            mModel.mLastRepeatSelectedPosition = position;
            // if we didn't pick an existing custom rule, remove it from the list of options
            if (position != mRepeatSelectorAdapter.getCustomValuePosition()) {
                mRepeatSelectorAdapter.clearCustomValue();
                // the data in the set may have changed - update selected position
                mRepeatSelector.setSelection(mRepeatSelectorAdapter.positionOf(
                        RepetitionOption.buildDummyForRule(mModel.mRule)));
            }
        }
    }

    private void showRecurrencePickerDialog() {
        RecurrencePickerDialog dialog = RecurrencePickerDialog.newInstance(this, mModel.mRule);
        assert getFragmentManager() != null;
        dialog.show(getFragmentManager(), "repeat_dialog");
    }

    @Override
    public void onRecurrencePicked(@NonNull RecurrenceRule rule) {
        // Continue with setting the recurrence rule
        Log.d(TAG, "Recurrence picker completed. Updating custom entry & selecting new rule: " + rule);
        int pos = mRepeatSelectorAdapter.setCustomValue(RepetitionOption.buildCustomForRule(rule));
        // update the selected postion to be the newly picked custom rule
        Log.d(TAG, "Updating repeat selector spinner to new custom position " + pos);
        mRepeatSelector.setSelection(pos);
        // assume onRepeatOption will update mModel.mRule mLastRepeatSelectedPosition
    }

    @Override
    public void onRecurrencePickerCancelled() {
        Log.d(TAG, "Recurrence picker cancelled. Reverting to last selected repeat value");
        // Restore old repeat position
        mRepeatSelector.setSelection(mModel.mLastRepeatSelectedPosition);
    }


    /*
     * Time picking methods: happy path
     */

    private void onTimeItemSelected(AdapterView<?> parent, int position) {
        if (CustomSnoozeOptionProvider.isCustomPosition(parent, position)) {
            Log.d(TAG, "Custom time selected. Launching time picker fragment");
            showTimePickerFragment();
            // continues in onTimeSet or onTimePickerCancel
        } else {
            mModel.mTime = CustomSnoozeOptionProvider.getLocalTimeAtPosition(parent, position);
            Log.d(TAG, "Named time of day selected. Set internal time to " + mModel.mTime);
            saveLastSelectedTimePosition();
            updateUiFromViewModel(); // not strictly necessary, but is a consistent pattern for future use
        }
    }

    private void showTimePickerFragment() {
        TimePickerFragment timePickerFragment = TimePickerFragment.newInstance(this, mModel.mTime);
        assert getFragmentManager() != null;
        timePickerFragment.show(getFragmentManager(), "snooze_time_picker");
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Log.d(TAG, String.format("Setting custom time to %s:%s", hourOfDay, minute));
        final LocalTime customTime = LocalTime.of(hourOfDay, minute);
        mModel.mTime = customTime;

        mCustomSnoozeOptionProvider.updateCustomTimeExample(customTime);
        saveLastSelectedTimePosition();
        updateUiFromViewModel();
    }

    private void saveLastSelectedTimePosition() {
        mModel.mLastTimeSelectedPosition = mTimeSelector.getSelectedItemPosition();
    }

    /*
     * Time picking methods: un-happy path
     */
    @Override
    public void onTimePickerCancel() {
        Log.d(TAG, "Time picking cancelled. Reverting to previous selection");
        revertToLastSelectedTimePosition();
        // no need to update UI from model - nothing has changed, picker has cancelled.
    }

    private void revertToLastSelectedTimePosition() {
        mTimeSelector.setSelection(mModel.mLastTimeSelectedPosition);
    }


    /*
     * View model implementation, holds the data for this fragment
     */
    static class CustomSnoozeViewModel extends ViewModel {

        static class Factory implements ViewModelProvider.Factory {
            private final TaskData factoryData;
            private final int factoryId;

            Factory(TaskData data, int taskId) {
                factoryData = data;
                factoryId = taskId;
            }

            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (!modelClass.isAssignableFrom(CustomSnoozeViewModel.class)) {
                    throw new IllegalArgumentException("Unknown ViewModel class");
                }
                //noinspection unchecked
                return (T) new CustomSnoozeViewModel(factoryData, factoryId);
            }
        }

        LocalDate mDate = null;
        LocalTime mTime = null;
        RecurrenceRule mRule = null;

        int mLastTimeSelectedPosition = 0;
        int mLastRepeatSelectedPosition = 0;

        CustomSnoozeViewModel(TaskData taskData, int taskId) {
            if (taskId != -1) { // only load if there's an existing task, else start from clean
                Task t = taskData.getTask(taskId);
                loadFrom(t);
            }
        }

        /**
         * If {@link #mDate} or {@link #mTime} are null, will throw a {@link NullPointerException}
         */
        LocalDateTime toLocalDateTime() {
            return LocalDateTime.of(mDate, mTime);
        }

        private void loadFrom(Task t) {
            /* Active tasks may still have snoozeUntil set from when they last woke.
               Use t.isSnoozed() over t.snoozeUntil != null to avoid restoring old snooze times. */
            if (t.isSnoozed()) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(t.snoozeUntil, ZoneId.systemDefault());
                mDate = localDateTime.toLocalDate();
                mTime = localDateTime.toLocalTime();
            }
            if (t.isRepeating()) {
                mRule = t.rrule;
            }
        }
    }
}
