package home.westering56.taskbox;

import android.content.Context;
import androidx.annotation.NonNull;


import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

import home.westering56.taskbox.data.room.Task;
import home.westering56.taskbox.data.room.TaskDatabase;

public class TaskData {

    private static TaskData instance;
    private final CursorAdapter ca;
    private final TaskDatabase taskDatabase;

    private TaskData(@NonNull Context appContext) {
        taskDatabase = TaskDatabase.getDatabase(appContext);
        ca = new SimpleCursorAdapter(appContext,
                android.R.layout.simple_list_item_1,
                taskDatabase.taskDao().loadAll(),
                new String[] {"summary"},
                new int[] { android.R.id.text1 },
                0);
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
        taskDatabase.taskDao().update(task);
        ca.swapCursor(taskDatabase.taskDao().loadAll());
    }

    public void deleteTask(Task task) {
        taskDatabase.taskDao().delete(task);
        ca.swapCursor(taskDatabase.taskDao().loadAll());
    }
}

