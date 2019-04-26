package home.westering56.taskbox;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoActivityResumedException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class TaskDetailActivityTest {

    @Rule
    public ActivityTestRule<TaskDetailActivity> mActivityRule = new ActivityTestRule<>(TaskDetailActivity.class);
    private TaskData mTaskData;

    @Before
    public void clearTasks() {
        TaskData.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext()).deleteAllTasks();
    }

    @Before
    public void initTaskData() {
        mTaskData = TaskData.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    private boolean isKeyboardOpened() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext().getSystemService(InputMethodManager.class).isAcceptingText();
    }

    @Test
    public void newTaskAndSave() {
        final String UUID_STRING = UUID.randomUUID().toString();
        assertThat(isKeyboardOpened(), is(true));

        onView(withId(R.id.task_detail_summary_text))
                .check(matches(allOf(withText(""), withHint(R.string.task_detail_hint_text))));
        // check initial menu options are fine
        onView(withId(R.id.menu_item_save)).check(matches(not(isEnabled())));
        onView(withId(R.id.menu_item_snooze)).check(matches(isEnabled()));
        onView(withId(R.id.menu_item_done)).check(doesNotExist());
        onView(withId(R.id.menu_item_delete)).check(doesNotExist());
        onView(withId(R.id.menu_item_reactivate)).check(doesNotExist());

        onView(withId(R.id.task_detail_summary_text))
                .perform(typeTextIntoFocusedView(UUID_STRING))
                .check(matches(withText(UUID_STRING)));

        // check save becomes is enabled
        // check other menu options are fine
        onView(withId(R.id.menu_item_save)).check(matches(isEnabled()));
        onView(withId(R.id.menu_item_snooze)).check(matches(isEnabled()));
        onView(withId(R.id.menu_item_done)).check(doesNotExist());
        onView(withId(R.id.menu_item_delete)).check(doesNotExist());
        onView(withId(R.id.menu_item_reactivate)).check(doesNotExist());

        onView(withId(R.id.menu_item_save)).perform(click());

        assertThat(mActivityRule.getActivityResult().getResultCode(), is(TaskDetailActivity.RESULT_TASK_CREATED));
        assertThat(mActivityRule.getActivityResult().getResultData(), is(nullValue()));

        // check it was added to the DB OK
        ListAdapter adapter = mTaskData.getActiveTaskAdapter();
        assertThat(adapter.getCount(), is(1));
        assertThat(mTaskData.getTask((int) adapter.getItemId(0)).summary, is(UUID_STRING));
    }

    @Test
    public void newTaskCanBeCancelledAfterCreation() {
        performCancelActivity();
        assertThat(mActivityRule.getActivityResult().getResultCode(), is(Activity.RESULT_CANCELED));
    }

    private void performCancelActivity() {
        Espresso.closeSoftKeyboard();
        try {
            Espresso.pressBack();
            fail("Expected NoActivityResumedException");
        } catch (NoActivityResumedException expected) {
        }
    }

    @Test
    public void newTaskCanBeCancelledWithoutSavingChanges() {
        int activeTaskCountBefore = mTaskData.getActiveTaskAdapter().getCount();

        UUID uuid = UUID.randomUUID();
        onView(withId(R.id.task_detail_summary_text)).perform(typeTextIntoFocusedView(uuid.toString()));

        performCancelActivity();

        assertThat(mActivityRule.getActivityResult().getResultCode(), is(equalTo(Activity.RESULT_CANCELED)));
        assertThat(mTaskData.getActiveTaskAdapter().getCount(), is(equalTo(activeTaskCountBefore)));
    }

}