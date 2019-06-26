package home.westering56.taskbox;

import android.content.Context;
import android.widget.SimpleAdapter;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import home.westering56.taskbox.SnoozeOptionProvider.SnoozeOption;

import static home.westering56.taskbox.SnoozeOptionProvider.AFTERNOON_ID;
import static home.westering56.taskbox.SnoozeOptionProvider.EVENING_ID;
import static home.westering56.taskbox.SnoozeOptionProvider.MORNING_ID;
import static home.westering56.taskbox.SnoozeOptionProvider.NEXT_WEEK_ID;
import static home.westering56.taskbox.SnoozeOptionProvider.WEEKEND_ID;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.temporal.TemporalAdjusters.next;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class SnoozeOptionsAdapterTest {
    private Context mContext;

    @Before
    public void initContext() {
        mContext = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testAdapterHasGoodValuesFor8amMonday() {
        LocalDateTime date = LocalDateTime.now().withHour(8).with(next(MONDAY));

        List<Map<String, Object>> expected = Arrays.asList(
                new SnoozeOption(date.withHour(9).withMinute(0), "This Morning", MORNING_ID).asMap(),
                new SnoozeOption(date.withHour(13).withMinute(0), "This Afternoon", AFTERNOON_ID).asMap(),
                new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID).asMap(),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "This Weekend", WEEKEND_ID).asMap(),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID).asMap()
        );
        SimpleAdapter adapter = SnoozeOptionProvider.newAdapter(mContext);

        assertThat(adapter.getCount(), is(equalTo(expected.size())));
        ArrayList<Map<String, Object>> actual = new ArrayList<>();
        for (int i = 0; i < expected.size(); i++) {
            //noinspection unchecked
            actual.add((Map<String, Object>) adapter.getItem(i));
        }
        assertThat(actual, is(equalTo(expected)));
    }
}
