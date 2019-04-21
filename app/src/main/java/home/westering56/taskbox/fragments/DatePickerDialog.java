package home.westering56.taskbox.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDate;
import java.util.Objects;

public class DatePickerDialog extends DialogFragment {
    /**
     * Extends OnDateSetListener to include a 'cancelled' callback
     */
    public interface CancellableOnDateSetListener extends android.app.DatePickerDialog.OnDateSetListener {
        void onDatePickerCancel();
    }

    static String FRAGMENT_TAG = "DatePickerDialogTag";
    private static final String TAG = "DatePickerDialog";

    private static final String ARG_INITIAL_DATE = "ARG_INITIAL_DATE";
    private static final String ARG_CALLBACK_TAG = "ARG_CALLBACK_TAG";

    private CancellableOnDateSetListener mCancellableOnDateSetListener;


    @SuppressWarnings("SameParameterValue")
    @NonNull
    static DatePickerDialog newInstance(@NonNull String callbackFragmentTag,
                                               @Nullable LocalDate initialDate) {
        Log.d(TAG, String.format("newInstance. (callbackFragmentTag=%s, initialDate=%s)", callbackFragmentTag, initialDate));
        DatePickerDialog datePickerDialog = new DatePickerDialog();
        datePickerDialog.setArguments(buildArguments(callbackFragmentTag, initialDate));
        return datePickerDialog;
    }

    private static Bundle buildArguments(@NonNull String callbackFragmentTag,
                                         @Nullable LocalDate initialDate) {
        Bundle args = new Bundle();
        args.putString(ARG_CALLBACK_TAG, callbackFragmentTag);
        args.putSerializable(ARG_INITIAL_DATE, initialDate);
        return args;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, String.format("onCreateDialog. (savedInstanceState=%s)", savedInstanceState));
        Bundle args = Objects.requireNonNull(getArguments());
        final LocalDate initialDate = (LocalDate) args.getSerializable(ARG_INITIAL_DATE);
        mCancellableOnDateSetListener = (CancellableOnDateSetListener) Objects.requireNonNull
                (getFragmentManager()).findFragmentByTag(args.getString(ARG_CALLBACK_TAG));

        Log.d(TAG, String.format("Creating inner Dialog. (initialDate=%s, listener=%s)",
                initialDate, mCancellableOnDateSetListener));
        android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(requireContext());
        dialog.setOnDateSetListener(mCancellableOnDateSetListener);
        if (initialDate != null) {
            dialog.updateDate(initialDate.getYear(),
                    initialDate.getMonth().getValue() - 1,
                    initialDate.getDayOfMonth());
        }
        return dialog;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        mCancellableOnDateSetListener.onDatePickerCancel();
    }
}
