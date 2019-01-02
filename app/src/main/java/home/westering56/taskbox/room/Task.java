package home.westering56.taskbox.room;

import java.util.Date;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int uid;
    public String summary;
//    public boolean isDone;
    /* TODO: Switch Date to something that is TZ aware, and do Room DB version migration */
//    public Date showAfter;

    public Task(String summary) {
        this.summary = summary;
    }
}
