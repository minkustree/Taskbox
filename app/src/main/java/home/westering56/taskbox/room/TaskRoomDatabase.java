package home.westering56.taskbox.room;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Task.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class TaskRoomDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();

    private static volatile TaskRoomDatabase INSTANCE;

    private static RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new populateDbAsync(INSTANCE).execute();
        }
    };

    private static class populateDbAsync extends AsyncTask<Void, Void, Void> {
        private final TaskDao taskDao;

        populateDbAsync(TaskRoomDatabase db) {
            taskDao = db.taskDao();
        }

        @Override
        protected Void doInBackground(final Void... noop) {
            taskDao.deleteAllTasks();
            taskDao.insertTask(new Task("Take the bins out"));
            taskDao.insertTask(new Task("Prep some D&D"));
            taskDao.insertTask(new Task("Wrap presents"));
            return null;
        }
    }

    public static TaskRoomDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (TaskRoomDatabase.class) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        TaskRoomDatabase.class, "task_database.db")
                        .addCallback(roomDatabaseCallback)
                        .build();
            }
        }
        return INSTANCE;
    }

}
