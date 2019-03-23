package home.westering56.taskbox.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.time.LocalTime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private CancellableOnTimeSetListener mOnTimeSetListener;
    /**
     * Extends OnTimeSetListener to include a 'cancelled' callback
     */
    public interface CancellableOnTimeSetListener extends TimePickerDialog.OnTimeSetListener {
        void onTimePickerCancel();
    }

    @NonNull
    public static TimePickerFragment newInstance(@NonNull CancellableOnTimeSetListener listener) {
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setOnTimeSetListener(listener);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // TODO: Adjust new snooze custom time to be something meaningful, rather than 'now'
        LocalTime now = LocalTime.now();
        return new TimePickerDialog(requireActivity(), mOnTimeSetListener,
                now.getHour(), now.getMinute(),
                DateFormat.is24HourFormat(requireContext()));
    }

    private void setOnTimeSetListener(@Nullable CancellableOnTimeSetListener listener) {
        mOnTimeSetListener = listener;
    }

    @Override
    public void onDetach() {
        mOnTimeSetListener = null;
        super.onDetach();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        mOnTimeSetListener.onTimePickerCancel();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mOnTimeSetListener.onTimeSet(view, hourOfDay, minute);
    }
}
