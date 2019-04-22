package home.westering56.taskbox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import home.westering56.taskbox.data.room.Task;
import home.westering56.taskbox.formatter.RepeatedTaskFormatter;
import home.westering56.taskbox.formatter.SnoozeTimeFormatter;
import home.westering56.taskbox.fragments.SnoozeOptionListener;
import home.westering56.taskbox.fragments.SnoozeOptionsDialog;

public class TaskDetailActivity extends AppCompatActivity implements SnoozeOptionListener {
    public static final String EXTRA_TASK_ID = "TASK_ID";
    private static final String TAG = "TaskDetailActivity";

    // Indicates the action the user took within the activity
    @SuppressWarnings({"PointlessArithmeticExpression"})
    public static final int RESULT_TASK_CREATED = RESULT_FIRST_USER + 0;
    public static final int RESULT_TASK_UPDATED = RESULT_FIRST_USER + 1;
    public static final int RESULT_TASK_DELETED = RESULT_FIRST_USER + 2;
    public static final int RESULT_TASK_DONE = RESULT_FIRST_USER + 3;
    public static final int RESULT_TASK_REACTIVATED = RESULT_FIRST_USER + 4;
    public static final int RESULT_TASK_SNOOZED = RESULT_FIRST_USER + 5;
    public static final String RESULT_EXTRA_SNOOZE_UNTIL = "snoozeUntil";

    /**
     * Stores the result of the operation performed by this activity so other activities can
     * display appropriate UI, even if they didn't call this as startActivityForResult
     */
    private static TaskDetailActionResult sActionResult;

    /**
     * Used to store result data for MainActivity to pick up via static methods
     */
    static class TaskDetailActionResult {
        private final int mResultCode;
        @Nullable
        private final Intent mData;

        TaskDetailActionResult(int resultCode, @Nullable Intent data) {
            this.mResultCode = resultCode;
            this.mData = data;
        }

        int getResultCode() {
            return mResultCode;
        }

        @Nullable
        Intent getData() {
            return mData;
        }
    }

    public static TaskDetailActionResult getActionResultAndClear() {
        synchronized (TaskDetailActivity.class) {
            TaskDetailActionResult result = sActionResult;
            sActionResult = null;
            return result;
        }
    }

    private static void setActionResult(TaskDetailActionResult actionResult) {
        synchronized (TaskDetailActivity.class) {
            sActionResult = actionResult;
        }
    }

    public static class TaskDetailViewModel extends ViewModel {
        private Task mOriginalTask;
        private Task mTask;

        public TaskDetailViewModel() {
            super();
            mTask = new Task();
        }

        void load(@NonNull Context context, int taskId) {
            mOriginalTask = TaskData.getInstance(context).getTask(taskId);
            mTask = TaskData.getInstance(context).getTask(taskId); // a separate copy for live changes
        }

        public Task getTask() {
            return mTask;
        }

        void clearTask() {
            mTask = null;
        }

        boolean isNewTask() {
            return mOriginalTask == null;
        }

        int commit(@NonNull Context context) {
            if (isNewTask()) { // creating
                TaskData.getInstance(context).addTask(mTask);
                return RESULT_TASK_CREATED;
            } else if (mTask == null) { // deleting
                TaskData.getInstance(context).deleteTask(mOriginalTask);
                return RESULT_TASK_DELETED;
            } else { // updating
                TaskData.getInstance(context).updateTask(mTask);
                return RESULT_TASK_UPDATED;
            }
        }
    }

    private EditText mTaskSummary;
    private View mBannerContainer;
    private TextView mSnoozeTimeBanner;
    private View mRepeatBannerGroup;
    private TextView mRepeatBannerText;

    private TaskDetailViewModel mModel;

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

        mTaskSummary = findViewById(R.id.task_detail_summary_text);
        mTaskSummary.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String newSummary = s.toString();
                if (newSummary.length() > 0) {
                    mModel.getTask().summary = newSummary;
                }
                invalidateOptionsMenu();
            }
        });
        // react to keyboard 'done' and 'Enter key' as if Save had been pressed
        mTaskSummary.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                onSaveClicked();
                return true;
            }
            return false;
        });

        mModel = ViewModelProviders.of(this).get(TaskDetailViewModel.class);
        // New task or existing task?
        int id = getIntent().getIntExtra(EXTRA_TASK_ID, -1);
        Log.d(TAG, "Task ID was " + id);
        if (id != -1) {
            mModel.load(this, id);
        }

        mBannerContainer = findViewById(R.id.task_detail_banner);
        mSnoozeTimeBanner = findViewById(R.id.task_detail_snooze_time);
        mRepeatBannerGroup = findViewById(R.id.task_detail_repeat_status);
        mRepeatBannerText = findViewById(R.id.task_detail_repeat_status_text);

        updateUiFromModel();
    }

    private void updateUiFromModel() {
        // TODO: Call this asynchronously when the DB lookup completes
        final Task task = mModel.getTask();
        if (task != null) {
            // existing task being updated
            mTaskSummary.setText(mModel.getTask().summary);
            mTaskSummary.setSelection(mTaskSummary.length());
        }
        updateBanners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getActionResultAndClear();
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
        final Task task = mModel.getTask();
        // enable save only if there's text to be saved
        menu.findItem(R.id.menu_item_save).setEnabled(mTaskSummary.length() > 0);
        // Always have snooze be visible, but have it create a snoozed task if it's a new one
        menu.findItem(R.id.menu_item_snooze).setVisible(true);
        if (mModel.isNewTask()) {
            menu.findItem(R.id.menu_item_delete).setVisible(false);
            menu.findItem(R.id.menu_item_done).setVisible(false);
            menu.findItem(R.id.menu_item_reactivate).setVisible(false);
        } else {
            menu.findItem(R.id.menu_item_delete).setVisible(true);
            menu.findItem(R.id.menu_item_done).setVisible(!task.isDone());
            menu.findItem(R.id.menu_item_reactivate).setVisible(task.isDone());
        }
        return super.onPrepareOptionsMenu(menu);
    }


    private void onSaveClicked() {
        if (mTaskSummary.length() == 0)
            throw new IllegalStateException("Should not be able to call save with zero-length summary");
        final int result = mModel.commit(this);
        if (mModel.getTask().isSnoozed()) { // repeating task done -> now snoozed until next time
            setResultAndLastAction(RESULT_TASK_SNOOZED, buildSnoozedResultIntent(mModel.getTask().snoozeUntil));
        } else {
            setResultAndLastAction(result);
        }
        finish();
    }

    private void setResultAndLastAction(int resultCode) {
        setResultAndLastAction(resultCode, null);
    }

    private void setResultAndLastAction(int resultCode, @Nullable Intent intent) {
        setResult(resultCode, intent);
        setActionResult(new TaskDetailActionResult(resultCode, intent));
    }

    private void onDeleteClicked() {
        mModel.clearTask();
        final int result = mModel.commit(this);
        setResultAndLastAction(result);
        finish();
    }

    private void onDoneClicked() {
        Task task = mModel.getTask();
        task.done();
        mModel.commit(this);
        if (task.isSnoozed()) { // repeating task done -> now snoozed until next time
            setResultAndLastAction(RESULT_TASK_SNOOZED, buildSnoozedResultIntent(task.snoozeUntil));
        } else {
            setResultAndLastAction(RESULT_TASK_DONE);
        }
        finish();
    }

    private void onReactivateClicked() {
        mModel.getTask().reactivate();
        mModel.commit(this);
        setResultAndLastAction(RESULT_TASK_REACTIVATED);
        finish();
    }

    private void onSnoozeClicked() {
        DialogFragment newFragment = SnoozeOptionsDialog.newInstance();
        newFragment.show(getSupportFragmentManager(), SnoozeOptionsDialog.TAG);
    }

    @Override
    public void onSnoozeOptionSelected(LocalDateTime snoozeUntil, RecurrenceRule rule) {
        Instant snoozeUntilInstant = ZonedDateTime.of(snoozeUntil, ZoneId.systemDefault()).toInstant();
        if (rule == null) {
            mModel.getTask().snooze(snoozeUntilInstant);
        } else {
            mModel.getTask().snoozeAndRepeat(snoozeUntilInstant, rule);
        }
        updateBanners();
        // For existing tasks, clicking snooze also save & dismiss
        if (!mModel.isNewTask()) {
            onSaveClicked();
        }
    }

    private static Intent buildSnoozedResultIntent(Instant until) {
        return buildSnoozedResultIntent(LocalDateTime.ofInstant(until, ZoneId.systemDefault()));
    }

    private static Intent buildSnoozedResultIntent(LocalDateTime until) {
        Intent result = new Intent();
        result.putExtra(RESULT_EXTRA_SNOOZE_UNTIL, until);
        return result;
    }


    private void updateBanners() {
        final Task task = mModel.getTask();
        boolean isBannerVisible = false;

        // "snoozed until" if the task is snoozed
        if (task == null || !task.isSnoozed()) {
            mSnoozeTimeBanner.setVisibility(View.GONE);
        } else {
            CharSequence line = SnoozeTimeFormatter.formatStatusLine(this, task);
            mSnoozeTimeBanner.setText(line);
            mSnoozeTimeBanner.setVisibility(View.VISIBLE);
            isBannerVisible = true;
        }

        // "Repeats every" if the task is repeating
        if (task == null || !task.isRepeating()) {
            mRepeatBannerGroup.setVisibility(View.GONE);
        } else {
            CharSequence line = RepeatedTaskFormatter.format(this, task.rrule);
            mRepeatBannerText.setText(line);
            mRepeatBannerGroup.setVisibility(View.VISIBLE);
            isBannerVisible = true;
        }

        mBannerContainer.setVisibility(isBannerVisible ? View.VISIBLE : View.GONE);
    }


}
