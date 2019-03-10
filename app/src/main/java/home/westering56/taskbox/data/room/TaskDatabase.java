package home.westering56.taskbox.data.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Task.class}, version = 4)
@TypeConverters({Converters.class})
public abstract class TaskDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();

    private static TaskDatabase instance;
    public static TaskDatabase getDatabase(@NonNull Context appContext) {
        if (instance == null) {
            instance = Room.databaseBuilder(appContext, TaskDatabase.class, "tasks.db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE task ADD COLUMN snooze_until INTEGER");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE task ADD COLUMN status INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.beginTransaction();
            try {
                database.execSQL("CREATE TEMPORARY TABLE task_backup(_id, summary, snooze_until)");
                database.execSQL("INSERT INTO task_backup SELECT _id, summary, snooze_until from task");
                database.execSQL("DROP TABLE task");
                database.execSQL("CREATE TABLE task (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `summary` TEXT, `snooze_until` INTEGER, `done_at` INTEGER)");
                database.execSQL("INSERT INTO task (_id, summary, snooze_until) SELECT _id, summary, snooze_until FROM task_backup");
                database.execSQL("DROP TABLE task_backup");
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
    };
}
