package home.westering56.taskbox;

import android.content.Context;
import android.widget.SimpleAdapter;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import home.westering56.taskbox.SnoozeOptionProvider.SnoozeOption;

import static home.westering56.taskbox.SnoozeOptionProvider.AFTERNOON_ID;
import static home.westering56.taskbox.SnoozeOptionProvider.EVENING_ID;
import static home.westering56.taskbox.SnoozeOptionProvider.MORNING_ID;
import static home.westering56.taskbox.SnoozeOptionProvider.NEXT_WEEK_ID;
import static home.westering56.taskbox.SnoozeOptionProvider.SNOOZE_OPTION_ICON;
import static home.westering56.taskbox.SnoozeOptionProvider.SNOOZE_OPTION_INSTANT;
import static home.westering56.taskbox.SnoozeOptionProvider.SNOOZE_OPTION_TITLE;
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
        for (int i = 0; i < expected.size(); i++) {
            assertThat(adapter.getItem(i), is(equalTo(expected.get(i))));
        }
    }

    private void assertEqual(Map<String, Object> adapterItem, SnoozeOption snoozeOption) {
        assertThat(adapterItem.get(SNOOZE_OPTION_TITLE), is(equalTo(snoozeOption.label)));
        assertThat(adapterItem.get(SNOOZE_OPTION_ICON), is(snoozeOption.drawableId));
        assertThat(adapterItem.get(SNOOZE_OPTION_INSTANT), is(equalTo(InstantOf(snoozeOption.dateTime))));
    }

    private static Instant InstantOf(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
