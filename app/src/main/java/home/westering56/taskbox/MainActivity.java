package home.westering56.taskbox;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_TASK_ID = "home.westering56.taskbox.TASK_ID";

    private TaskData td;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDetailView();
            }
        });

        td = TaskData.getInstance(getApplicationContext());

        ListView listView = findViewById(R.id.task_list_view);
        listView.setAdapter(td.getAdapter());
        listView.setOnItemClickListener(taskClickedHandler);
    }

    private final AdapterView.OnItemClickListener taskClickedHandler = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            showDetailView(id);
        }
    };

    private void showDetailView(long id) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(EXTRA_TASK_ID, id);
        startActivity(intent);
    }
    private void showDetailView() {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_action_add_sample:
                td.addSampleData(getApplicationContext());
                return true;
            case R.id.menu_action_delete_all:
                td.deleteAllTasks();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
