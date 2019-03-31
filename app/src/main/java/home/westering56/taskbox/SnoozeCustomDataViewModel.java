package home.westering56.taskbox;

import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import androidx.lifecycle.ViewModel;
import home.westering56.taskbox.data.room.Task;

public class SnoozeCustomDataViewModel extends ViewModel {
    public LocalDate mDate = null;
    public LocalTime mTime = null;
    public RecurrenceRule mRule = null;

    public int mLastTimeSelectedPosition = 0;

    private boolean mLoaded = false;

    /** If {@link #mDate} or {@link #mTime} are null, will throw a {@link NullPointerException} */
    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.of(mDate, mTime);
    }

    public void loadFrom(Task t) {
        if (!mLoaded) { // load once only
            /* Active tasks may still have snoozeUntil set from when they last woke.
               Use t.isSnoozed() over t.snoozeUntil != null to avoid restoring old snooze times. */
            if (t.isSnoozed()) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(t.snoozeUntil, ZoneId.systemDefault());
                mDate = localDateTime.toLocalDate();
                mTime = localDateTime.toLocalTime();
            }
            if (t.isRepeating()) {
                mRule = t.rrule;
            }
            mLoaded = true;
        }
    }
}
