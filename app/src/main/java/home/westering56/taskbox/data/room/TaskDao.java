package home.westering56.taskbox.data.room;

import android.database.Cursor;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM task")
    List<Task> getAll();

    @Query("SELECT * FROM task")
    Cursor loadAll();

    @Query("SELECT * FROM task")
    LiveData<List<Task>> asyncGetAll();

    @Insert
    void insert(Task task);


}
