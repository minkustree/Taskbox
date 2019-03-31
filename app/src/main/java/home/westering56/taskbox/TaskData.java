package home.westering56.taskbox;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Supplier;
import home.westering56.taskbox.data.room.Task;
import home.westering56.taskbox.data.room.TaskDatabase;

public class TaskData {
    private static final String TAG = "TaskData";

    private static TaskData sInstance;

    private static class UndoBuffer {
        private @Nullable Task mOldTask;
        private @Nullable Task mNewTask;

        UndoBuffer() {
            clear();
        }

        void storeDelete(@NonNull Task oldTask) {
            mOldTask = oldTask;
            mNewTask = null;
        }

        void storeUpdate(@NonNull Task oldTask, @NonNull Task newTask) {
            mOldTask = oldTask;
            mNewTask = newTask;
        }

        boolean isDelete() {
            return mNewTask == null && mOldTask != null;
        }

        boolean isUpdate() {
            return mNewTask != null && mOldTask != null;
        }

        boolean isEmpty() {
            return  mNewTask == null && mOldTask == null;
        }

        void clear() {
            mOldTask = mNewTask = null;
        }
    }

    private final DataSetObservable mDataSetObservable = new DataSetObservable();
    private final TaskCursorAdapter activeTaskAdapter;
    private final TaskCursorAdapter doneTaskAdapter;
    private final TaskCursorAdapter snoozedTaskAdapter;
    private final TaskDatabase taskDatabase;
    private final UndoBuffer mUndoBuffer;

    @SuppressWarnings("WeakerAccess")
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

//    public void unregisterDataSetObserver(DataSetObserver observer) {
//        mDataSetObservable.unregisterObserver(observer);
//    }

    /**
     * Call when the underlying data set has changed, to call each registered {@link DataSetObserver}.
     * Called automatically when destructive operations happen.
     */
    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    static class TaskCursorAdapter extends SimpleCursorAdapter {
        private final Supplier<Cursor> mCursorSupplier;
        private final DataSetObserver mTaskDataObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                swapCursor(mCursorSupplier.get());
            }
        };

        TaskCursorAdapter(Context appContext, int layout, Supplier<Cursor> cursorSupplier, String[] from, int[] to) {
            super(appContext, layout, cursorSupplier.get(), from, to, 0);
            this.mCursorSupplier = cursorSupplier;
        }

        DataSetObserver getTaskDataObserver() {
            return mTaskDataObserver;
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
                    textValue = "Snoozed until " + SnoozeTimeFormatter.formatInstant(view.getContext(),
                            Instant.ofEpochMilli(cursor.getLong(columnIndex)));
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
        mUndoBuffer = new UndoBuffer();
        taskDatabase = TaskDatabase.getDatabase(appContext);

        // Set up snoozeDataAdapter for active tasks
        activeTaskAdapter = new TaskCursorAdapter(appContext, android.R.layout.simple_list_item_1, new Supplier<Cursor>() {

            @Override
            public Cursor get() {
                return taskDatabase.taskDao().loadAllActive();
            }
        }, new String[] {"summary"}, new int[] {android.R.id.text1});
        registerDataSetObserver(activeTaskAdapter.getTaskDataObserver());

        // Set up snoozeDataAdapter for done tasks
        doneTaskAdapter = new TaskCursorAdapter(appContext, android.R.layout.simple_list_item_2, new Supplier<Cursor>() {

            @Override
            public Cursor get() {
                return taskDatabase.taskDao().loadAllDone();
            }
        }, new String[] {"summary", "done_at"}, new int[] {android.R.id.text1, android.R.id.text2});
        doneTaskAdapter.setViewBinder(new DoneFormattingViewBinder());
        registerDataSetObserver(doneTaskAdapter.getTaskDataObserver());

        // Set up snoozeDataAdapter for snoozed tasks
        snoozedTaskAdapter = new TaskCursorAdapter(appContext, android.R.layout.simple_list_item_2, new Supplier<Cursor>() {

            @Override
            public Cursor get() {
                return taskDatabase.taskDao().loadAllSnoozed();
            }
        }, new String[] {"summary", "snooze_until"}, new int[] {android.R.id.text1, android.R.id.text2});
        snoozedTaskAdapter.setViewBinder(new SnoozeFormattingViewBinder());
        registerDataSetObserver(snoozedTaskAdapter.getTaskDataObserver());

        // Schedule next notification wakeup each time the data set changes
        registerDataSetObserver(SnoozeNotificationManager.newTaskDataObserver(appContext));
    }

    public static TaskData getInstance(@NonNull Context context) {
        synchronized (TaskData.class) {
            if (sInstance == null) {
                sInstance = new TaskData(context.getApplicationContext());
            }
            return sInstance;
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
        notifyDataSetChanged();
    }

    public void add(CharSequence taskSummary) {
        mUndoBuffer.clear(); // can't undo this
        taskDatabase.taskDao().insert(new Task(taskSummary));
        notifyDataSetChanged();
    }

    public Task getTask(int id) {
        return taskDatabase.taskDao().get(id);
    }

    public void updateTask(Task task) {
        Task oldTask = taskDatabase.taskDao().get(task.uid);
        mUndoBuffer.storeUpdate(oldTask, task);
        taskDatabase.taskDao().update(task);
        notifyDataSetChanged();
    }

    public void deleteTask(Task task) {
        mUndoBuffer.storeDelete(task);
        taskDatabase.taskDao().delete(task);
        notifyDataSetChanged();
    }

    public void deleteAllTasks() {
        mUndoBuffer.clear(); // cannot undo delete all
        taskDatabase.clearAllTables();
        notifyDataSetChanged();
    }

    public void undoLast() {
        if (mUndoBuffer.isEmpty()) {
            Log.w(TAG, "Undo requested, but nothing in undo buffer");
            return;
        }
        if (mUndoBuffer.isDelete()) {
            // undo a delete
            Log.d(TAG, "Undoing delete");
            taskDatabase.taskDao().insert(mUndoBuffer.mOldTask); // fall through
        }
        if (mUndoBuffer.isUpdate()) {
            // undo a modification
            Log.d(TAG, "Undoing update");
            taskDatabase.taskDao().update(mUndoBuffer.mOldTask); // fall through
        }
        // Either we undid it, or it was an odd state. Either way, clear the buffer
        mUndoBuffer.clear();
        notifyDataSetChanged();
    }

    /**
     * The @{@link Instant} at which the next snoozed task is scheduled to wake.
     */
    public Instant getNextWakeupInstant() {
        return taskDatabase.taskDao().getNextWakeupDue();
    }

    /**
     * Fetch the tasks that have become active during the time between the two specified instants.
     * @param now inclusive
     */
    public List<Task> getNewlyActiveTasks(Instant lastChecked, Instant now) {
        Log.d(TAG, "Fetching tasks that un-snoozed between " + lastChecked + " and " + now);
        return taskDatabase.taskDao().getNewlyActiveTasks(lastChecked, now);
    }

}

