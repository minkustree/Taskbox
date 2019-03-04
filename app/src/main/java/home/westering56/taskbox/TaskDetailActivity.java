package home.westering56.taskbox;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class TaskDetailActivity extends AppCompatActivity {

    private EditText taskSummary;
    private TaskData taskData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
            ab.setDisplayShowTitleEnabled(false);
        }

        taskSummary = findViewById(R.id.task_detail_summary_text);
        // menu entries change if there's text here or not
        taskSummary.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                invalidateOptionsMenu();
            }
        });

        taskData = TaskData.getInstance(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_save:
                onSaveClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // enable save only if there's text to be saved
        menu.findItem(R.id.menu_item_save).setEnabled(taskSummary.length() > 0);
        return super.onPrepareOptionsMenu(menu);
    }


    private void onSaveClicked() {
        CharSequence summary = taskSummary.getText();
        if (summary == null) {
            setResult(RESULT_CANCELED);
        } else {
            taskData.add(summary);
            setResult(RESULT_OK);
        }
        finish();
    }
}
