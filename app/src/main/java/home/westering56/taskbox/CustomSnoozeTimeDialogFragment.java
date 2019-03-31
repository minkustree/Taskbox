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

import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import home.westering56.taskbox.RepeatedTaskAdapterFactory.RepetitionOption;
import home.westering56.taskbox.fragments.DatePickerFragment;
import home.westering56.taskbox.fragments.TimePickerFragment;


@SuppressWarnings("WeakerAccess")
public class CustomSnoozeTimeDialogFragment extends DialogFragment
        implements DatePickerFragment.CancellableOnDateSetListener,
        TimePickerFragment.CancellableOnTimeSetListener {
    private static final String TAG = "CustomSnoozeTimeDlg";

    public static CustomSnoozeTimeDialogFragment newInstance(@NonNull SnoozeDialogFragment.SnoozeOptionListener listener) {
        CustomSnoozeTimeDialogFragment fragment = new CustomSnoozeTimeDialogFragment();
        fragment.setSnoozeOptionListener(listener);
        return fragment;
    }

    static class SnoozeCustomModel {
        LocalDate mDate = null;
        LocalTime mTime = null;
        RecurrenceRule mRule = null;

        LocalDateTime toLocalDateTime() {
            // TODO: Guard against mDate and/or mTime being null
            return LocalDateTime.of(mDate, mTime);
        }
    }

    private SnoozeCustomModel mModel;
    private SnoozeDialogFragment.SnoozeOptionListener mSnoozeOptionListener;
    private TextView mDateText;
    private Spinner mTimeSelector;
    private int mLastTimeSelectedPosition;

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
        mModel = new SnoozeCustomModel();

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // It's OK to pass in 'null' as root here, as layout params are replaced by alert dialog
        View view = inflater.inflate(R.layout.snooze_date_time_picker, null, false);

        mDateText = view.findViewById(R.id.snooze_custom_date_selector);
        mDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "date picker clicked");
                showDatePickerFragment();
            }
        });

        mTimeSelector = view.findViewById(R.id.snooze_custom_time_selector);
        mTimeSelector.setAdapter(CustomSnoozeTimeProvider.getInstance().getDefaultAdapter(requireContext()));
        mTimeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onTimeItemSelected(parent, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op
            }
        });
        mLastTimeSelectedPosition = 0;

        Spinner mRepeatSelector = view.findViewById(R.id.snooze_custom_repeat_selector);
        mRepeatSelector.setAdapter(RepeatedTaskAdapterFactory.buildAdapter(requireContext()));
        // assume "No repeat" is at first position, and custom is at last position
        mRepeatSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mModel.mRule = ((RepetitionOption)parent.getItemAtPosition(position)).getRule();
                Log.d(TAG, "Selected repeat pattern: " + mModel.mRule);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder .setView(view)
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
        updateUiFields();
        // Show the date picker when the dialog starts. If the user doesn't commit to a date,
        // #onDatePickerCancel() will cancel this dialog, as without a date there's no point
        // continuing with any other questions.
        if (mModel.mDate == null) {
            showDatePickerFragment();
        }
    }

    private void updateUiFields() {
        if (mModel.mDate != null) {
            mDateText.setText(SnoozeTimeFormatter.formatDate(getContext(), mModel.mDate));
        }
    }

    private void showDatePickerFragment() {
        DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(this);
        assert getFragmentManager() != null;
        datePickerFragment.show(getFragmentManager(), "snooze_date_picker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int zeroBasedMonth, int dayOfMonth) {
        // month arrives zero-based for compatibility with old Calendar#MONTH. Adjust.
        final int month = zeroBasedMonth + 1;
        Log.d(TAG, String.format("Date picked: %4d-%02d-%2d", year, month, dayOfMonth));
        mModel.mDate = LocalDate.of(year, month, dayOfMonth);
        updateUiFields();
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

    private void showTimePickerFragment() {
        TimePickerFragment timePickerFragment = TimePickerFragment.newInstance(this);
        assert getFragmentManager() != null;
        timePickerFragment.show(getFragmentManager(), "snooze_time_picker");
    }

    public void onTimeItemSelected(AdapterView<?> parent, int position) {
        if (CustomSnoozeTimeProvider.isCustomPosition(parent, position)) {
            Log.d(TAG, "Custom time selected. Launching time picker fragment");
            showTimePickerFragment();
        } else {
            mModel.mTime = CustomSnoozeTimeProvider.getLocalTimeAtPosition(parent, position);
            Log.d(TAG, "Named time of day selected. Set internal time to " + mModel.mTime);
            // User chose something other than custom, so reset the custom time field
            CustomSnoozeTimeProvider.getInstance().clearCustomTimeExample();
            saveLastSelectedPosition(position);
            updateUiFields(); // not strictly necessary, but is a consistent pattern for future use
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Log.d(TAG, String.format("Setting custom time to %s:%s", hourOfDay, minute));
        final LocalTime customTime = LocalTime.of(hourOfDay, minute);
        mModel.mTime = customTime;
        // Have the custom spinner entry update to show this new custom time, e.g. "Custom (3:14 PM)"
        CustomSnoozeTimeProvider snoozeTimeProvider = CustomSnoozeTimeProvider.getInstance();
        snoozeTimeProvider.updateCustomTimeExample(customTime);
        saveLastSelectedPosition(snoozeTimeProvider.getCustomPosition());
        updateUiFields();  // not strictly necessary, but is a consistent pattern for future use
    }

    @Override
    public void onTimePickerCancel() {
        Log.d(TAG, "Time picking cancelled. Reverting to previous selection");
        restoreLastSelectedPosition();
    }

    private void saveLastSelectedPosition(int position) {
        mLastTimeSelectedPosition = position;
    }

    private void restoreLastSelectedPosition() {
        mTimeSelector.setSelection(mLastTimeSelectedPosition);
    }
}
