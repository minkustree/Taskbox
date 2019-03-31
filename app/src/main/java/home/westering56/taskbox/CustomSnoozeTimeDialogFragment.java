package home.westering56.taskbox;

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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;
import home.westering56.taskbox.RepeatedTaskAdapterFactory.RepetitionOption;
import home.westering56.taskbox.data.room.Task;
import home.westering56.taskbox.fragments.DatePickerFragment;
import home.westering56.taskbox.fragments.TimePickerFragment;

import static home.westering56.taskbox.MainActivity.EXTRA_TASK_ID;


@SuppressWarnings("WeakerAccess")
public class CustomSnoozeTimeDialogFragment extends DialogFragment
        implements DatePickerFragment.CancellableOnDateSetListener,
        TimePickerFragment.CancellableOnTimeSetListener {

    private abstract class ABSOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public abstract void onItemSelected(AdapterView<?> parent, View view, int position, long id);

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private static final String TAG = "CustomSnoozeTimeDlg";

    /** @param taskId @{@link home.westering56.taskbox.data.room.Task#uid} of the task we're snoozing, or -1 if no task is stored yet. */
    public static CustomSnoozeTimeDialogFragment newInstance(@NonNull SnoozeDialogFragment.SnoozeOptionListener listener, int taskId) {
        CustomSnoozeTimeDialogFragment fragment = new CustomSnoozeTimeDialogFragment();
        fragment.setSnoozeOptionListener(listener);
        Bundle args = new Bundle();
        args.putInt(EXTRA_TASK_ID, taskId);
        fragment.setArguments(args);
        return fragment;
    }

    private SnoozeCustomDataViewModel mModel;
    private CustomSnoozeTimeProvider mCustomSnoozeTimeProvider;
    private SnoozeDialogFragment.SnoozeOptionListener mSnoozeOptionListener;

    private TextView mDateText;
    private Spinner mTimeSelector;

    private void setSnoozeOptionListener(@Nullable SnoozeDialogFragment.SnoozeOptionListener listener) {
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
        mModel = ViewModelProviders.of(this).get(SnoozeCustomDataViewModel.class);
        mCustomSnoozeTimeProvider = new CustomSnoozeTimeProvider(requireContext());
        Bundle args = getArguments();
        if (args != null && args.containsKey(EXTRA_TASK_ID)) {
            int taskId = args.getInt(EXTRA_TASK_ID);
            Task t = TaskData.getInstance(requireContext()).getTask(taskId);
            mModel.loadFrom(t);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // It's OK to pass in 'null' as root here, as layout params are replaced by alert dialog
        View view = inflater.inflate(R.layout.snooze_date_time_picker, null, false);

        mDateText = view.findViewById(R.id.snooze_custom_date_selector);

        mTimeSelector = view.findViewById(R.id.snooze_custom_time_selector);
        mTimeSelector.setAdapter(mCustomSnoozeTimeProvider.getAdapter());

        Spinner mRepeatSelector = view.findViewById(R.id.snooze_custom_repeat_selector);
        mRepeatSelector.setAdapter(RepeatedTaskAdapterFactory.buildAdapter(requireContext()));

        // Must call this before registering onItemSelected listeners, but after setting adapters.
        // Otherwise, listeners either fire as the UI is updated to match the view model or
        // calling Spinner#setSelection doesn't have any effect
        updateUiFromViewModel();

        mDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "date picker clicked");
                showDatePickerFragment();
            }
        });


        mTimeSelector.setOnItemSelectedListener(new ABSOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onTimeItemSelected(parent, position);
            }
        });


        mRepeatSelector.setOnItemSelectedListener(new ABSOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mModel.mRule = ((RepetitionOption) parent.getItemAtPosition(position)).getRule();
                Log.d(TAG, "Selected repeat pattern: " + mModel.mRule);
            }
        });

        builder.setView(view)
                .setTitle(R.string.snooze_pick_date_time_title)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Save button clicked");
                        if (mSnoozeOptionListener != null) {
                            if (mModel.mRule == null) {
                                mSnoozeOptionListener.onSnoozeOptionSelected(mModel.toLocalDateTime());
                            } else {
                                mSnoozeOptionListener.onSnoozeOptionSelected(mModel.toLocalDateTime(), mModel.mRule);
                            }
                        }
                    }
                });
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

    private void updateUiFromViewModel() {
        if (mModel.mDate != null) {
            mDateText.setText(SnoozeTimeFormatter.formatDate(getContext(), mModel.mDate));
        }
        if (mModel.mTime != null) {
            int position = mCustomSnoozeTimeProvider.getPositionForTime(mModel.mTime);
            // Update the time associated with the custom entry, if we have a custom time
            if (position == mCustomSnoozeTimeProvider.getCustomPosition()) {
                mCustomSnoozeTimeProvider.updateCustomTimeExample(mModel.mTime);
            } else {
                mCustomSnoozeTimeProvider.clearCustomTimeExample();
            }
            // Set the spinner to be the position matching the time data
            mTimeSelector.setSelection(position);
        }
        if (mModel.mRule != null) {
            // TODO: Set the spinner to be the position matching the rule data
        }
    }

    /*
     * Date picking methods
     */

    private void showDatePickerFragment() {
        DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(this);
        // TODO: Put mModel.mDate into the arguments if it's there already
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
     * Time picking methods
     */

    private void showTimePickerFragment() {
        TimePickerFragment timePickerFragment = TimePickerFragment.newInstance(this);
        // TODO: Put mModel.mTime into the arguments if it's there already
        assert getFragmentManager() != null;
        timePickerFragment.show(getFragmentManager(), "snooze_time_picker");
    }

    /*
     * Time picking methods: happy path
     */

    public void onTimeItemSelected(AdapterView<?> parent, int position) {
        if (CustomSnoozeTimeProvider.isCustomPosition(parent, position)) {
            Log.d(TAG, "Custom time selected. Launching time picker fragment");
            showTimePickerFragment();
            // continues in onTimeSet or onTimePickerCancel
        } else {
            mModel.mTime = CustomSnoozeTimeProvider.getLocalTimeAtPosition(parent, position);
            Log.d(TAG, "Named time of day selected. Set internal time to " + mModel.mTime);
            saveLastSelectedTimePosition();
            updateUiFromViewModel(); // not strictly necessary, but is a consistent pattern for future use
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Log.d(TAG, String.format("Setting custom time to %s:%s", hourOfDay, minute));
        final LocalTime customTime = LocalTime.of(hourOfDay, minute);
        mModel.mTime = customTime;

        mCustomSnoozeTimeProvider.updateCustomTimeExample(customTime);
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
}
