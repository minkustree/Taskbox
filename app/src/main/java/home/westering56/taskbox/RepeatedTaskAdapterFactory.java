package home.westering56.taskbox;

import android.content.Context;
import android.widget.ArrayAdapter;

import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RepeatedTaskAdapterFactory {

    public static class RepetitionOption {
        final String mLabel;
        final RecurrenceRule mRule;

        RepetitionOption(@NonNull String label, @Nullable RecurrenceRule rule) {
            this.mLabel = label;
            this.mRule = rule;
        }

        @Override
        public String toString() {
            return mLabel;
        }

        @Nullable
        public RecurrenceRule getRule() {
            return mRule;
        }
    }

    public static ArrayAdapter<RepetitionOption> buildAdapter(Context context) {
        ArrayAdapter<RepetitionOption> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // TODO: Extract strings into resource file
        adapter.add(new RepetitionOption("Doesn't repeat", null));
        adapter.add(new RepetitionOption("Daily", new RecurrenceRule(Freq.DAILY)));
        adapter.add(new RepetitionOption("Weekly", new RecurrenceRule(Freq.WEEKLY)));
        adapter.add(new RepetitionOption("Monthly", new RecurrenceRule(Freq.MONTHLY)));
        adapter.add(new RepetitionOption("Yearly", new RecurrenceRule(Freq.YEARLY)));
        // TODO: Come back and add custom options here
        return adapter;
    }

}
