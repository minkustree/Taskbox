package home.westering56.taskbox.data.room;

import java.time.Instant;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Task {

    public static final int STATUS_ACTIVE = 0; // default value
    public static final int STATUS_DONE = 1;

    public Task(String summary) {
        this.summary = summary;
        this.status = STATUS_ACTIVE;
    }

    public Task(CharSequence summary) {
        this(summary.toString());
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public int uid;

    public String summary;

    @ColumnInfo(name = "snooze_until")
    public Instant snoozeUntil;

    public int status;
}
