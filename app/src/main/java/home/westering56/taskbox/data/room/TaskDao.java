package home.westering56.taskbox.data.room;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TaskDao {
//    @Query("SELECT * FROM task")
//    List<Task> getAll();

    @Query("SELECT * FROM task")
    Cursor loadAll();

//    @Query("SELECT * FROM task")
//    LiveData<List<Task>> asyncGetAll();

    @Insert
    void insert(Task task);

    @Query("SELECT * FROM task WHERE _id = :id LIMIT 1")
    Task get(long id);

    @Update
    void update(Task task);
}
