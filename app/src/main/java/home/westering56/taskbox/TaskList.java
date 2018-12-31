package home.westering56.taskbox;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TaskList extends AppCompatActivity {

    private static final String[] dummyData = new String[]{"Lorem", "Ipsum", "dolor", "sit", "amet", "the", "quick brown", "fox", "jumped", "over", "the", "lazy", "Dog's", "back"};

    private ArrayAdapter<String> taskData;
    private ListView taskListView;

    private RecyclerView taskListRecyclerView;
    private RecyclerView.LayoutManager taskListLayoutManager;
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Regular list version of the list
        taskData = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dummyData);
        taskListView = findViewById(R.id.taskListView);
        taskListView.setAdapter(taskData);


        // RecyclerView version of the list
//        taskListLayoutManager = new LinearLayoutManager(this);
//        taskAdapter = new TaskAdapter(dummyData);
//        taskListRecyclerView = findViewById(R.id.taskListRecyclerView);
//
//        taskListRecyclerView.setHasFixedSize(true);
//        taskListRecyclerView.setLayoutManager(taskListLayoutManager);
//        taskListRecyclerView.setAdapter(taskAdapter);

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
