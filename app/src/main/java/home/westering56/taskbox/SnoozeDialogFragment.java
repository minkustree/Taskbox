package home.westering56.taskbox;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.time.LocalDateTime;

import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import static home.westering56.taskbox.MainActivity.EXTRA_TASK_ID;

@ContentView(R.layout.snooze_dialog)
public class SnoozeDialogFragment extends DialogFragment {
    private static final String TAG = "SnoozeDialog";
    private SnoozeOptionListener mSnoozeOptionListener;
    private int mTaskId;

    public interface SnoozeOptionListener {
        void onSnoozeOptionSelected(LocalDateTime snoozeUntil);
        void onSnoozeOptionSelected(LocalDateTime snoozeUntil, RecurrenceRule rule);
    }

    /**
     * @param taskId @{@link home.westering56.taskbox.data.room.Task#uid} of the task we're snoozing, or -1 if no task is stored yet.
     */
    public static SnoozeDialogFragment newInstance(SnoozeOptionListener snoozeOptionListener, int taskId) {
        SnoozeDialogFragment fragment = new SnoozeDialogFragment();
        fragment.setSnoozeOptionListener(snoozeOptionListener);
        Bundle args = new Bundle();
        args.putInt(EXTRA_TASK_ID, taskId);
        fragment.setArguments(args);
        return fragment;
    }

    private void setSnoozeOptionListener(SnoozeOptionListener listener) {
        mSnoozeOptionListener = listener;
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
        content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mSnoozeOptionListener != null) {
                    mSnoozeOptionListener.onSnoozeOptionSelected(
                            SnoozeOptionProvider.getDateTimeAtPosition(parent, position));
                }
            }
        });
        final Button custom = view.findViewById(R.id.snooze_dialog_button_custom);
        custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomSnoozeTimeDialog();
            }
        });
    }

    private void showCustomSnoozeTimeDialog() {
        CustomSnoozeTimeDialogFragment customSnoozeTimeDialogFragment = CustomSnoozeTimeDialogFragment.newInstance(mSnoozeOptionListener, mTaskId);
        // TODO: Consider whether user should be able to go back to this dialog or not
        assert getFragmentManager() != null;
        customSnoozeTimeDialogFragment.show(getFragmentManager(), "snooze_custom");
    }
}
