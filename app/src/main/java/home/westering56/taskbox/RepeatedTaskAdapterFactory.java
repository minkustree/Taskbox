package home.westering56.taskbox;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.util.Objects;

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

        /**
         * Two options are equal only if their mRule fields are equal. Label is ignored.
         */
        @Override
        public boolean equals(Object o) {
            // This allows ArrayAdapter.getPosition() to find based on recurrence rule only.
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RepetitionOption that = (RepetitionOption) o;
            // if either is null, they're equal only if the other is null too
            if (this.mRule == null) return that.mRule == null;
            if (that.mRule == null) return this.mRule == null;
            // if we get through all that, test for rule string equality
            return this.mRule.toString().equals(that.mRule.toString());
        }

        @Override
        public int hashCode() {
            return Objects.hash(mRule);
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

    /**
     * Find the position of the entry which contains the specified recurrence rule.
     * A rule of 'null' will find the 'Doesn't repeat' entry.
     *
     * @return -1 if the rule is not found in the specified adapter (e.g. custom)
     */
    public static int getPositionForRule(@NonNull SpinnerAdapter adapter, @Nullable RecurrenceRule rule) {
        RepetitionOption target = new RepetitionOption("Unchecked", rule);
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(target)) {
                return i;
            }
        }
        return -1; // not found
    }

}
