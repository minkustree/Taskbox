package home.westering56.taskbox;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
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
    private static final String TAG = "TaskData";

    private static TaskData instance;

    private final TaskCursorAdapter activeTaskAdapter;
    private final TaskCursorAdapter doneTaskAdapter;
    private final TaskCursorAdapter snoozedTaskAdapter;
    private final TaskDatabase taskDatabase;

    private final List<TaskCursorAdapter> adapters = new ArrayList<>();

    static class TaskCursorAdapter extends SimpleCursorAdapter {
        private final Supplier<Cursor> cursorSupplier;

        TaskCursorAdapter(Context appContext, int layout, Supplier<Cursor> cursorSupplier, String[] from, int[] to) {
            super(appContext, layout, cursorSupplier.get(), from, to, 0);
            this.cursorSupplier = cursorSupplier;
        }

        void sync() {
            swapCursor(cursorSupplier.get());
        }
    }

    private static class SnoozeFormattingViewBinder implements SimpleCursorAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (columnIndex == cursor.getColumnIndexOrThrow("snooze_until")) {
                final String textValue;
                if (cursor.isNull(columnIndex)) {
                    // no snooze data yet, don't expose a value, but to overwrite what might
                    // have been displayed before
                    textValue = "";
                } else {
                    textValue = "Snoozed until " + SnoozeTimeFormatter.format(view.getContext(),
                            Instant.ofEpochMilli(cursor.getLong(columnIndex)).atZone(
                                    ZoneId.systemDefault()));
                }
                ((TextView) view).setText(textValue);
                return true;
            }
            return false;
        }
    }

    private static class DoneFormattingViewBinder implements SimpleCursorAdapter.ViewBinder {
        private final DateTimeFormatter dtf = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.LONG)
                .withZone(ZoneId.systemDefault());

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (columnIndex == cursor.getColumnIndexOrThrow("done_at")) {
                final String textValue;
                if (cursor.isNull(columnIndex)) { // unexpected?
                    // no snooze data yet, don't expose a value, but to overwrite what might
                    // have been displayed before
                    textValue = "";
                } else {
                    textValue = "Done at " + dtf.format(Instant.ofEpochMilli(cursor.getLong(columnIndex)));
                }
                ((TextView) view).setText(textValue);
                return true;
            }
            return false;
        }
    }

    private TaskData(@NonNull Context appContext) {
        taskDatabase = TaskDatabase.getDatabase(appContext);

        // Set up adapter for active tasks
        activeTaskAdapter = new TaskCursorAdapter(appContext, android.R.layout.simple_list_item_1, new Supplier<Cursor>() {

            @Override
            public Cursor get() {
                return taskDatabase.taskDao().loadAllActive();
            }
        }, new String[] {"summary"}, new int[] {android.R.id.text1});
        adapters.add(activeTaskAdapter);

        // Set up adapter for done tasks
        doneTaskAdapter = new TaskCursorAdapter(appContext, android.R.layout.simple_list_item_2, new Supplier<Cursor>() {

            @Override
            public Cursor get() {
                return taskDatabase.taskDao().loadAllDone();
            }
        }, new String[] {"summary", "done_at"}, new int[] {android.R.id.text1, android.R.id.text2});
        doneTaskAdapter.setViewBinder(new DoneFormattingViewBinder());
        adapters.add(doneTaskAdapter);

        // Set up adapter for snoozed tasks
        snoozedTaskAdapter = new TaskCursorAdapter(appContext, android.R.layout.simple_list_item_2, new Supplier<Cursor>() {

            @Override
            public Cursor get() {
                return taskDatabase.taskDao().loadAllSnoozed();
            }
        }, new String[] {"summary", "snooze_until"}, new int[] {android.R.id.text1, android.R.id.text2});
        snoozedTaskAdapter.setViewBinder(new SnoozeFormattingViewBinder());
        adapters.add(snoozedTaskAdapter);
    }

    public static TaskData getInstance(@NonNull Context context) {
        synchronized (TaskData.class) {
            if (instance == null) {
                instance = new TaskData(context.getApplicationContext());
            }
            return instance;
        }
    }

    public ListAdapter getActiveTaskAdapter() { return activeTaskAdapter; }
    public ListAdapter getDoneTaskAdapter() {
        return doneTaskAdapter;
    }
    public ListAdapter getSnoozedTaskAdapter() {
        return snoozedTaskAdapter;
    }

    public void addSampleData(Context context) {
        String[] sampleTasks = context.getApplicationContext().getResources().getStringArray(R.array.sampleTasks);
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
     * changes to the underlying data, e.g. via the DAO or to be sure that the model has the most
     * up to date information.
     */
    public void syncAdapters() {
        for (TaskCursorAdapter adapter : adapters) {
            adapter.sync();
        }
    }

    /**
     * Schedule a wakeup to check for newly active, previously snoozed tasks.
     * Usually scheduled for the time that the next snoozed task is due to become active.
     */
    public void scheduleNextUpdate(@NonNull Context context) {
        Instant nextWakeInstant = taskDatabase.taskDao().getNextWakeupDue();
        if (nextWakeInstant != null) {
            Log.d(TAG, "Scheduling next update check for " + nextWakeInstant.toString());
            final Context appContext = context.getApplicationContext();
            AlarmManager alarmManager = appContext.getSystemService(AlarmManager.class);
            Intent intent = new Intent(appContext, WokenTaskReceiver.class);
            // used to determine what became active between now and wakeup, for notification use
            intent.putExtra(WokenTaskReceiver.EXTRA_LAST_SEEN, Instant.now());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intent, 0);
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextWakeInstant.toEpochMilli(), pendingIntent);
        } else {
            Log.d(TAG, "No snoozed tasks, no update check scheduled");
        }
    }

    /**
     * Fetch the tasks that have become active between the two specified instants
     * @param lastSeen exclusive
     * @param now inclusive
     */
    public List<Task> getNewlyActiveTasks(Instant lastSeen, Instant now) {
        return taskDatabase.taskDao().getNewlyActiveTasks(lastSeen, now);
    }

}

