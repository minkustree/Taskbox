package home.westering56.taskbox.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;

import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.time.LocalDateTime;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import home.westering56.taskbox.R;
import home.westering56.taskbox.SnoozeOptionProvider;

import static home.westering56.taskbox.MainActivity.EXTRA_TASK_ID;

public class SnoozeOptionsDialogFragment extends DialogFragment {
    private static final String TAG = "SnoozeDialog";
    private SnoozeOptionListener mSnoozeOptionListener;
    private int mTaskId;

    public interface SnoozeOptionListener {
        /**
         * @param snoozeUntil the chosen time until which the task will snooze
         * @param rule {@link RecurrenceRule} that describes how this task repeats after snoozing,
         *                                   or null if the task does not repeat
         */
        void onSnoozeOptionSelected(LocalDateTime snoozeUntil, RecurrenceRule rule);
    }

    /**
     * @param taskId @{@link home.westering56.taskbox.data.room.Task#uid} of the task we're snoozing, or -1 if no task is stored yet.
     */
    public static SnoozeOptionsDialogFragment newInstance(SnoozeOptionListener snoozeOptionListener, int taskId) {
        SnoozeOptionsDialogFragment fragment = new SnoozeOptionsDialogFragment();
        fragment.setSnoozeOptionListener(snoozeOptionListener);
        Bundle args = new Bundle();
        args.putInt(EXTRA_TASK_ID, taskId);
        fragment.setArguments(args);
        return fragment;
    }

    private void setSnoozeOptionListener(SnoozeOptionListener listener) {
        mSnoozeOptionListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.snooze_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");

        Bundle args = getArguments();
        if (args != null && args.containsKey(EXTRA_TASK_ID)) {
            mTaskId = args.getInt(EXTRA_TASK_ID);
        }

        final GridView content = view.findViewById(R.id.snooze_dialog_content);
        content.setAdapter(SnoozeOptionProvider.newAdapter(requireContext()));
        content.setOnItemClickListener((parent, view1, position, id) -> {
            if (mSnoozeOptionListener != null) {
                mSnoozeOptionListener.onSnoozeOptionSelected(
                        SnoozeOptionProvider.getDateTimeAtPosition(parent, position), null);
            }
        });
        final Button custom = view.findViewById(R.id.snooze_dialog_button_custom);
        custom.setOnClickListener(v -> showCustomSnoozeTimeDialog());
    }

    private void showCustomSnoozeTimeDialog() {
        CustomSnoozeOptionsDialog customSnoozeOptionsDialog = CustomSnoozeOptionsDialog.newInstance(mSnoozeOptionListener, mTaskId);
        // TODO: Consider whether user should be able to go back to this dialog or not
        customSnoozeOptionsDialog.show(Objects.requireNonNull(getFragmentManager()), CustomSnoozeOptionsDialog.FRAGMENT_TAG);
    }
}
