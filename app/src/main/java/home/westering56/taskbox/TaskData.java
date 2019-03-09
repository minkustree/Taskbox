package home.westering56.taskbox;

import android.content.Context;
import androidx.annotation.NonNull;


import android.database.Cursor;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import home.westering56.taskbox.data.room.Task;
import home.westering56.taskbox.data.room.TaskDatabase;

public class TaskData {

    private static TaskData instance;
    private final CursorAdapter ca;
    private final TaskDatabase taskDatabase;

    private TaskData(@NonNull Context appContext) {
        taskDatabase = TaskDatabase.getDatabase(appContext);
        ca = new SimpleCursorAdapter(appContext,
                android.R.layout.simple_list_item_2,
                taskDatabase.taskDao().loadAll(),
                new String[] {"summary", "snooze_until"},
                new int[] { android.R.id.text1, android.R.id.text2 },
                0);
        ((SimpleCursorAdapter) ca).setViewBinder(new SnoozeFormattingViewBinder());
    }

    private class SnoozeFormattingViewBinder implements SimpleCursorAdapter.ViewBinder {
        private DateTimeFormatter dtf = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.LONG)
                .withZone(ZoneId.systemDefault());

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (columnIndex == cursor.getColumnIndexOrThrow("snooze_until")) {
                final long snoozeUntilEpochMilli = cursor.getLong(columnIndex);
                final String textValue;
                if (snoozeUntilEpochMilli == 0) {
                    // no snooze data yet, don't expose a value, but to overwrite what might
                    // have been there before
                    textValue = "";
                } else {
                    textValue = "Snoozed until " + dtf.format(Instant.ofEpochMilli(snoozeUntilEpochMilli));
                }
                ((TextView) view).setText(textValue);
                return true;
            }
            return false;
        }
    }


    public static TaskData getInstance(@NonNull Context appContext) {
        synchronized (TaskData.class) {
            if (instance == null) {
                instance = new TaskData(appContext);
            }
            return instance;
        }
    }


    public ListAdapter getAdapter() {
        return ca;
    }

    public void addSampleData(Context appContext) {
        String[] sampleTasks = appContext.getResources().getStringArray(R.array.sampleTasks);
        for (String t : sampleTasks) {
            taskDatabase.taskDao().insert(new Task(t));
        }
        ca.swapCursor(taskDatabase.taskDao().loadAll());
    }

    public void add(CharSequence taskSummary) {
        taskDatabase.taskDao().insert(new Task(taskSummary));
        ca.swapCursor(taskDatabase.taskDao().loadAll());
    }

    public Task getTask(long id) {
        return taskDatabase.taskDao().get(id);
    }

    public void updateTask(Task task) {
        task.snoozeUntil = Instant.now();
        taskDatabase.taskDao().update(task);
        ca.swapCursor(taskDatabase.taskDao().loadAll());
    }

    public void deleteTask(Task task) {
        taskDatabase.taskDao().delete(task);
        ca.swapCursor(taskDatabase.taskDao().loadAll());
    }

    public void deleteAllTasks() {
        taskDatabase.clearAllTables();
        ca.swapCursor(taskDatabase.taskDao().loadAll());
    }
}

