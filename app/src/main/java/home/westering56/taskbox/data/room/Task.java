package home.westering56.taskbox.data.room;

import java.time.Instant;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Task {

    public Task() {
        this.doneAt = null;
        this.snoozeUntil = null;
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

    @ColumnInfo(name = "snooze_until")
    public Instant snoozeUntil;

    @ColumnInfo(name = "done_at")
    public Instant doneAt;


    public boolean isSnoozed() {
        return (snoozeUntil != null) && Instant.now().isBefore(snoozeUntil);
    }

    public boolean isActive() {
        return (doneAt == null) && ((snoozeUntil == null) || Instant.now().isAfter(snoozeUntil));
    }

    public boolean isDone() {
        return doneAt != null;
    }

    public void actionDone() {
        doneAt = Instant.now();
        // no need to mess with snooze timings
    }

    public void actionReactivate() {
        // Clear the snooze on (re)activate, since that's what Inbox does.
        // in fact, this is essentially 'back to inbox' and acts as 'un-snooze'
        doneAt = null;
        snoozeUntil = null;
    }

    public void actionSnooze(Instant until) {
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
}
