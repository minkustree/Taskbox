package home.westering56.taskbox;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.util.Supplier;
import home.westering56.taskbox.data.room.Task;
import home.westering56.taskbox.data.room.TaskDatabase;
import home.westering56.taskbox.formatter.SnoozeTimeFormatter;

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
    private final TaskCursorAdapter mActiveTaskAdapter;
    private final TaskCursorAdapter mDoneTaskAdapter;
    private final TaskCursorAdapter mSnoozedTaskAdapter;
    private final TaskDatabase mTaskDatabase;
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
    void notifyDataSetChanged() {
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
                final CharSequence textValue;
                if (cursor.isNull(columnIndex)) {
                    // no snooze data yet, don't expose a value, but to overwrite what might
                    // have been displayed before
                    textValue = "";
                } else {
                    Instant until = Instant.ofEpochMilli(cursor.getLong(columnIndex));
                    textValue = SnoozeTimeFormatter.formatAdapterLine(view.getContext(), until);
                }
                ((TextView) view).setText(textValue);
                return true;
            }
            if (columnIndex == cursor.getColumnIndexOrThrow("rrule")) {
                if (!cursor.isNull(columnIndex)) {
                    Drawable icon = AppCompatResources.getDrawable(view.getContext(), R.drawable.ic_autorenew_black_24dp);
                    view.setVisibility(View.VISIBLE);
                    ((ImageView) view).setImageDrawable(icon);
                } else {
                    view.setVisibility(View.INVISIBLE);
                }
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

    private static class ActiveFormattingViewBinder implements SimpleCursorAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (columnIndex == cursor.getColumnIndexOrThrow("rrule")) {
                view.setVisibility(cursor.isNull(columnIndex) ? View.INVISIBLE : View.VISIBLE);
                return true;
            }
            return false;
        }
    }

    private TaskData(@NonNull Context appContext) {
        mUndoBuffer = new UndoBuffer();
        mTaskDatabase = TaskDatabase.getDatabase(appContext);

        // Set up snoozeDataAdapter for active tasks
        mActiveTaskAdapter = new TaskCursorAdapter(appContext,
                R.layout.task_list_item_active,
                () -> mTaskDatabase.taskDao().loadAllActive(),
                new String[] {"summary", "rrule"},
                new int[] {android.R.id.text1, R.id.task_list_item_repeat_icon});
        mActiveTaskAdapter.setViewBinder(new ActiveFormattingViewBinder());
        registerDataSetObserver(mActiveTaskAdapter.getTaskDataObserver());

        // Set up snoozeDataAdapter for done tasks
        mDoneTaskAdapter = new TaskCursorAdapter(appContext,
                android.R.layout.simple_list_item_2,
                () -> mTaskDatabase.taskDao().loadAllDone(),
                new String[] {"summary", "done_at"},
                new int[] {android.R.id.text1, android.R.id.text2});
        mDoneTaskAdapter.setViewBinder(new DoneFormattingViewBinder());
        registerDataSetObserver(mDoneTaskAdapter.getTaskDataObserver());

        // Set up snoozeDataAdapter for snoozed tasks
        mSnoozedTaskAdapter = new TaskCursorAdapter(appContext,
                R.layout.task_list_item,
                () -> mTaskDatabase.taskDao().loadAllSnoozed(),
                new String[] {"summary", "snooze_until", "rrule"},
                new int[] {android.R.id.text1, android.R.id.text2, R.id.task_list_item_repeat_icon});
        mSnoozedTaskAdapter.setViewBinder(new SnoozeFormattingViewBinder());
        registerDataSetObserver(mSnoozedTaskAdapter.getTaskDataObserver());

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

    ListAdapter getActiveTaskAdapter() { return mActiveTaskAdapter; }
    ListAdapter getDoneTaskAdapter() {
        return mDoneTaskAdapter;
    }
    ListAdapter getSnoozedTaskAdapter() {
        return mSnoozedTaskAdapter;
    }

    void addSampleData(Context context) {
        String[] sampleTasks = context.getApplicationContext().getResources().getStringArray(R.array.sampleTasks);
        for (String t : sampleTasks) {
            mTaskDatabase.taskDao().insert(new Task(t));
        }
        notifyDataSetChanged();
    }

    public void add(CharSequence taskSummary) {
        mUndoBuffer.clear(); // can't undo this
        mTaskDatabase.taskDao().insert(new Task(taskSummary));
        notifyDataSetChanged();
    }

    public Task getTask(int id) {
        return mTaskDatabase.taskDao().get(id);
    }

    void updateTask(Task task) {
        Task oldTask = mTaskDatabase.taskDao().get(task.uid);
        mUndoBuffer.storeUpdate(oldTask, task);
        mTaskDatabase.taskDao().update(task);
        notifyDataSetChanged();
    }

    void deleteTask(Task task) {
        mUndoBuffer.storeDelete(task);
        mTaskDatabase.taskDao().delete(task);
        notifyDataSetChanged();
    }

    void deleteAllTasks() {
        mUndoBuffer.clear(); // cannot undo delete all
        mTaskDatabase.clearAllTables();
        notifyDataSetChanged();
    }

    void undoLast() {
        if (mUndoBuffer.isEmpty()) {
            Log.w(TAG, "Undo requested, but nothing in undo buffer");
            return;
        }
        if (mUndoBuffer.isDelete()) {
            // undo a delete
            Log.d(TAG, "Undoing delete");
            mTaskDatabase.taskDao().insert(mUndoBuffer.mOldTask); // fall through
        }
        if (mUndoBuffer.isUpdate()) {
            // undo a modification
            Log.d(TAG, "Undoing update");
            mTaskDatabase.taskDao().update(mUndoBuffer.mOldTask); // fall through
        }
        // Either we undid it, or it was an odd state. Either way, clear the buffer
        mUndoBuffer.clear();
        notifyDataSetChanged();
    }

    /**
     * The @{@link Instant} at which the next snoozed task is scheduled to wake.
     */
    Instant getNextWakeupInstant() {
        return mTaskDatabase.taskDao().getNextWakeupDue();
    }

    /**
     * Fetch the tasks that have become active during the time between the two specified instants.
     * @param now inclusive
     */
    List<Task> getNewlyActiveTasks(Instant lastChecked, Instant now) {
        Log.d(TAG, "Fetching tasks that un-snoozed between " + lastChecked + " and " + now);
        return mTaskDatabase.taskDao().getNewlyActiveTasks(lastChecked, now);
    }

}

