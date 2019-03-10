package home.westering56.taskbox;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.util.Supplier;
import home.westering56.taskbox.data.room.Task;
import home.westering56.taskbox.data.room.TaskDatabase;

public class TaskData {

    private static TaskData instance;

    private final TaskCursorAdapter activeTaskAdapter;
    private final TaskCursorAdapter doneTaskAdapter;
    private final TaskCursorAdapter snoozedTaskAdapter;
    private final TaskDatabase taskDatabase;

    private final List<TaskCursorAdapter> adapters = new ArrayList<>();

    static class TaskCursorAdapter extends SimpleCursorAdapter {
        private final Supplier<Cursor> cursorSupplier;

        TaskCursorAdapter(Context appContext, Supplier<Cursor> cursorSupplier) {
            super(appContext, android.R.layout.simple_list_item_2, cursorSupplier.get(),
                    new String[] {"summary", "snooze_until"},
                    new int[] { android.R.id.text1, android.R.id.text2 },
                    0);
            this.cursorSupplier = cursorSupplier;
            setViewBinder(new SnoozeFormattingViewBinder());
        }

        void sync() {
            swapCursor(cursorSupplier.get());
        }
    }

    private static class SnoozeFormattingViewBinder implements SimpleCursorAdapter.ViewBinder {
        private final DateTimeFormatter dtf = DateTimeFormatter
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

    private TaskData(@NonNull Context appContext) {
        taskDatabase = TaskDatabase.getDatabase(appContext);
        activeTaskAdapter = new TaskCursorAdapter(appContext, new Supplier<Cursor>() {
            @Override
            public Cursor get() {
                return taskDatabase.taskDao().loadAllActive();
            }
        });
        adapters.add(activeTaskAdapter);
        doneTaskAdapter = new TaskCursorAdapter(appContext, new Supplier<Cursor>() {
            @Override
            public Cursor get() {
                return taskDatabase.taskDao().loadAllDone();
            }
        });
        adapters.add(doneTaskAdapter);
        snoozedTaskAdapter= new TaskCursorAdapter(appContext, new Supplier<Cursor>() {
            @Override
            public Cursor get() {
                return taskDatabase.taskDao().loadAllSnoozed();
            }
        });
        adapters.add(snoozedTaskAdapter);
    }

    public static TaskData getInstance(@NonNull Context appContext) {
        synchronized (TaskData.class) {
            if (instance == null) {
                instance = new TaskData(appContext);
            }
            return instance;
        }
    }

    public ListAdapter getActiveTaskAdapter() {
        return activeTaskAdapter;
    }
    public ListAdapter getDoneTaskAdapter() {
        return doneTaskAdapter;
    }
    public ListAdapter getSnoozedTaskAdapter() {
        return snoozedTaskAdapter;
    }

    public void addSampleData(Context appContext) {
        String[] sampleTasks = appContext.getResources().getStringArray(R.array.sampleTasks);
        for (String t : sampleTasks) {
            taskDatabase.taskDao().insert(new Task(t));
        }
        syncAdapters();
    }

    public void add(CharSequence taskSummary) {
        taskDatabase.taskDao().insert(new Task(taskSummary));
        syncAdapters();
    }

    public Task getTask(long id) {
        return taskDatabase.taskDao().get(id);
    }

    public void updateTask(Task task) {
        taskDatabase.taskDao().update(task);
        syncAdapters();
    }

    public void deleteTask(Task task) {
        taskDatabase.taskDao().delete(task);
        syncAdapters();
    }

    public void deleteAllTasks() {
        taskDatabase.clearAllTables();
        syncAdapters();
    }

    /**
     * Refresh the contents of all adapters managed by this class. Call this after making any
     * changes to the underlying data, e.g. via the DAO.
     */
    private void syncAdapters() {
        for (TaskCursorAdapter adapter : adapters) {
            adapter.sync();
        }
    }
}

