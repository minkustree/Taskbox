package home.westering56.taskbox.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;

import home.westering56.taskbox.R;
import home.westering56.taskbox.RepeatedTaskAdapterFactory;
import home.westering56.taskbox.RepeatedTaskAdapterFactory.RepetitionOption;
import home.westering56.taskbox.SnoozeTimeAdapterFactory;
import home.westering56.taskbox.TaskDetailActivity;
import home.westering56.taskbox.data.room.Task;
import home.westering56.taskbox.formatter.SnoozeTimeFormatter;
import home.westering56.taskbox.widget.CustomSpinnerAdapter;


public class CustomSnoozeOptionsDialog extends DialogFragment
        implements SnoozeOptionListener,
        DatePickerDialog.CancellableOnDateSetListener,
        AdapterView.OnItemSelectedListener,
        TimePickerDialog.CancellableOnTimeSetListener,
        DialogInterface.OnClickListener, RecurrencePickerDialog.OnRecurrencePickListener {


    private static final String TAG = "CustomSnoozeOptDlg";
    static final String FRAGMENT_TAG = TAG;

    /*
     * View model implementation, holds the data for this fragment
     */
    static class CustomSnoozeViewModel extends ViewModel {
        LocalDate mDate = null;
        LocalTime mTime = null;
        RecurrenceRule mRule = null;

        int mLastTimeSelectedPosition = 0;
        int mLastRepeatSelectedPosition = 0;

        /**
         * If {@link #mDate} or {@link #mTime} are null, will throw a {@link NullPointerException}
         */
        LocalDateTime toLocalDateTime() {
            return LocalDateTime.of(mDate, mTime);
        }

        void loadFrom(@NonNull final Task t) {
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


    static CustomSnoozeOptionsDialog newInstance() {
        return new CustomSnoozeOptionsDialog();
    }

    private CustomSnoozeViewModel mModel;
    private SnoozeOptionListener mSnoozeOptionListener;

    private TextView mDateText;
    private Spinner mTimeSelector;
    private Spinner mRepeatSelector;
    private CustomSpinnerAdapter<LocalTime> mTimeSelectorAdapter;
    private CustomSpinnerAdapter<RepetitionOption> mRepeatSelectorAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mSnoozeOptionListener = (SnoozeOptionListener) context;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("CustomSnoozeOptionsDialog must be attached to Activity which implements SnoozeOptionListener");
        }
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // set up the models
        TaskDetailActivity.TaskDetailViewModel taskViewModel = ViewModelProviders.of(requireActivity()).get(TaskDetailActivity.TaskDetailViewModel.class);
        mModel = ViewModelProviders.of(requireActivity()).get(CustomSnoozeViewModel.class);
        mModel.loadFrom(taskViewModel.getTask());


        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // It's OK to pass in 'null' as root here, as layout params are replaced by alert dialog
        View view = inflater.inflate(R.layout.snooze_date_time_picker, null, false);

        mDateText = view.findViewById(R.id.snooze_custom_date_selector);

        mTimeSelector = view.findViewById(R.id.snooze_custom_time_selector);
        mTimeSelectorAdapter = SnoozeTimeAdapterFactory.buildAdapter(requireContext());
        mTimeSelector.setAdapter(mTimeSelectorAdapter);

        mRepeatSelector = view.findViewById(R.id.snooze_custom_repeat_selector);
        mRepeatSelectorAdapter = RepeatedTaskAdapterFactory.buildAdapter(requireContext());
        mRepeatSelector.setAdapter(mRepeatSelectorAdapter);

        // Must call this before registering onItemSelected listeners, but after setting adapters.
        // Otherwise, listeners either fire as the UI is updated to match the view model or
        // calling Spinner#setSelection doesn't have any effect
        updateUiFromModel();

        // Set listeners now that we've updated the selections & states of the UI
        mDateText.setOnClickListener(viewIgnored -> {
            Log.d(TAG, "date picker clicked");
            showDatePickerFragment();
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

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        // re-show the calling fragment
        Objects.requireNonNull(getFragmentManager()).popBackStack();
    }

    /**
     * Called when the dialog's positive button has been clicked to dispatch the
     * chosen custom snooze option result to the waiting listener.
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        Log.d(TAG, "Save button clicked");
        onSnoozeOptionSelected(mModel.toLocalDateTime(), mModel.mRule);
    }

    /**
     * @param snoozeUntil the chosen time until which the task will snooze
     * @param rule        {@link RecurrenceRule} that describes how this task repeats after snoozing,
     */
    @Override
    public void onSnoozeOptionSelected(LocalDateTime snoozeUntil, RecurrenceRule rule) {
        mSnoozeOptionListener.onSnoozeOptionSelected(snoozeUntil, rule);
        // all done. No need to go back, decisions on snooze time have all been made. Just get out.
        Objects.requireNonNull(getFragmentManager()).beginTransaction().remove(this).commit();
    }

    private void updateUiFromModel() {
        if (mModel.mDate != null) updateDateTextFromModel();
        if (mModel.mTime != null) updateTimeSpinnerFromModel();
        updateRepeatSpinnerFromModel();
    }

    private void updateDateTextFromModel() {
        mDateText.setText(SnoozeTimeFormatter.formatDate(getContext(), mModel.mDate));
    }

    private void updateTimeSpinnerFromModel() {
        // Update the time associated with the custom entry, if we have a custom time
        int position = mTimeSelectorAdapter.positionOf(mModel.mTime);
        if (position == -1) {
            position = mTimeSelectorAdapter.setCustomValue(mModel.mTime);
        }
        // Set the spinner to be the position matching the time data
        Log.d(TAG, "Initialising repeat time position to be: " + position);
        mTimeSelector.setSelection(position);
        mModel.mLastTimeSelectedPosition = position;
    }

    private void updateRepeatSpinnerFromModel() {
        // mModel.mRule
        Log.d(TAG, "Determining repeat spinner position for rule: " + mModel.mRule);
        // will create or update the 'existing custom' position if mRule is not already in there
        int position = mRepeatSelectorAdapter.positionOf(RepetitionOption.buildDummyForRule(mModel.mRule));
        if (position == -1) {
            position = mRepeatSelectorAdapter.setCustomValue(RepetitionOption.buildCustomForRule(requireContext(), mModel.mRule));
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
            onTimeItemSelected(position);
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
        final FragmentManager manager = Objects.requireNonNull(getFragmentManager());
        DatePickerDialog datePickerDialog = (DatePickerDialog) manager.findFragmentByTag(DatePickerDialog.FRAGMENT_TAG);

        FragmentTransaction transaction = manager.beginTransaction();
        if (datePickerDialog == null) {
            datePickerDialog = DatePickerDialog.newInstance(FRAGMENT_TAG, mModel.mDate);
            transaction.add(datePickerDialog, DatePickerDialog.FRAGMENT_TAG);
        }
        transaction.show(datePickerDialog);
        transaction.commit();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int zeroBasedMonth, int dayOfMonth) {
        // month arrives zero-based for compatibility with old Calendar#MONTH. Adjust.
        final int month = zeroBasedMonth + 1;
        Log.d(TAG, String.format("Date picked: %4d-%02d-%2d", year, month, dayOfMonth));
        mModel.mDate = LocalDate.of(year, month, dayOfMonth);
        updateDateTextFromModel();
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
        RecurrencePickerDialog dialog = RecurrencePickerDialog.newInstance(this, mModel.mRule, mModel.mDate);
        assert getFragmentManager() != null;
        dialog.show(getFragmentManager(), "repeat_dialog");
    }

    @Override
    public void onRecurrencePicked(@NonNull RecurrenceRule rule) {
        // Continue with setting the recurrence rule
        Log.d(TAG, "Recurrence picker completed. Updating custom entry & selecting new rule: " + rule);
        int pos = mRepeatSelectorAdapter.setCustomValue(RepetitionOption.buildCustomForRule(requireContext(), rule));
        // update the selected position to be the newly picked custom rule
        Log.d(TAG, "Updating repeat selector spinner to new custom position " + pos);
        mRepeatSelector.setSelection(pos);
        // assume onRepeatOption will update mModel.mRule and mLastRepeatSelectedPosition
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
    private void onTimeItemSelected(int position) {
        if (position == mTimeSelectorAdapter.getCustomPickPosition()) {
            Log.d(TAG, "Custom time selected. Launching time picker fragment");
            showTimePickerFragment();
            // continues in onTimeSet or onTimePickerCancel
        } else {
            mModel.mTime = mTimeSelectorAdapter.getValue(position);
            mModel.mLastTimeSelectedPosition = position;
            Log.d(TAG, String.format("Time item at position %d selected. Stored time: %s", position, mModel.mTime));
            if (position != mTimeSelectorAdapter.getCustomValuePosition()) {
                mTimeSelectorAdapter.clearCustomValue();
                // re-select for new position in case clearing custom value changed it
                mTimeSelector.setSelection(mTimeSelectorAdapter.positionOf(mModel.mTime));
            }
        }
    }

    private void showTimePickerFragment() {
        /*
         * This ens up getting called each time we rotate, causing multiple time pickers to show. So
         * only create a new fragment if we need to, otherwise, else ensure the old one is showing.
         */
        FragmentManager manager = Objects.requireNonNull(getFragmentManager());
        TimePickerDialog timePickerDialog = (TimePickerDialog) manager.findFragmentByTag(TimePickerDialog.FRAGMENT_TAG);

        FragmentTransaction transaction = manager.beginTransaction();
        if (timePickerDialog == null) {
            timePickerDialog = TimePickerDialog.newInstance(FRAGMENT_TAG, mModel.mTime);
            transaction.add(timePickerDialog, TimePickerDialog.FRAGMENT_TAG);
        } // else... don't need to addTask a fragment that's been found to be there already. Continue.

        transaction.show(timePickerDialog);
        transaction.commit();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Log.d(TAG, String.format("Setting custom time to %s:%s", hourOfDay, minute));
        final LocalTime customTime = LocalTime.of(hourOfDay, minute);
        mTimeSelectorAdapter.setCustomValue(customTime);
        mTimeSelector.setSelection(mTimeSelectorAdapter.getCustomValuePosition());
        // callback to onTimeItemSelected triggered by this will take care of
        // setting mTime and mLastTimeSelectedPosition in mModel
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

}
