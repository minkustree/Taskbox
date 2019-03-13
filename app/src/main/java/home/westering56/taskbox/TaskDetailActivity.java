package home.westering56.taskbox;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import home.westering56.taskbox.data.room.Task;

public class TaskDetailActivity extends AppCompatActivity {

    // Indicates the action the user took within the activity
    @SuppressWarnings({"PointlessArithmeticExpression", "WeakerAccess"})
    public static final int RESULT_TASK_CREATED     = RESULT_FIRST_USER + 0;
    @SuppressWarnings("WeakerAccess") // Remove weaker access suppression when used externally
    public static final int RESULT_TASK_UPDATED     = RESULT_FIRST_USER + 1;
    @SuppressWarnings("WeakerAccess")
    public static final int RESULT_TASK_DELETED     = RESULT_FIRST_USER + 2;
    @SuppressWarnings("WeakerAccess")
    public static final int RESULT_TASK_DONE        = RESULT_FIRST_USER + 3;
    @SuppressWarnings("WeakerAccess")
    public static final int RESULT_TASK_REACTIVATED = RESULT_FIRST_USER + 4;
    @SuppressWarnings("WeakerAccess")
    public static final int RESULT_TASK_SNOOZED     = RESULT_FIRST_USER + 5;

    private static final String TAG = "TaskDetail";
    private EditText taskSummary;
    private TaskData taskData;
    private Task task;

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
        // react to keyboard 'done' and 'Enter key' as if Save had been pressed
        taskSummary.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                    onSaveClicked();
                    return true;
                }
                return false;
            }
        });

        taskData = TaskData.getInstance(getApplicationContext());

        // New task or existing task?
        Intent intent = getIntent();
        long id = intent.getLongExtra(MainActivity.EXTRA_TASK_ID, -1);
        if (id != -1) { task = taskData.getTask(id); }
        if (task != null) {
            // TODO: Set these fields asynchronously when the DB lookup completes
            // existing task being updated
            taskSummary.setText(task.summary);
            taskSummary.setSelection(taskSummary.length());
        }
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
            case R.id.menu_item_delete:
                onDeleteClicked();
                return true;
            case R.id.menu_item_done:
                onDoneClicked();
                return true;
            case R.id.menu_item_reactivate:
                onReactivateClicked();
                return true;
            case R.id.menu_item_snooze:
                onSnoozeClicked();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // enable save only if there's text to be saved
        menu.findItem(R.id.menu_item_save).setEnabled(taskSummary.length() > 0);
        // TODO: Always have snooze be visible, but have it create a snoozed task if it's a new one
        if (task != null) {
            menu.findItem(R.id.menu_item_snooze).setVisible(true);
            menu.findItem(R.id.menu_item_delete).setVisible(true);
            menu.findItem(R.id.menu_item_done).setVisible(!task.isDone());
            menu.findItem(R.id.menu_item_reactivate).setVisible(task.isDone());
        } else {
            menu.findItem(R.id.menu_item_snooze).setVisible(false);
            menu.findItem(R.id.menu_item_delete).setVisible(false);
            menu.findItem(R.id.menu_item_done).setVisible(false);
            menu.findItem(R.id.menu_item_reactivate).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    private void onSaveClicked() {
        CharSequence summary = taskSummary.getText();
        if (taskSummary.length() == 0) {
            setResult(RESULT_CANCELED);
        } else {
            if (task != null) {
                ensureTask();
                taskData.updateTask(task);
                setResult(RESULT_TASK_UPDATED);
            } else {
                taskData.add(summary);
                setResult(RESULT_TASK_CREATED);
            }
        }
        finish();
    }

    private void onDeleteClicked() {
        ensureTask();
        taskData.deleteTask(task);
        task = null;
        setResult(RESULT_TASK_DELETED);
        finish();
    }

    private void onDoneClicked() {
        ensureTask(false);
        task.actionDone();
        taskData.updateTask(task);
        setResult(RESULT_TASK_DONE);
        finish();
    }

    private void onReactivateClicked() {
        ensureTask();
        task.actionReactivate();
        taskData.updateTask(task);
        setResult(RESULT_TASK_REACTIVATED);
        finish();
    }

    private void onSnoozeClicked() {
        // Display a fragment to select a snooze time
        DialogFragment newFragment = SnoozeDialogFragment.newInstance(new SnoozeDialogFragment.SnoozeOptionListener() {
            @Override
            public void onSnoozeOptionSelected(String title, LocalDateTime snoozeUntil) {
                Log.d(TAG, "Snooze option (" + title + ", " + snoozeUntil.toString() + ")");
                completeSnoozeClicked(snoozeUntil);
            }
        });
        newFragment.show(getSupportFragmentManager(), "snooze_dialog");
    }

    private void completeSnoozeClicked(LocalDateTime snoozeUntil) {
        ensureTask();
        task.actionSnooze(ZonedDateTime.of(snoozeUntil, ZoneId.systemDefault()).toInstant());
        taskData.updateTask(task);
        setResult(RESULT_TASK_SNOOZED);
        finish();
    }

    private void ensureTask() {
        ensureTask(true);
    }

    /**
     * Ensures the local task object exists and reflects the current summary text
     */
    private void ensureTask(boolean updateSummary) {
        if (task == null) {
            task = new Task();
        }
        if (updateSummary) {
            // TODO: Bail out of saving if current text is empty - should not make the change
            task.summary = taskSummary.getText().toString();
        }
        assert task != null;
    }
}
