package home.westering56.taskbox.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.time.LocalTime;
import java.util.Objects;

public class TimePickerDialog extends DialogFragment {
    static final String FRAGMENT_TAG = "TimePickerTag";

    private static final String TAG = "TimePickerDialog";
    private static final String ARG_INITIAL_TIME = "ARG_INITIAL_TIME";
    private static final String ARG_CALLBACK_TAG = "ARG_CALLBACK_TAG";

    private CancellableOnTimeSetListener mOnTimeSetListener;

    /**
     * Extends OnTimeSetListener to include a 'cancelled' callback
     */
    public interface CancellableOnTimeSetListener extends android.app.TimePickerDialog.OnTimeSetListener {
        void onTimePickerCancel();
    }


    @SuppressWarnings("SameParameterValue")
    @NonNull
    static TimePickerDialog newInstance(@NonNull String callbackFragmentTag,
                                        @Nullable LocalTime initialTime) {
        Log.d(TAG, String.format("newInstance. (callbackFragmentTag=%s, initialTime=%s)", callbackFragmentTag, initialTime));
        TimePickerDialog fragment = new TimePickerDialog();
        fragment.setArguments(buildArguments(callbackFragmentTag, initialTime));
        return fragment;
    }

    private static Bundle buildArguments(@NonNull String callbackFragmentTag,
                                         @Nullable LocalTime initialTime) {
        Bundle args = new Bundle();
        args.putString(ARG_CALLBACK_TAG, callbackFragmentTag);
        args.putSerializable(ARG_INITIAL_TIME, initialTime);
        return args;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Bundle args = Objects.requireNonNull(getArguments());
        Log.d(TAG, String.format("onCreateDialog (savedInstanceState = %s, args = %s)", savedInstanceState, args));

        LocalTime initialTime = (LocalTime) args.getSerializable(ARG_INITIAL_TIME);
        if (initialTime == null) initialTime = LocalTime.now();
        Log.d(TAG, String.format("initialTime restored or set to %s", initialTime));

        final String callbackTag = args.getString(ARG_CALLBACK_TAG);
        mOnTimeSetListener = (CancellableOnTimeSetListener) Objects.requireNonNull(getFragmentManager()).findFragmentByTag(callbackTag);
        Log.d(TAG, String.format("mOnTimeSetListener set to %s", mOnTimeSetListener));

        return new android.app.TimePickerDialog(requireActivity(), mOnTimeSetListener,
                initialTime.getHour(), initialTime.getMinute(),
                DateFormat.is24HourFormat(requireContext()));
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        mOnTimeSetListener.onTimePickerCancel();
    }


}
