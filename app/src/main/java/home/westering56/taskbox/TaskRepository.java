package home.westering56.taskbox;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import androidx.lifecycle.LiveData;
import home.westering56.taskbox.room.Task;
import home.westering56.taskbox.room.TaskDao;
import home.westering56.taskbox.room.TaskRoomDatabase;

public class TaskRepository {
    private TaskDao taskDao;
    private LiveData<List<Task>> allTasks;

    public TaskRepository(Application application) {
        TaskRoomDatabase db = TaskRoomDatabase.getInstance(application);
        taskDao = db.taskDao();
        allTasks = taskDao.loadAllTasks();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    /** Must be called on a non-ui thread */
    public void insert(Task task) {
        new insertAsyncTask(taskDao).execute(task);
    }

    private static class insertAsyncTask extends AsyncTask<Task, Void, Void> {
        private TaskDao asyncTaskDao;

        insertAsyncTask(TaskDao taskDao) {
            asyncTaskDao = taskDao;
        }

        @Override
        protected Void doInBackground(final Task... tasks) {
            asyncTaskDao.insertTask(tasks[0]);
            return null;
        }
    }
}
