package home.westering56.taskbox.room;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TaskDao {
    @Query("SELECT * from task")
    LiveData<List<Task>> loadAllTasks();

//    @Query("SELECT * from task where showAfter <= :now and isDone=0")
//    LiveData<List<Task>> loadCurrentTasks(Date now);
//
//    @Query("SELECT * from task where showAfter > :now and isDone=0")
//    LiveData<List<Task>> loadSnoozedTasks(Date now);
//
//    @Query("SELECT * from task where isDone=1")
//    LiveData<List<Task>> loadDoneTasks();

    @Insert
    void insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Update
    void updateTasks(Task... tasks);

    @Delete
    void deleteTask(Task task);

    @Delete
    void deleteTasks(Task... tasks);

    @Query("DELETE from task")
    void deleteAllTasks();
}
