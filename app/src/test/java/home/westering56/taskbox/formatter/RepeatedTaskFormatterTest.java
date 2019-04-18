package home.westering56.taskbox.formatter;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.dmfs.rfc5545.Weekday;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(RobolectricTestRunner.class)
public class RepeatedTaskFormatterTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

//    @Test
//    public void format() {
//    }

    @Test
    public void appendByWeekdayParts_null() {
        StringBuilder sb = new StringBuilder();
        RepeatedTaskFormatter.appendByWeekdayParts(null, sb, null);
        assertThat(sb.toString().length(), is(0));
    }

    @Test
    public void appendByWeekdayParts_empty() {
        StringBuilder sb = new StringBuilder();
        List<RecurrenceRule.WeekdayNum> testList = new ArrayList<RecurrenceRule.WeekdayNum>();
        RepeatedTaskFormatter.appendByWeekdayParts(null, sb, testList);
        assertThat(sb.toString().length(), is(0));
    }

    @Test
    public void appendByWeekdayParts_one_day() {
        StringBuilder sb = new StringBuilder();
        List<RecurrenceRule.WeekdayNum> testList = new ArrayList<RecurrenceRule.WeekdayNum>() {{
            add(new RecurrenceRule.WeekdayNum(0, Weekday.MO));
        }};
        Context c = ApplicationProvider.getApplicationContext();
        RepeatedTaskFormatter.appendByWeekdayParts(c.getResources(), sb, testList);
        assertThat(sb.toString(), is("on Monday"));
    }

    @Test
    public void appendByWeekdayParts_multi_day() {
        StringBuilder sb = new StringBuilder();
        List<RecurrenceRule.WeekdayNum> testList = new ArrayList<RecurrenceRule.WeekdayNum>() {{
            add(new RecurrenceRule.WeekdayNum(0, Weekday.SU));
            add(new RecurrenceRule.WeekdayNum(0, Weekday.MO));
        }};
        Context c = ApplicationProvider.getApplicationContext();
        RepeatedTaskFormatter.appendByWeekdayParts(c.getResources(), sb, testList);
        assertThat(sb.toString(), is("on Sun, Mon"));
    }

    @Test
    public void appendByWeekdayParts_days_are_ordered() {
        StringBuilder sb = new StringBuilder();
        List<RecurrenceRule.WeekdayNum> testList = new ArrayList<RecurrenceRule.WeekdayNum>() {{
            add(new RecurrenceRule.WeekdayNum(0, Weekday.TH));
            add(new RecurrenceRule.WeekdayNum(0, Weekday.WE));
            add(new RecurrenceRule.WeekdayNum(0, Weekday.SU));
            add(new RecurrenceRule.WeekdayNum(0, Weekday.MO));
        }};
        Context c = ApplicationProvider.getApplicationContext();
        RepeatedTaskFormatter.appendByWeekdayParts(c.getResources(), sb, testList);
        assertThat(sb.toString(), is("on Thu, Wed, Sun, Mon"));
    }

}