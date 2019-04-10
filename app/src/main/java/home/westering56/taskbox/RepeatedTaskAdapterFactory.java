package home.westering56.taskbox;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import home.westering56.taskbox.widget.CustomSpinnerAdapter;

public class RepeatedTaskAdapterFactory {

    private static final RepetitionOption NO_REPEAT = new RepetitionOption("Doesn't repeat", null);
    private static final RepetitionOption DAILY_REPEAT = new RepetitionOption("Daily", new RecurrenceRule(Freq.DAILY));
    private static final RepetitionOption WEEKLY_REPEAT = new RepetitionOption("Weekly", new RecurrenceRule(Freq.WEEKLY));
    private static final RepetitionOption MONTHLY_REPEAT = new RepetitionOption("Monthly", new RecurrenceRule(Freq.MONTHLY));
    private static final RepetitionOption YEARLY_REPEAT = new RepetitionOption("Yearly", new RecurrenceRule(Freq.YEARLY));

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
            return isRuleEqual(this.mRule, that.mRule);
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

    public static SpinnerAdapter buildAdapter(@NonNull Context context) {
        ArrayAdapter<RepetitionOption> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // TODO: Extract strings into resource file
        adapter.add(NO_REPEAT);
        adapter.add(DAILY_REPEAT);
        adapter.add(WEEKLY_REPEAT);
        adapter.add(MONTHLY_REPEAT);
        adapter.add(YEARLY_REPEAT);
        return new CustomSpinnerAdapter(context, adapter, android.R.layout.simple_spinner_item,
                android.R.layout.simple_spinner_dropdown_item);
    }

    /**
     * Find the position of the entry which contains the specified recurrence rule.
     * A rule of 'null' will find the 'Doesn't repeat' entry.
     * If rule is not found in the list, inserts it at position 0 as a custom rule, replacing what's there
     */
    public static int getPositionForRuleOrCreateCustomEntry(@NonNull CustomSpinnerAdapter adapter, @Nullable RecurrenceRule rule) {
        RepetitionOption target = new RepetitionOption("Unchecked", rule);
        final int pos = adapter.positionOf(target);
        if (pos != -1) return pos;
        // rule was not found, so it must be cs custom. (re)set it if it's not there
        adapter.setCustomValue(new RepetitionOption("Custom: " + rule.toString(), rule));
        return adapter.getCustomValuePosition();
    }

    /**
     * Removes the 'Existing custom rule' entry from the top of the list of data managed by the adapter
     * if it doesn't match the currently selected rule.
     */
    public static boolean removeCustomEntryIfNotSelected(CustomSpinnerAdapter adapter, @Nullable RecurrenceRule selectedRule) {
        if (adapter.hasCustomValue() && !isRuleEqual(((RepetitionOption)adapter.getCustomValue()).mRule, selectedRule)) {
            adapter.clearCustomValue();
            return true;
        }
        return false;
    }

    /**
     * Compare two @{@link org.dmfs.rfc5545.recur.RecurrenceRule} objects for equality by comparing
     * their @{@link RecurrenceRule#toString()} values.
     */
    private static boolean isRuleEqual(@Nullable final RecurrenceRule a, @Nullable final RecurrenceRule b) {
        if (a == null) return b == null;
        if (b == null) return false; // we know that a != null because of the previous line
        return a.toString().equals(b.toString());
    }
}
