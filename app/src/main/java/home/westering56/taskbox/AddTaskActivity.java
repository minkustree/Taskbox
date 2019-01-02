package home.westering56.taskbox;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import home.westering56.taskbox.room.Task;
import home.westering56.taskbox.viewmodel.TaskViewModel;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class AddTaskActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private TaskViewModel taskViewModel;
    private TextView editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        editText = findViewById(R.id.editText);
        actionBar = getSupportActionBar();
        actionBar.setTitle("New Task");
        actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);

        taskViewModel = ViewModelProviders.of(this).get(TaskViewModel.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_task_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save_menu_item) {
            String summary = editText.getText().toString();
            if (summary.length() > 0) {
                taskViewModel.insert(new Task(summary));
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
