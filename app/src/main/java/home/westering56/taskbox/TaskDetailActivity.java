package home.westering56.taskbox;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TaskDetailActivity extends AppCompatActivity {

    private EditText taskSummary;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        taskSummary = findViewById(R.id.task_detail_summary_text);
        saveButton = findViewById(R.id.task_detail_save_button);
    }

    public void onSaveClicked(View view) {
        setResult(RESULT_OK);
        finish();
    }
}
