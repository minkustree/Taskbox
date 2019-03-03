package home.westering56.taskbox;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class TaskData {


    private static TaskData instance;
    private ArrayAdapter<CharSequence> adapter;


    private TaskData(@NonNull Context appContext) {
        CharSequence[] sampleData = appContext.getResources().getTextArray(R.array.sampleTasks);
        adapter = new ArrayAdapter<>(appContext, android.R.layout.simple_list_item_1);
        adapter.addAll(sampleData);
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
        return adapter;
    }

    public void add(CharSequence taskSummary) {
        adapter.add(taskSummary);
    }
}
