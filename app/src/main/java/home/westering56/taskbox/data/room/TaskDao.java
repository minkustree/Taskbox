package home.westering56.taskbox.data.room;

import android.database.Cursor;

import java.time.Instant;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public abstract class TaskDao {

    // TODO: There's probably a better timestamp to use for ordering things, but this does for now

    public Cursor loadAllActive() { return loadAllActiveAt(Instant.now()); }

    @Query("SELECT * from task WHERE done_at IS NULL AND (snooze_until IS NULL OR :instant > snooze_until) ORDER BY snooze_until DESC")
    public abstract Cursor loadAllActiveAt(Instant instant);

    @Query("SELECT * from task WHERE done_at IS NOT NULL ORDER BY done_at DESC")
    public abstract Cursor loadAllDone();

    public Cursor loadAllSnoozed() { return loadAllSnoozedAt(Instant.now()); }

    @Query("SELECT * from task WHERE done_at IS NULL AND snooze_until > :instant ORDER BY snooze_until ASC")
    public abstract Cursor loadAllSnoozedAt(Instant instant);

    @Query("SELECT snooze_until from task WHERE done_at IS NULL AND snooze_until > :instant ORDER BY snooze_until ASC LIMIT 1")
    public abstract Instant getNextTaskToWakeupAfter(Instant instant);

    /** @return the {@link java.time.Instant} that the next snoozed task is due to wake, or null
     *  if there are no snoozed tasks still to wake.
     */
    public Instant getNextWakeupDue() { return getNextTaskToWakeupAfter(Instant.now()); }

    @Query("SELECT * FROM task WHERE _id = :id LIMIT 1")
    public abstract Task get(long id);

    @Insert
    public abstract void insert(Task task);

    @Update
    public abstract void update(Task task);

    @Delete
    public abstract void delete(Task task);

    @Query("SELECT * FROM task WHERE done_at IS NULL AND snooze_until > :fromExclusive AND snooze_until <= :toInclusive ORDER BY snooze_until ASC")
    public abstract List<Task> getNewlyActiveTasks(Instant fromExclusive, Instant toInclusive);
}
