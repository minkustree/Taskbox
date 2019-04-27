package home.westering56.taskbox;

import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.content.pm.ActivityInfo.*;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SnoozeOptionsTest {

    @Rule
    public final ActivityTestRule<TaskDetailActivity> mActivityTestRule = new ActivityTestRule<>(TaskDetailActivity.class);

    @Test
    public void settingSnoozeOptionsCreatesSnoozeBar() {
        onView(withId(R.id.task_detail_snooze_time)).check(matches(not(isDisplayed())));
        onView(withId(R.id.menu_item_snooze)).perform(click());
        onView(withId(R.id.snooze_dialog_content)).check(matches(isDisplayed()));

        // technically supposed to use onData here, but that's best for when lots of data may load from an adapter.
        // The adapter here is static, and parsing the adapter objects is painful.
        onView(allOf(withId(R.id.snooze_option_item_title), withText("Tomorrow Morning"))).perform(click());

        onView(withId(R.id.snooze_dialog_content)).check(doesNotExist());

        onView(withId(R.id.task_detail_snooze_time))
                .check(matches(allOf(
                        isDisplayed(),
                        withText(allOf(
                                containsString("Snoozed until"),
                                containsString("9:00")
                        ))
                )));
    }

    @Test
    public void snoozeOptionsSurvivesRotate() {
        onView(withId(R.id.task_detail_snooze_time)).check(matches(not(isDisplayed())));
        onView(withId(R.id.menu_item_snooze)).perform(click());
        onView(withId(R.id.snooze_dialog_content)).check(matches(isDisplayed()));

        toggleOrientation();

        // still shown after a rotate (and there's only one - matcher will fail if not unique
        onView(withId(R.id.snooze_dialog_content)).check(matches(isDisplayed()));

        // data still works?
        onView(allOf(withId(R.id.snooze_option_item_title), withText("Tomorrow Morning"))).perform(click());

        onView(withId(R.id.snooze_dialog_content)).check(doesNotExist());

        onView(withId(R.id.task_detail_snooze_time))
                .check(matches(allOf(
                        isDisplayed(),
                        withText(allOf(
                                containsString("Snoozed until"),
                                containsString("9:00")
                        ))
                )));
    }

    @Test
    public void snoozeOptionsIsCancelable() {
        onView(withId(R.id.task_detail_snooze_time)).check(matches(not(isDisplayed())));
        onView(withId(R.id.menu_item_snooze)).perform(click());
        onView(withId(R.id.snooze_dialog_content)).check(matches(isDisplayed()));

        Espresso.pressBack();

        onView(withId(R.id.snooze_dialog_content)).check(doesNotExist());
        onView(withId(R.id.task_detail_snooze_time)).check(matches(not(isDisplayed())));
    }

    @Test
    public void snoozeOptionsIsCancelableAfterRotate() {
        onView(withId(R.id.task_detail_snooze_time)).check(matches(not(isDisplayed())));
        onView(withId(R.id.menu_item_snooze)).perform(click());
        onView(withId(R.id.snooze_dialog_content)).check(matches(isDisplayed()));

        toggleOrientation();
        onView(withId(R.id.snooze_dialog_content)).check(matches(isDisplayed()));
        Espresso.pressBack();

        onView(withId(R.id.snooze_dialog_content)).check(doesNotExist());
        onView(withId(R.id.task_detail_snooze_time)).check(matches(not(isDisplayed())));
    }

    @Test
    public void customSnoozeDateTimeDialogIsCancelable() {
        onView(withId(R.id.task_detail_snooze_time)).check(matches(not(isDisplayed())));

        onView(withId(R.id.menu_item_snooze)).perform(click());
        onView(withId(R.id.snooze_dialog_content)).check(matches(isDisplayed()));

        onView(withId(R.id.snooze_dialog_button_custom)).perform(click());

        // pick the default date in the date picker
        onView(withResourceName("datePicker")).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform(click());

        // should now get the custom dialog
        onView(withId(R.id.snooze_dialog_content)).check(doesNotExist());
        onView(withId(R.id.snooze_custom_date_time_dialog)).check(matches(isDisplayed()));
        checkCustomSnoozeDateTimeDialogDefaults();

        Espresso.pressBack();
        onView(withId(R.id.snooze_custom_date_time_dialog)).check(doesNotExist());
        onView(withId(R.id.snooze_dialog_content)).check(matches(isDisplayed()));

        Espresso.pressBack();

        onView(withId(R.id.snooze_dialog_content)).check(doesNotExist());
        onView(withId(R.id.task_detail_snooze_time)).check(matches(not(isDisplayed())));
    }

    private void checkCustomSnoozeDateTimeDialogDefaults() {
        onView(withId(R.id.snooze_custom_date_selector)).check(matches(withText(R.string.snooze_time_format_today)));
        onView(withId(R.id.snooze_time_spinner_item_title)).check(matches(withText(R.string.snooze_time_picker_morning)));
        onView(withId(R.id.snooze_custom_repeat_selector)).check(matches(withSpinnerText(R.string.repeat_option_does_not_repeat)));
    }

    private void toggleOrientation() {
        int orientation = mActivityTestRule.getActivity().getRequestedOrientation();
        int newOrientation = (orientation == SCREEN_ORIENTATION_LANDSCAPE) ? SCREEN_ORIENTATION_PORTRAIT : SCREEN_ORIENTATION_LANDSCAPE;
        mActivityTestRule.getActivity().setRequestedOrientation(newOrientation);
    }
}
