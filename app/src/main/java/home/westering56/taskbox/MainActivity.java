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
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TaskboxMain";
    public static final String EXTRA_TASK_ID = "home.westering56.taskbox.TASK_ID";
    private static final String STATE_SELECTED_TAB = "home.westering56.taskbox.SELECTED_TAB";

    private TaskData td;
    private ListView listView;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
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
            case 1: // Done
                listView.setAdapter(td.getDoneTaskAdapter());
                break;
            default:
                listView.setAdapter(td.getActiveTaskAdapter());
        }
    }

    private void showDetailView(long id) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(EXTRA_TASK_ID, id);
        startActivityForResult(intent, 0);
    }
    private void showDetailView() {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        startActivityForResult(intent, 0);
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
