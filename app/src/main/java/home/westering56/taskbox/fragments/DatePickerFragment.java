package home.westering56.taskbox.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    /**
     * Extends OnDateSetListener to include a 'cancelled' callback
     */
    public interface CancellableOnDateSetListener extends DatePickerDialog.OnDateSetListener {
        void onDatePickerCancel();
    }

    private CancellableOnDateSetListener mCancellableOnDateSetListener;

    @NonNull
    public static DatePickerFragment newInstance(@NonNull CancellableOnDateSetListener listener) {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setOnDateSetListener(listener);
        return datePickerFragment;
    }

    private void setOnDateSetListener(@Nullable CancellableOnDateSetListener listener) {
        mCancellableOnDateSetListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final DatePickerDialog d = new DatePickerDialog(requireActivity());
        d.setOnDateSetListener(mCancellableOnDateSetListener);
        return d;
    }

    @Override
    public void onDetach() {
        mCancellableOnDateSetListener = null;
        super.onDetach();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mCancellableOnDateSetListener.onDateSet(view, year, month, dayOfMonth);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        mCancellableOnDateSetListener.onDatePickerCancel();
    }
}
