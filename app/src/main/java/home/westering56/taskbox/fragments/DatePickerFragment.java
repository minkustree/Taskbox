package home.westering56.taskbox.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.DatePicker;

import java.time.LocalDate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private LocalDate mInitialDate;

    /**
     * Extends OnDateSetListener to include a 'cancelled' callback
     */
    public interface CancellableOnDateSetListener extends DatePickerDialog.OnDateSetListener {
        void onDatePickerCancel();
    }

    private CancellableOnDateSetListener mCancellableOnDateSetListener;

    @NonNull
    public static DatePickerFragment newInstance(@NonNull CancellableOnDateSetListener listener,
                                                 @Nullable LocalDate initialDate) {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setOnDateSetListener(listener);
        if (initialDate != null) datePickerFragment.setInitialDate(initialDate);
        return datePickerFragment;
    }

    private void setOnDateSetListener(@Nullable CancellableOnDateSetListener listener) {
        mCancellableOnDateSetListener = listener;
    }

    private void setInitialDate(LocalDate date) {
        mInitialDate = date;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final DatePickerDialog d = new DatePickerDialog(requireActivity());
        d.setOnDateSetListener(mCancellableOnDateSetListener);
        if (mInitialDate != null) {
            d.updateDate(mInitialDate.getYear(),
                    mInitialDate.getMonth().getValue() - 1,
                    mInitialDate.getDayOfMonth());
        }
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
