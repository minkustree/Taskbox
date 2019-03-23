package home.westering56.taskbox;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;


@SuppressWarnings("WeakerAccess")
public class CustomSnoozeTimeDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = "CustomSnoozeDTDlg";

    static class SnoozeCustomModel {
        LocalDate mDate = null;
        // TODO: Replace with morning, afternoon, and evening picker
        // TODO: Add 'custom' to the above
        LocalTime mTime = LocalTime.of(9, 0);

        LocalDateTime toLocalDateTime() {
            // TODO: Guard against mDate and/or mTime being null
            return LocalDateTime.of(mDate, mTime);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        DatePickerDialog.OnDateSetListener mDateSetListener;

        void setOnDateSetListener(@Nullable DatePickerDialog.OnDateSetListener listener) {
            mDateSetListener = listener;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final DatePickerDialog d = new DatePickerDialog(requireActivity());
            d.setOnDateSetListener(mDateSetListener);
            return d;
        }

        @Override
        public void onDetach() {
            mDateSetListener = null;
            super.onDetach();
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            if (mDateSetListener == null) {
                Log.e(TAG, "No mDateSetListener set for handling onDateSet");
            } else {
                mDateSetListener.onDateSet(view, year, month, dayOfMonth);
            }
        }
    }

    private SnoozeCustomModel mModel;
    private SnoozeDialogFragment.SnoozeOptionListener mSnoozeOptionListener;
    private TextView mDateText;

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
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.setOnDateSetListener(CustomSnoozeTimeDialogFragment.this);
                assert getFragmentManager() != null;
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        Spinner timeSelector = view.findViewById(R.id.snooze_custom_time_selector);
        timeSelector.setAdapter(CustomSnoozeTimeProvider.getInstance().newAdapter(requireContext()));
        timeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onTimeItemSelected(parent, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op
            }
        });
        builder .setView(view)
                .setTitle(R.string.snooze_pick_date_time_title)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Save button clicked");
                        if (mSnoozeOptionListener != null) {
                            mSnoozeOptionListener.onSnoozeOptionSelected(mModel.toLocalDateTime());
                        }
                    }
                });
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUiFields();
    }

    public void setSnoozeOptionListener(@Nullable SnoozeDialogFragment.SnoozeOptionListener listener) {
        mSnoozeOptionListener = listener;
    }

    @Override
    public void onDestroy() {
        mSnoozeOptionListener = null;
        super.onDestroy();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int zeroBasedMonth, int dayOfMonth) {
        // month arrives zero-based for compatibility with old Calendar#MONTH. Adjust.
        final int month = zeroBasedMonth + 1;
        Log.d(TAG, String.format("Date picked: %4d-%02d-%2d", year, month, dayOfMonth));
        mModel.mDate = LocalDate.of(year, month, dayOfMonth);
        updateUiFields();
    }

    public void onTimeItemSelected(AdapterView<?> parent, int position) {
        mModel.mTime = CustomSnoozeTimeProvider.getLocalTimeAtPosition(parent, position);
        updateUiFields(); // not strictly necessary, but is a consistent pattern for future use
    }

    private void updateUiFields() {
        if (mModel.mDate != null) {
            mDateText.setText(SnoozeTimeFormatter.formatDate(getContext(), mModel.mDate));
        }
    }

}
