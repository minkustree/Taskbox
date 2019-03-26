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

    private TaskData td;
    private ListView listView;
    private CoordinatorLayout rootView;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
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

        td = TaskData.getInstance(getApplicationContext());

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
                listView.setAdapter(td.getActiveTaskAdapter());
                break;
            case 1: // Snoozed
                listView.setAdapter(td.getSnoozedTaskAdapter());
                break;
            case 2: // Done
                listView.setAdapter(td.getDoneTaskAdapter());
                break;
            default:
                listView.setAdapter(td.getActiveTaskAdapter());
        }
        td.syncAdapters();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        CharSequence label = null;
        View.OnClickListener action = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Undo button clicked");
                TaskData.getInstance(v.getContext()).undoLast();
            }
        };
        switch (requestCode) {
            case REQUEST_NEW_TASK: // fall through
            case REQUEST_EDIT_TASK:
                switch (resultCode) {
                    case RESULT_CANCELED:
                        label = "Cancelled"; break;
                    case TaskDetailActivity.RESULT_TASK_CREATED:
                        action = null;
                        // fall through
                    case TaskDetailActivity.RESULT_TASK_UPDATED:
                        label = "Task saved"; break;
                    case TaskDetailActivity.RESULT_TASK_DONE:
                        label = "Marked as done"; break;
                    case TaskDetailActivity.RESULT_TASK_REACTIVATED:
                        label = "Marked as active"; break;
                    case TaskDetailActivity.RESULT_TASK_DELETED:
                        label = "Deleted"; break;
                    case TaskDetailActivity.RESULT_TASK_SNOOZED:
                        label = "Snoozed";
                        // wow, this is some C-like errors handling just to be null-safe...
                        if (data == null) break;
                        Bundle extras = data.getExtras();
                        if (extras == null) break;
                        LocalDateTime until = (LocalDateTime)extras.get(TaskDetailActivity.RESULT_EXTRA_SNOOZE_UNTIL);
                        if (until == null) break;
                        label = getString(R.string.task_detail_snoozed_until,
                                SnoozeTimeFormatter.format(getApplicationContext(), until));
                        break;
                }
                if (label != null) {
                    Snackbar snackbar = Snackbar.make(rootView, label, Snackbar.LENGTH_LONG);
                    if (action != null) { snackbar.setAction("Undo", action); }
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
