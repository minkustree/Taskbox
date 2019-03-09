package home.westering56.taskbox.data.room;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public abstract class TaskDao {

    @Query("SELECT * from task WHERE status = :status")
    public abstract Cursor loadByStatus(int status);

    public Cursor loadAllActive() {
        return loadByStatus(Task.STATUS_ACTIVE);
    }

    public Cursor loadAllDone() {
        return loadByStatus(Task.STATUS_DONE);
    }

    @Query("SELECT * FROM task WHERE _id = :id LIMIT 1")
    public abstract Task get(long id);

    @Insert
    public abstract void insert(Task task);


    @Update
    public abstract void update(Task task);

    public @Delete
    abstract void delete(Task task);

}
