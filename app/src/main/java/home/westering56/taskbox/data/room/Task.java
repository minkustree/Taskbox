package home.westering56.taskbox.data.room;

import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.time.Instant;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import home.westering56.taskbox.TaskHelper;

@Entity
public class Task {

    public Task() {
        this.doneAt = null;
        this.snoozeUntil = null;
        this.rrule = null;
    }

    @Ignore
    public Task(@NonNull CharSequence summary) {
        this();
        this.summary = summary.toString();
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public int uid;

    public String summary;

    @SuppressWarnings("WeakerAccess")
    @ColumnInfo(name = "snooze_until")
    public Instant snoozeUntil;

    @SuppressWarnings("WeakerAccess")
    @ColumnInfo(name = "done_at")
    public Instant doneAt;

    public RecurrenceRule rrule;

    public boolean isSnoozed() {
        return (snoozeUntil != null) && Instant.now().isBefore(snoozeUntil);
    }

// --Commented out by Inspection START (23/03/2019 11:33 AM):
//    public boolean isActive() {
//        return (doneAt == null) && ((snoozeUntil == null) || Instant.now().isAfter(snoozeUntil));
//    }
// --Commented out by Inspection STOP (23/03/2019 11:33 AM)

    public boolean isDone() {
        return doneAt != null;
    }

    public boolean isRepeating() {
        return rrule != null;
    }

    public void done() {
        Instant nextSnooze = TaskHelper.nextSnoozeUntilTime(this);
        if (nextSnooze == null) {
            markDone();
        } else {
            markSnoozed(nextSnooze); // keep repeat rule
        }
    }

    private void markDone() {
        doneAt = Instant.now();
        // no need to mess with snooze timings or recurrence rules, doneAt means done. Period.
    }

    public void reactivate() {
        // Clear the snooze on (re)activate, since that's what Inbox does.
        // in fact, this is essentially 'back to inbox' and acts as 'un-snooze'
        doneAt = null;
        snoozeUntil = null;
        /* What does reactivating a repeatedly snoozing task look like?
         * Does it just reset it to a normal task, and then the user must select the snooze time
         * & repeat pattern again? Actually, That's not a bad place to start. */
        rrule = null;
    }

    /** Snooze and clear repeat rule */
    public void snooze(Instant until) {
        markSnoozed(until);
        rrule = null; // assumes no repeat
    }

    /** Snooze and set repeat rule */
    public void snoozeAndRepeat(Instant initialUntil, RecurrenceRule rule) {
        doneAt = null;
        snoozeUntil = initialUntil;
        rrule = rule;
    }

    /** Mark as snoozed without affecting repeat rule */
    private void markSnoozed(Instant until) {
        // Call this when isDone to reactive and re-snooze
        doneAt = null;
        snoozeUntil = until;
    }

    /*
    A task starts off active.
    doneAt == null
    snoozeUntil == null

    It can be marked as done:
    doneAt = Date
    snoozeUntil == <don't care>

    It can be done & snoozed => done
    doneAt = Date
    snoozeUntil <set> (but value doesn't matter)

    It can be snoozed:
    doneAt == null
    snoozeUntil = <set>

    A snoozed task can become active when now is after snooze_until time

    doneAt == null
    Now().isAfter(snoozeUntil)

    A snoozed task remains snoozed when now is before the snoozeUntil time.

    doneAt == null
    Now().isBefore(snoozeUntil)
     */

    /*
     * To handle repeating tasks, we store a recurrence rule 'rrule'.
     * If rrule is non-null, then the task repeats.
     * You can only set a task as repeating by the snooze UI, so it must snooze at least once
     * before repeating.
     *
     * Marking a repeating task as 'done' will snooze it to the next repeat time.
     * The snoozeUntil time will be updated to reflect the next time it needs to wake.
     */
}
