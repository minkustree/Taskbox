package home.westering56.taskbox.viewmodel;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import home.westering56.taskbox.TaskRepository;
import home.westering56.taskbox.room.Task;

public class TaskViewModel extends AndroidViewModel {
    private TaskRepository repository;
    private LiveData<List<Task>> tasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        tasks = repository.getAllTasks();
    }

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    public void insert(Task t) {
        repository.insert(t);
    }
}
