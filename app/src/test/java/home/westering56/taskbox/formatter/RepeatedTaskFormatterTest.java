package home.westering56.taskbox.formatter;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.dmfs.rfc5545.Weekday;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static home.westering56.taskbox.formatter.RepeatedTaskFormatter.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(AndroidJUnit4.class)
public class RepeatedTaskFormatterTest {

    private Context mContext;

    @Before
    public void getContext() {
        mContext = ApplicationProvider.getApplicationContext();
    }


    @Test
    public void appendByWeekdayParts_null() {
        StringBuilder sb = new StringBuilder();
        RepeatedTaskFormatter.appendByWeekdayParts(null, sb, null);
        assertThat(sb.toString().length(), is(0));
    }

    @Test
    public void appendByWeekdayParts_empty() {
        StringBuilder sb = new StringBuilder();
        RepeatedTaskFormatter.appendByWeekdayParts(null, sb, Collections.emptyList());
        assertThat(sb.toString().length(), is(0));
    }

    @Test
    public void appendByWeekdayParts_one_day() {
        StringBuilder sb = new StringBuilder();
        List<RecurrenceRule.WeekdayNum> testList = new ArrayList<RecurrenceRule.WeekdayNum>() {{
            add(new RecurrenceRule.WeekdayNum(0, Weekday.MO));
        }};
        RepeatedTaskFormatter.appendByWeekdayParts(mContext.getResources(), sb, testList);
        assertThat(sb.toString(), is(" on Monday"));
    }

    @Test
    public void appendByWeekdayParts_multi_day() {
        StringBuilder sb = new StringBuilder();
        List<RecurrenceRule.WeekdayNum> testList = new ArrayList<RecurrenceRule.WeekdayNum>() {{
            add(new RecurrenceRule.WeekdayNum(0, Weekday.SU));
            add(new RecurrenceRule.WeekdayNum(0, Weekday.MO));
        }};
        RepeatedTaskFormatter.appendByWeekdayParts(mContext.getResources(), sb, testList);
        assertThat(sb.toString(), is(" on Sun, Mon"));
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
        RepeatedTaskFormatter.appendByWeekdayParts(mContext.getResources(), sb, testList);
        assertThat(sb.toString(), is(" on Thu, Wed, Sun, Mon"));
    }

    @Test
    public void format_daily() throws InvalidRecurrenceRuleException {
        assertThat(format(mContext, new RecurrenceRule("FREQ=DAILY")), is("Every day"));
    }

    @Test
    public void format_multi_daily() throws InvalidRecurrenceRuleException {
        assertThat(format(mContext, new RecurrenceRule("FREQ=DAILY;INTERVAL=3")), is("Every 3 days"));
    }

    @Test
    public void format_weekly() throws InvalidRecurrenceRuleException {
        assertThat(format(mContext, new RecurrenceRule("FREQ=WEEKLY")), is("Every week"));
    }

    @Test
    public void format_multi_weekly() throws InvalidRecurrenceRuleException {
        assertThat(format(mContext, new RecurrenceRule("FREQ=WEEKLY;INTERVAL=10")), is("Every 10 weeks"));
    }

    @Test
    public void format_multi_weekly_with_pattern() throws InvalidRecurrenceRuleException {
        assertThat(format(mContext, new RecurrenceRule("FREQ=WEEKLY;INTERVAL=2;BYDAY=TH")), is("Every 2 weeks on Thursday"));
    }

    @Test
    public void format_multi_weekly_with_pattern2() throws InvalidRecurrenceRuleException {
        assertThat(format(mContext, new RecurrenceRule("FREQ=WEEKLY;INTERVAL=2;BYDAY=TU,TH")), is("Every 2 weeks on Tue, Thu"));
    }

    @Test
    public void format_monthly() throws InvalidRecurrenceRuleException {
        assertThat(format(mContext, new RecurrenceRule("FREQ=MONTHLY")), is("Every month"));
    }

    @Test
    public void format_multi_monthly() throws InvalidRecurrenceRuleException {
        assertThat(format(mContext, new RecurrenceRule("FREQ=MONTHLY;INTERVAL=42")), is("Every 42 months"));
    }

    @Test
    public void format_yearly() throws InvalidRecurrenceRuleException {
        assertThat(format(mContext, new RecurrenceRule("FREQ=YEARLY")), is("Every year"));
    }

    @Test
    public void format_multi_yearly() throws InvalidRecurrenceRuleException {
        assertThat(format(mContext, new RecurrenceRule("FREQ=YEARLY;INTERVAL=4")), is("Every 4 years"));
    }

    @Test
    public void format_monthly_every_30th() throws InvalidRecurrenceRuleException {
        assertThat(format(mContext, new RecurrenceRule("FREQ=MONTHLY;BYMONTHDAY=30")), is("Every month on the 30th"));
    }

    @Test
    public void format_3_monthly_every_last_day() throws InvalidRecurrenceRuleException {
        assertThat(format(mContext, new RecurrenceRule("FREQ=MONTHLY;INTERVAL=3;BYMONTHDAY=-1")), is("Every 3 months on the last day of the month"));
    }

    @Test
    public void format_monthly_every_second_sunday() throws InvalidRecurrenceRuleException {
        assertThat(format(mContext, new RecurrenceRule("FREQ=MONTHLY;BYDAY=2SU")), is("Every month on the second Sunday"));
    }
}