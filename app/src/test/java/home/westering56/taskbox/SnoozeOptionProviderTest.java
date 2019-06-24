package home.westering56.taskbox;

import androidx.annotation.DrawableRes;
import androidx.test.filters.SmallTest;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import home.westering56.taskbox.SnoozeOptionProvider.SnoozeOption;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.TemporalAdjusters.next;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;


@SmallTest
public class SnoozeOptionProviderTest {

    private static final @DrawableRes int MORNING_ID = R.drawable.ic_morning_24dp;
    private static final @DrawableRes int AFTERNOON_ID = R.drawable.ic_restaurant_black_24dp;
    private static final @DrawableRes int EVENING_ID = R.drawable.ic_hot_tub_black_24dp;
    private static final @DrawableRes int WEEKEND_ID = R.drawable.ic_weekend_black_24dp;
    private static final @DrawableRes int NEXT_WEEK_ID = R.drawable.ic_next_week_black_24dp;

    private static final CharSequence THIS_MORNING = "This Morning";
    private static final CharSequence TOMORROW_MORNING = "Tomorrow Morning";
    private static final CharSequence THIS_AFTERNOON = "This Afternoon";
    private static final CharSequence TOMORROW_AFTERNOON = "Tomorrow Afternoon";
    private static final CharSequence THIS_EVENING = "This Evening";
    private static final CharSequence TOMORROW_EVENING = "Tomorrow Evening";
    private static final CharSequence THIS_WEEKEND = "This Weekend";
    private static final CharSequence NEXT_WEEKEND = "Next Weekend";
    private static final CharSequence NEXT_WEEK = "Next Week";

    private LocalDateTime date;

    @Before
    public void initialiseDate() {
        date = LocalDateTime.now().truncatedTo(HOURS);
    }

    @Test
    public void expectedTimesForMonday8am() {
        date = date.withHour(8).with(next(MONDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(9).withMinute(0), THIS_MORNING, MORNING_ID),
                new SnoozeOption(date.withHour(13).withMinute(0), THIS_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), THIS_WEEKEND, WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday11am() {
        date = date.withHour(11).with(next(MONDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
            new SnoozeOption(date.withHour(13).withMinute(0), THIS_AFTERNOON, AFTERNOON_ID),
            new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
            new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), TOMORROW_MORNING, MORNING_ID),
            new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), THIS_WEEKEND, WEEKEND_ID),
            new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday3pm() {
        date = date.withHour(15).with(next(MONDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), TOMORROW_MORNING, MORNING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(13).withMinute(0), TOMORROW_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), THIS_WEEKEND, WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday9pm() {
        date = date.withHour(21).with(next(MONDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), TOMORROW_MORNING, MORNING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(13).withMinute(0), TOMORROW_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(18).withMinute(0), TOMORROW_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), THIS_WEEKEND, WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForTuesdayMidnight() {
        date = date.withHour(0).with(next(TUESDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(9).withMinute(0), THIS_MORNING, MORNING_ID),
                new SnoozeOption(date.withHour(13).withMinute(0), THIS_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), THIS_WEEKEND, WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday9am() {
        date = date.withHour(9).with(next(MONDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(13).withMinute(0), THIS_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), TOMORROW_MORNING, MORNING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), THIS_WEEKEND, WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID)
        ))));
    }

    // Expected times just before, on and just after one of our changeover periods

    @Test
    public void expectedTimesForMonday1pmPrecisely() {
        date = date.withHour(13).with(next(MONDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), TOMORROW_MORNING, MORNING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(13).withMinute(0), TOMORROW_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), THIS_WEEKEND, WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday1pmPlusSeconds() {
        date = date.withHour(13).with(next(MONDAY)).plus(30, SECONDS);

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);
        date = date.truncatedTo(MINUTES); // needed to ignore the seconds part

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), TOMORROW_MORNING, MORNING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(13).withMinute(0), TOMORROW_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), THIS_WEEKEND, WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday1pmPlusMinutes() {
        date = date.withHour(13).with(next(MONDAY)).plus(1, MINUTES);

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), TOMORROW_MORNING, MORNING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(13).withMinute(0), TOMORROW_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), THIS_WEEKEND, WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday1pmMinusWithinTolerance() {
        date = date.withHour(13).with(next(MONDAY)).minus(30, SECONDS);

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);
        date = date.truncatedTo(MINUTES); // needed to ignore the seconds part

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), TOMORROW_MORNING, MORNING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(13).withMinute(0), TOMORROW_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), THIS_WEEKEND, WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday1pmMinusBeyondTolerance() {
        date = date.withHour(13).with(next(MONDAY)).minus(6, MINUTES);

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(13).withMinute(0), THIS_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), TOMORROW_MORNING, MORNING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), THIS_WEEKEND, WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday1pmMinusAtTolerance() {
        date = date.withHour(13).with(next(MONDAY)).minus(5, MINUTES);

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), TOMORROW_MORNING, MORNING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(13).withMinute(0), TOMORROW_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), THIS_WEEKEND, WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID)
        ))));
    }



    // if time is 3pm on Thursday, show:
    // * This Evening
    // * Tomorrow Morning
    // * Tomorrow Afternoon
    // * This Weekend
    // * Next Week
    @Test
    public void expectedTimesForThursday3pm() {
        date = date.withHour(15).with(next(THURSDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(FRIDAY)).withHour(9).withMinute(0), TOMORROW_MORNING, MORNING_ID),
                new SnoozeOption(date.with(next(FRIDAY)).withHour(13).withMinute(0), TOMORROW_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), THIS_WEEKEND, WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForFriday3pm() {
        date = date.withHour(15).with(next(FRIDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), TOMORROW_MORNING, MORNING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(13).withMinute(0), TOMORROW_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID),
                new SnoozeOption(date.with(next(SATURDAY)).plus(1, WEEKS).withHour(9).withMinute(0), NEXT_WEEKEND, WEEKEND_ID)
        ))));
    }

    @Test
    public void expectedTimesForFriday9pm() {
        date = date.withHour(21).with(next(FRIDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), TOMORROW_MORNING, MORNING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(13).withMinute(0), TOMORROW_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(18).withMinute(0), TOMORROW_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID),
                new SnoozeOption(date.with(next(SATURDAY)).plus(1, WEEKS).withHour(9).withMinute(0), NEXT_WEEKEND, WEEKEND_ID)
        ))));
    }

    @Test
    public void expectedTimesForSaturday7am() {
        // Checking that 'this morning' is Sat at 9am, and 'next weekend' is the following week (not 'this weekend')
        date = date.withHour(7).with(next(SATURDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(9).withMinute(0), THIS_MORNING, MORNING_ID),
                new SnoozeOption(date.withHour(13).withMinute(0), THIS_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), NEXT_WEEKEND, WEEKEND_ID)
        ))));
    }

    @Test
    public void expectedTimesForSaturday10am() {
        // Checking that 'this morning' is Sat at 9am, and 'next weekend' is the following week (not 'this weekend')
        date = date.withHour(10).with(next(SATURDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(13).withMinute(0), THIS_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.withHour(9).withMinute(0).plus(1, DAYS), TOMORROW_MORNING, MORNING_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), NEXT_WEEKEND, WEEKEND_ID)
        ))));
    }


    @Test
    public void expectedTimesForSunday7am() {
        // Check that weekend slot is 'next weekend' rather than 'this weekend'
        date = date.withHour(7).with(next(SUNDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(9).withMinute(0), THIS_MORNING, MORNING_ID),
                new SnoozeOption(date.withHour(13).withMinute(0), THIS_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), NEXT_WEEKEND, WEEKEND_ID)
        ))));
    }

    @Test
    public void expectedTimesForSunday10am() {
        // Check correct handling of 'next week' and 'tomorrow morning'
        date = date.withHour(10).with(next(SUNDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(13).withMinute(0), THIS_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.plus(1, DAYS).withHour(9).withMinute(0), TOMORROW_MORNING, MORNING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), NEXT_WEEKEND, WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).plus(1, WEEKS).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForSaturday856am() {
        // check correct handling of 'next weekend' when we're within tolerance

        // Checking that 'this morning' is Sat at 9am, and 'next weekend' is the following week (not 'this weekend')
        date = date.withHour(8).withMinute(56).with(next(SATURDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(13).withMinute(0), THIS_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.withHour(18).withMinute(0), THIS_EVENING, EVENING_ID),
                new SnoozeOption(date.plus(1, DAYS).withHour(9).withMinute(0), TOMORROW_MORNING, MORNING_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), NEXT_WEEKEND, WEEKEND_ID)
        ))));
    }

    @Test
    public void expectedTimesForFriday1756am() {
        // check correct handling of 'next weekend' when we're within tolerance, should be the same as friday after 6pm

        // Checking that 'this morning' is Sat at 9am, and 'next weekend' is the following week (not 'this weekend')
        date = date.withHour(17).withMinute(56).with(next(FRIDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.plus(1, DAYS).withHour(9).withMinute(0), TOMORROW_MORNING, MORNING_ID),
                new SnoozeOption(date.plus(1, DAYS).withHour(13).withMinute(0), TOMORROW_AFTERNOON, AFTERNOON_ID),
                new SnoozeOption(date.plus(1, DAYS).withHour(18).withMinute(0), TOMORROW_EVENING, EVENING_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), NEXT_WEEK, NEXT_WEEK_ID),
                new SnoozeOption(date.with(next(SATURDAY)).plus(1, WEEKS).withHour(9).withMinute(0), NEXT_WEEKEND, WEEKEND_ID)
        ))));
    }

}
