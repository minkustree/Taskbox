package home.westering56.taskbox;

import android.content.Context;
import android.widget.Adapter;
import android.widget.ArrayAdapter;

import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RepeatedTaskAdapterFactory {

    private static final RepetitionOption NO_REPEAT = new RepetitionOption("Doesn't repeat", null);
    private static final RepetitionOption DAILY_REPEAT = new RepetitionOption("Daily", new RecurrenceRule(Freq.DAILY));
    private static final RepetitionOption WEEKLY_REPEAT = new RepetitionOption("Weekly", new RecurrenceRule(Freq.WEEKLY));
    private static final RepetitionOption MONTHLY_REPEAT = new RepetitionOption("Monthly", new RecurrenceRule(Freq.MONTHLY));
    private static final RepetitionOption YEARLY_REPEAT = new RepetitionOption("Yearly", new RecurrenceRule(Freq.YEARLY));
    private static final RepetitionOption PICK_CUSTOM_REPEAT = new RepetitionOption("Custom...", new RecurrenceRule(Freq.SECONDLY)); // sentinel RecurrenceRule value

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

    public static ArrayAdapter<RepetitionOption> buildAdapter(Context context) {
        ArrayAdapter<RepetitionOption> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // TODO: Extract strings into resource file
        adapter.add(NO_REPEAT);
        adapter.add(DAILY_REPEAT);
        adapter.add(WEEKLY_REPEAT);
        adapter.add(MONTHLY_REPEAT);
        adapter.add(YEARLY_REPEAT);
        // always at the end of the list - if not, change getPositionForCustomPicker()
        adapter.add(PICK_CUSTOM_REPEAT);
        return adapter;
    }

    /**
     * Find the position of the entry which contains the specified recurrence rule.
     * A rule of 'null' will find the 'Doesn't repeat' entry.
     * If rule is not found in the list, inserts it at position 0 as a custom rule, replacing what's there
     *
     */
    public static int getPositionForRuleOrCreateCustomEntry(@NonNull Adapter adapter, @Nullable RecurrenceRule rule) {
        RepetitionOption target = new RepetitionOption("Unchecked", rule);
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(target)) {
                return i;
            }
        }
        // rule was not found, so let's assume it's custom. (re)set it if it's not there
        //noinspection unchecked
        createOrUpdateCustomEntry((ArrayAdapter<RepetitionOption>) adapter, rule);
        return 0;
    }

    public static int getPositionForCustomPicker(@NonNull Adapter adapter) {
        final int pos = adapter.getCount() - 1;
        assert adapter.getItem(pos) == PICK_CUSTOM_REPEAT;
        return pos; // assumes that custom is always at the end of the list
    }

    private static void createOrUpdateCustomEntry(ArrayAdapter<RepetitionOption> adapter, RecurrenceRule rule) {
        adapter.setNotifyOnChange(false);
        clearCustomEntry(adapter);
        adapter.insert(new RepetitionOption("Custom: " + rule.toString(), rule), 0);
        adapter.notifyDataSetChanged(); // restores setNotifyOnChange to true
    }

    private static void clearCustomEntry(ArrayAdapter<RepetitionOption> adapter) {
        RepetitionOption firstOption = adapter.getItem(0);
        assert firstOption != null;
        if (firstOption.equals(NO_REPEAT)) return; // no custom entry to clear
        adapter.remove(firstOption); // remove the first entry
    }

    /**
     * Removes the 'Existing custom rule' entry from the top of the list of data managed by the adapter
     * if it doesn't match the currently selected rule.
     */
    public static boolean removeCustomEntryIfNotSelected(ArrayAdapter<RepetitionOption> adapter, @Nullable RecurrenceRule selectedRule) {
        RepetitionOption firstOption = adapter.getItem(0);
        assert firstOption != null;
        if (firstOption.equals(NO_REPEAT)) return false; // there's no custom entry to remove
        if (isRuleEqual(selectedRule, firstOption.mRule)) return false; // the custom entry is the selected entry, ignore
        adapter.remove(firstOption);
        return true;
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
