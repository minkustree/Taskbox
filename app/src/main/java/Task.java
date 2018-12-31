import java.util.Date;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Task {
    @PrimaryKey
    public int uid;
    public String summary;
    public boolean isDone;
    public Date showAfter;
}
