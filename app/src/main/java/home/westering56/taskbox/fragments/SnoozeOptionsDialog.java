package home.westering56.taskbox.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.time.LocalDateTime;
import java.util.Objects;

import home.westering56.taskbox.R;
import home.westering56.taskbox.SnoozeOptionProvider;

public class SnoozeOptionsDialog extends DialogFragment implements SnoozeOptionListener {
    public static final String TAG = "SnoozeDialog";
    private SnoozeOptionListener mSnoozeOptionListener;

    public static SnoozeOptionsDialog newInstance() {
        return new SnoozeOptionsDialog();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mSnoozeOptionListener = (SnoozeOptionListener) context;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Containing Activity must implement SnoozeOptionListener");
        }
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

        final GridView content = view.findViewById(R.id.snooze_dialog_content);
        content.setAdapter(SnoozeOptionProvider.newAdapter(requireContext()));
        content.setOnItemClickListener((parent, view1, position, id) -> onSnoozeOptionSelected(
                SnoozeOptionProvider.getDateTimeAtPosition(parent, position), null));

        final Button custom = view.findViewById(R.id.snooze_dialog_button_custom);
        custom.setOnClickListener(v -> showCustomSnoozeTimeDialog());
    }

    private void showCustomSnoozeTimeDialog() {
        CustomSnoozeOptionsDialog customSnoozeOptionsDialog = CustomSnoozeOptionsDialog.newInstance();
        FragmentManager manager = Objects.requireNonNull(getFragmentManager());
        // replace this fragment with the new one, but put it on the back stack so user can back out
        manager.beginTransaction()
                .add(customSnoozeOptionsDialog, CustomSnoozeOptionsDialog.FRAGMENT_TAG)
                .addToBackStack("custom_snooze_options")
                .remove(this)
                .show(customSnoozeOptionsDialog)
                .commit();
    }

    @Override
    public void onSnoozeOptionSelected(LocalDateTime snoozeUntil, RecurrenceRule rule) {
        mSnoozeOptionListener.onSnoozeOptionSelected(snoozeUntil, rule);
        // remove this fragment, as it's job has been done
        Objects.requireNonNull(getFragmentManager()).beginTransaction().remove(this).commit();
    }

}
