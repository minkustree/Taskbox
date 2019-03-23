package home.westering56.taskbox;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import java.time.LocalDateTime;

import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

@ContentView(R.layout.snooze_dialog)
public class SnoozeDialogFragment extends DialogFragment {
    private static final String TAG = "SnoozeDialog";
    private SnoozeOptionListener mSnoozeOptionListener;

    public interface SnoozeOptionListener {
        void onSnoozeOptionSelected(LocalDateTime snoozeUntil);
    }

    public static SnoozeDialogFragment newInstance(SnoozeOptionListener snoozeOptionListener) {
        SnoozeDialogFragment fragment = new SnoozeDialogFragment();
        fragment.setSnoozeOptionListener(snoozeOptionListener);
        return fragment;
    }

    @SuppressWarnings("WeakerAccess")
    public void setSnoozeOptionListener(SnoozeOptionListener listener) {
        mSnoozeOptionListener = listener;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");

        final GridView content = view.findViewById(R.id.snooze_dialog_content);
        content.setAdapter(SnoozeOptionProvider.getInstance().newAdapter(requireContext()));
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
        CustomSnoozeTimeDialogFragment customSnoozeTimeDialogFragment = CustomSnoozeTimeDialogFragment.newInstance(mSnoozeOptionListener);
        // TODO: Consider whether user should be able to go back to this dialog or not
        assert getFragmentManager() != null;
        customSnoozeTimeDialogFragment.show(getFragmentManager(), "snooze_custom");
    }
}
