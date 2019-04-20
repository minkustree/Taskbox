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

class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private CancellableOnTimeSetListener mOnTimeSetListener;
    private LocalTime mInitialTime;

    /**
     * Extends OnTimeSetListener to include a 'cancelled' callback
     */
    public interface CancellableOnTimeSetListener extends TimePickerDialog.OnTimeSetListener {
        void onTimePickerCancel();
    }

    private void setInitialTime(LocalTime initialTime) {
        this.mInitialTime = initialTime;
    }

    @NonNull
    static TimePickerFragment newInstance(@NonNull CancellableOnTimeSetListener listener,
                                          @Nullable LocalTime initialTime) {
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setOnTimeSetListener(listener);
        if (initialTime != null) fragment.setInitialTime(initialTime);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LocalTime initialTime = mInitialTime != null ? mInitialTime : LocalTime.now();
        return new TimePickerDialog(requireActivity(), mOnTimeSetListener,
                initialTime.getHour(), initialTime.getMinute(),
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
