package home.westering56.taskbox;

import android.content.Intent;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import home.westering56.taskbox.room.Task;
import home.westering56.taskbox.viewmodel.TaskViewModel;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import static androidx.recyclerview.widget.ItemTouchHelper.END;
import static androidx.recyclerview.widget.ItemTouchHelper.START;

public class TaskList extends AppCompatActivity {

    private RecyclerView taskListRecyclerView;
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


        // RecyclerView version of the list
        taskListRecyclerView = findViewById(R.id.taskListRecyclerView);
        final TaskRecyclerViewAdapter taskRecyclerViewAdapter = new TaskRecyclerViewAdapter(this);
        taskListRecyclerView.setAdapter(taskRecyclerViewAdapter);

        LinearLayoutManager taskListLayoutManager = new LinearLayoutManager(this);
        taskListRecyclerView.setLayoutManager(taskListLayoutManager);

        taskListRecyclerView.setHasFixedSize(true);
        taskListRecyclerView.addItemDecoration(
                new DividerItemDecoration(taskListRecyclerView.getContext(),
                        taskListLayoutManager.getOrientation()));

        // Wire up data bindings
        taskViewModel = ViewModelProviders.of(this).get(TaskViewModel.class);
        taskViewModel.getTasks().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                taskRecyclerViewAdapter.setTasks(tasks);
            }
        });

        // Wire up touch helper
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, START | END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }
        });
        touchHelper.attachToRecyclerView(taskListRecyclerView);
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
