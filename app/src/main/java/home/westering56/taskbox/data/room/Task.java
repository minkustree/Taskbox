package home.westering56.taskbox.data.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Task {

    public Task(String summary) {
        this.summary = summary;
    }

    public Task(CharSequence summary) {
        this(summary.toString());
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public int uid;

    public String summary;
}
