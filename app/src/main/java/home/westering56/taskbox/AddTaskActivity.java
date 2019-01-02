package home.westering56.taskbox;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class AddTaskActivity extends AppCompatActivity {

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        actionBar = getSupportActionBar();
        actionBar.setTitle("New home.westering56.taskbox.room.Task");
        actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
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
            // TODO: Save menu action
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
