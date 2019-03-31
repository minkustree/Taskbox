package home.westering56.taskbox;

import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;

import java.time.Instant;
import java.util.TimeZone;

import home.westering56.taskbox.data.room.Task;

public class TaskHelper {

    /** @return the next snooze time selected for a repeating task, or null if the task either does
     *          not repeat or no further repeats are called for by the recurrence rule.
     */
    public static Instant nextSnoozeUntilTime(Task repeatingTask) {
        if (!repeatingTask.isRepeating()) return null;
        // TODO: This assumes snoozeUntil was in TimeZone.getDefault(). Do we care?
        RecurrenceRuleIterator it = repeatingTask.rrule.iterator(
                repeatingTask.snoozeUntil.toEpochMilli(), TimeZone.getDefault());
        // Get the next snooze time that has yet to pass
        long nextSnoozeMilli;
        do {
            if (!it.hasNext()) return null; // don't snooze again
            nextSnoozeMilli = it.nextMillis();
        } while (nextSnoozeMilli <= Instant.now().toEpochMilli());
        return Instant.ofEpochMilli(nextSnoozeMilli);
    }

}
