package home.westering56.taskbox;

import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import home.westering56.taskbox.room.Task;
import home.westering56.taskbox.viewmodel.TaskViewModel;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class TaskList extends AppCompatActivity {

    public static final String[] dummyData = new String[]{"Lorem", "Ipsum", "dolor", "sit", "amet", "the", "quick brown", "fox", "jumped", "over", "the", "lazy", "Dog's", "back"};

//    private ArrayAdapter<String> taskData;
//    private ListView taskListView;

    private RecyclerView taskListRecyclerView;
    private RecyclerView.LayoutManager taskListLayoutManager;

    private TaskViewModel taskViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        taskViewModel = new TaskViewModel(getApplication());

        setContentView(R.layout.activity_task_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddTaskActivity.class);
                startActivity(intent);;
            }
        });

        // Regular list version of the list
//        taskData = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dummyData);
//        taskListView = findViewById(R.id.taskListView);
//        taskListView.setAdapter(taskData);

        // RecyclerView version of the list
        taskListRecyclerView = findViewById(R.id.taskListRecyclerView);
        final TaskAdapter taskAdapter = new TaskAdapter(this);
        taskListRecyclerView.setAdapter(taskAdapter);

        taskListLayoutManager = new LinearLayoutManager(this);
        taskListRecyclerView.setLayoutManager(taskListLayoutManager);

        taskListRecyclerView.setHasFixedSize(true);

        // Wire up data bindings
        taskViewModel = ViewModelProviders.of(this).get(TaskViewModel.class);
        taskViewModel.getTasks().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                taskAdapter.setTasks(tasks);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
