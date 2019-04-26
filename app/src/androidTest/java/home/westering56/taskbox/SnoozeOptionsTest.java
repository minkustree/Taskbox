package home.westering56.taskbox;

import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SnoozeOptionsTest {

    @Rule
    public ActivityTestRule<TaskDetailActivity> mActivityTestRule = new ActivityTestRule<>(TaskDetailActivity.class);

    @Test
    public void settingSnoozeOptionsCreatesSnoozeBar() {
        onView(withId(R.id.task_detail_snooze_time)).check(matches(not(isDisplayed())));
        onView(withId(R.id.menu_item_snooze)).perform(click());
        onView(withId(R.id.snooze_dialog_content)).check(matches(isDisplayed()));

        // technically supposed to use onData here, but that's best for when lots of data may load from an adapter.
        // The adapter here is static, and parsing the adapter objects is painful.
        onView(allOf(withId(R.id.snooze_option_item_title), withText("Tomorrow Morning"))).perform(click());

        onView(withId(R.id.snooze_dialog_content)).check(ViewAssertions.doesNotExist());

        onView(withId(R.id.task_detail_snooze_time))
                .check(matches(allOf(
                        isDisplayed(),
                        withText(allOf(
                                containsString("Snoozed until"),
                                containsString("9:00")
                        ))
                )));
    }
}
