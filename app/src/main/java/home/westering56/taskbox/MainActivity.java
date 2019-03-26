package home.westering56.taskbox;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDateTime;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TaskboxMain";
    public static final String EXTRA_TASK_ID = "home.westering56.taskbox.TASK_ID";
    private static final String STATE_SELECTED_TAB = "home.westering56.taskbox.SELECTED_TAB";

    private static final int REQUEST_NEW_TASK   = 1;
    private static final int REQUEST_EDIT_TASK  = 2;

    private TaskData mTaskData;
    private ListView listView;
    private CoordinatorLayout rootView;
    private TabLayout tabLayout;
    private UndoClickListener mUndoClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        mUndoClickListener = new UndoClickListener();

        setContentView(R.layout.activity_main);
        rootView = findViewById(R.id.root); // needed for Snackbar to attach to
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDetailView();
            }
        });

        mTaskData = TaskData.getInstance(getApplicationContext());

        listView = findViewById(R.id.task_list_view);
        listView.setOnItemClickListener(taskClickedHandler);

        tabLayout = findViewById(R.id.task_tabs_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "OnTabSelected " + tab.toString());
                updateList();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        updateList();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // We need to save the selected tab as it's not automatically preserved like other things
        // Fixes selected tab being lost on rotation
        Log.d(TAG, "onSaveInstanceState");
        outState.putInt(STATE_SELECTED_TAB, tabLayout.getSelectedTabPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState");
        restoreSelectedTab(savedInstanceState);
    }

    private void restoreSelectedTab(Bundle savedInstanceState) {
        TabLayout.Tab activeTab = tabLayout.getTabAt(savedInstanceState.getInt(STATE_SELECTED_TAB));
        if (activeTab != null) { activeTab.select(); }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (tabLayout != null) { tabLayout.clearOnTabSelectedListeners(); }
        super.onDestroy();
    }

    private final AdapterView.OnItemClickListener taskClickedHandler = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            showDetailView(id);
        }
    };

    private void updateList() {
        Log.d(TAG, "updateList");
        switch (tabLayout.getSelectedTabPosition()) {
            case 0: // Active
                listView.setAdapter(mTaskData.getActiveTaskAdapter());
                break;
            case 1: // Snoozed
                listView.setAdapter(mTaskData.getSnoozedTaskAdapter());
                break;
            case 2: // Done
                listView.setAdapter(mTaskData.getDoneTaskAdapter());
                break;
            default:
                listView.setAdapter(mTaskData.getActiveTaskAdapter());
        }
        mTaskData.syncAdapters();
    }

    private void showDetailView(long id) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(EXTRA_TASK_ID, id);
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }
    private void showDetailView() {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        startActivityForResult(intent, REQUEST_NEW_TASK);
    }

    /**
     * Called whenever an undo action button is pressed, e.g. from a Snackbar when returning from
     * {@link TaskDetailActivity} after making changes.
     *
     * Default implementation simply calls {@link TaskData#undoLast()}.
     */
    class UndoClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "Undo action clicked");
            mTaskData.undoLast();
        }
    }

    /**
     * Extract the snooze time from the supplied {@link Intent}'s extras {@link Bundle}.
     * Looks for a {@link java.time.LocalDateTime} under the key
     * {@link TaskDetailActivity#RESULT_EXTRA_SNOOZE_UNTIL}
     */
    @NonNull
    private static LocalDateTime snoozeTimeFromIntentExtras(final Intent data) {
        /*
         * Extracted into its own method to hide a lot of this ugly null checking and casting
         */
        return (LocalDateTime) Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(
                    data)
                    .getExtras())
                    .get(TaskDetailActivity.RESULT_EXTRA_SNOOZE_UNTIL));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        CharSequence label = null;
        View.OnClickListener action = mUndoClickListener; // undo, unless it's a 'created' result
        switch (requestCode) {
            case REQUEST_NEW_TASK: // fall through
            case REQUEST_EDIT_TASK:
                switch (resultCode) {
                    case RESULT_CANCELED:
                        label = "Cancelled"; break;
                    case TaskDetailActivity.RESULT_TASK_CREATED:
                        action = null; // No undo action. Fall through
                    case TaskDetailActivity.RESULT_TASK_UPDATED:
                        label = "Task saved"; break;
                    case TaskDetailActivity.RESULT_TASK_DONE:
                        label = "Marked as done"; break;
                    case TaskDetailActivity.RESULT_TASK_REACTIVATED:
                        label = "Marked as active"; break;
                    case TaskDetailActivity.RESULT_TASK_DELETED:
                        label = "Deleted"; break;
                    case TaskDetailActivity.RESULT_TASK_SNOOZED:
                        LocalDateTime until = snoozeTimeFromIntentExtras(data);
                        label = getString(R.string.task_detail_snoozed_until,
                                SnoozeTimeFormatter.format(getApplicationContext(), until));
                        break;
                }
                if (label != null) {
                    Snackbar snackbar = Snackbar.make(rootView, label, Snackbar.LENGTH_LONG);
                    if (action != null) { snackbar.setAction(R.string.action_undo, action); }
                    snackbar.show();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
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
                mTaskData.addSampleData(getApplicationContext());
                return true;
            case R.id.menu_action_delete_all:
                mTaskData.deleteAllTasks();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
