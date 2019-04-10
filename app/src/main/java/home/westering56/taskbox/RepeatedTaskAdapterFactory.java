package home.westering56.taskbox;

import android.content.Context;
import android.widget.ArrayAdapter;

import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.util.Objects;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import home.westering56.taskbox.widget.CustomSpinnerAdapter;

public class RepeatedTaskAdapterFactory {

    public static class RepetitionOption {
        final String mLabel;
        final RecurrenceRule mRule;

        RepetitionOption(@NonNull String label, @Nullable RecurrenceRule rule) {
            this.mLabel = label;
            this.mRule = rule;
        }

        // Creates a dummy object that can be used to compare recurrence rules
        public static RepetitionOption buildDummyForRule(@Nullable RecurrenceRule rule) {
            return new RepetitionOption("Dummy", rule);
        }

        // Creates a object used to represent a custom recurrence rule
        public static RepetitionOption buildCustomForRule(@NonNull RecurrenceRule rule) {
            return new RepetitionOption("Custom: " + rule.toString(), rule);
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

        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return mLabel;
        }

        @Nullable
        public RecurrenceRule getRule() {
            return mRule;
        }
    }

    public static CustomSpinnerAdapter buildAdapter(@NonNull Context context) {
        @LayoutRes final int itemResource = android.R.layout.simple_spinner_item;
        @LayoutRes final int dropDownResource = android.R.layout.simple_spinner_dropdown_item;
        ArrayAdapter<RepetitionOption> adapter = new ArrayAdapter<>(context, itemResource);
        adapter.setDropDownViewResource(dropDownResource);

        adapter.add(new RepetitionOption(context.getString(R.string.repeat_option_doesnt_repeat), null));
        adapter.add(new RepetitionOption(context.getString(R.string.repeat_option_daily), new RecurrenceRule(Freq.DAILY)));
        adapter.add(new RepetitionOption(context.getString(R.string.repeat_option_weekly), new RecurrenceRule(Freq.WEEKLY)));
        adapter.add(new RepetitionOption(context.getString(R.string.repeat_option_monthly), new RecurrenceRule(Freq.MONTHLY)));
        adapter.add(new RepetitionOption(context.getString(R.string.repeat_option_yearly), new RecurrenceRule(Freq.YEARLY)));

        return new CustomSpinnerAdapter(context, adapter, itemResource, dropDownResource);
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
