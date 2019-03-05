package home.westering56.taskbox.data.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Task.class}, version = 1)
public abstract class TaskDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();

    private static TaskDatabase instance;
    public static TaskDatabase getDatabase(@NonNull Context appContext) {
        if (instance == null) {
            instance = Room.databaseBuilder(appContext, TaskDatabase.class, "tasks.db")
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
