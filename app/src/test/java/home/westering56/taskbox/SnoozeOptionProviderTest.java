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

    /*
     * If time is 7pm on a Monday, show:
     * Tomorrow Morning
     * Tomorrow Afternoon
     * Tomorrow Evening
     * This weekend
     * Next Week
     */


    private LocalDateTime date;

    private static final @DrawableRes int MORNING_ID = R.drawable.ic_morning_24dp;
    private static final @DrawableRes int AFTERNOON_ID = R.drawable.ic_restaurant_black_24dp;
    private static final @DrawableRes int EVENING_ID = R.drawable.ic_hot_tub_black_24dp;
    private static final @DrawableRes int WEEKEND_ID = R.drawable.ic_weekend_black_24dp;
    private static final @DrawableRes int NEXT_WEEK_ID = R.drawable.ic_next_week_black_24dp;

    @Before
    public void initialiseDate() {
        date = LocalDateTime.now().truncatedTo(HOURS);
    }

    @Test
    public void expectedTimesForMonday8am() {
        date = date.withHour(8).with(next(MONDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(9).withMinute(0), "This Morning", MORNING_ID),
                new SnoozeOption(date.withHour(13).withMinute(0), "This Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "This Weekend", WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday11am() {
        date = date.withHour(11).with(next(MONDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
            new SnoozeOption(date.withHour(13).withMinute(0), "This Afternoon", AFTERNOON_ID),
            new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID),
            new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), "Tomorrow Morning", MORNING_ID),
            new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "This Weekend", WEEKEND_ID),
            new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday3pm() {
        date = date.withHour(15).with(next(MONDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), "Tomorrow Morning", MORNING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(13).withMinute(0), "Tomorrow Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "This Weekend", WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday9pm() {
        date = date.withHour(21).with(next(MONDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), "Tomorrow Morning", MORNING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(13).withMinute(0), "Tomorrow Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(18).withMinute(0), "Tomorrow Evening", EVENING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "This Weekend", WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForTuesdayMidnight() {
        date = date.withHour(0).with(next(TUESDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(9).withMinute(0), "This Morning", MORNING_ID),
                new SnoozeOption(date.withHour(13).withMinute(0), "This Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "This Weekend", WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday9am() {
        date = date.withHour(9).with(next(MONDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(13).withMinute(0), "This Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), "Tomorrow Morning", MORNING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "This Weekend", WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID)
        ))));
    }

    // Expected times just before, on and just after one of our changeover periods

    @Test
    public void expectedTimesForMonday1pmPrecisely() {
        date = date.withHour(13).with(next(MONDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), "Tomorrow Morning", MORNING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(13).withMinute(0), "Tomorrow Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "This Weekend", WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday1pmPlusSeconds() {
        date = date.withHour(13).with(next(MONDAY)).plus(30, SECONDS);

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);
        date = date.truncatedTo(MINUTES); // needed to ignore the seconds part

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), "Tomorrow Morning", MORNING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(13).withMinute(0), "Tomorrow Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "This Weekend", WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday1pmPlusMinutes() {
        date = date.withHour(13).with(next(MONDAY)).plus(1, MINUTES);

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), "Tomorrow Morning", MORNING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(13).withMinute(0), "Tomorrow Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "This Weekend", WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday1pmMinusWithinTolerance() {
        date = date.withHour(13).with(next(MONDAY)).minus(30, SECONDS);

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);
        date = date.truncatedTo(MINUTES); // needed to ignore the seconds part

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), "Tomorrow Morning", MORNING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(13).withMinute(0), "Tomorrow Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "This Weekend", WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday1pmMinusBeyondTolerance() {
        date = date.withHour(13).with(next(MONDAY)).minus(6, MINUTES);

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(13).withMinute(0), "This Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), "Tomorrow Morning", MORNING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "This Weekend", WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForMonday1pmMinusAtTolerance() {
        date = date.withHour(13).with(next(MONDAY)).minus(5, MINUTES);

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(9).withMinute(0), "Tomorrow Morning", MORNING_ID),
                new SnoozeOption(date.with(next(TUESDAY)).withHour(13).withMinute(0), "Tomorrow Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "This Weekend", WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID)
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
                new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID),
                new SnoozeOption(date.with(next(FRIDAY)).withHour(9).withMinute(0), "Tomorrow Morning", MORNING_ID),
                new SnoozeOption(date.with(next(FRIDAY)).withHour(13).withMinute(0), "Tomorrow Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "This Weekend", WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID)
        ))));
    }

    @Test
    public void expectedTimesForFriday3pm() {
        date = date.withHour(15).with(next(FRIDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "Tomorrow Morning", MORNING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(13).withMinute(0), "Tomorrow Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID),
                new SnoozeOption(date.with(next(SATURDAY)).plus(1, WEEKS).withHour(9).withMinute(0), "Next Weekend", WEEKEND_ID)
        ))));
    }

    @Test
    public void expectedTimesForFriday9pm() {
        date = date.withHour(21).with(next(FRIDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "Tomorrow Morning", MORNING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(13).withMinute(0), "Tomorrow Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(18).withMinute(0), "Tomorrow Evening", EVENING_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID),
                new SnoozeOption(date.with(next(SATURDAY)).plus(1, WEEKS).withHour(9).withMinute(0), "Next Weekend", WEEKEND_ID)
        ))));
    }

    @Test
    public void expectedTimesForSaturday7am() {
        // Checking that 'this morning' is Sat at 9am, and 'next weekend' is the following week (not 'this weekend')
        date = date.withHour(7).with(next(SATURDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(9).withMinute(0), "This Morning", MORNING_ID),
                new SnoozeOption(date.withHour(13).withMinute(0), "This Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "Next Weekend", WEEKEND_ID)
        ))));
    }

    @Test
    public void expectedTimesForSunday7am() {
        // Check that weekend slot is 'next weekend' rather than 'this weekend'
        date = date.withHour(7).with(next(SUNDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(9).withMinute(0), "This Morning", MORNING_ID),
                new SnoozeOption(date.withHour(13).withMinute(0), "This Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID),
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "Next Weekend", WEEKEND_ID)
        ))));
    }

    @Test
    public void expectedTimesForSunday10am() {
        // Check correct handling of 'next week' and 'tomorrow morning'
        date = date.withHour(10).with(next(SUNDAY));

        List<SnoozeOption> options = SnoozeOptionProvider.getSnoozeOptionsForDateTime(date);

        assertThat(options, is(equalTo(Arrays.asList(
                new SnoozeOption(date.withHour(13).withMinute(0), "This Afternoon", AFTERNOON_ID),
                new SnoozeOption(date.withHour(18).withMinute(0), "This Evening", EVENING_ID),
                new SnoozeOption(date.plus(1, DAYS).withHour(9).withMinute(0), "Tomorrow Morning", MORNING_ID),
                new SnoozeOption(date.with(next(SATURDAY)).withHour(9).withMinute(0), "Next Weekend", WEEKEND_ID),
                new SnoozeOption(date.with(next(MONDAY)).plus(1, WEEKS).withHour(9).withMinute(0), "Next Week", NEXT_WEEK_ID)
        ))));
    }

    // TODO: Test cases for times within tolerance of 'next week' / 'weekend' corner cases

    // TODO: Extract more test cases from the old label tests below, e.g. 'next week' / 'tomorrow morning' / 'this morning' corner cases.
/*

    static LocalDate anyMonday = LocalDate.of(2019, 1, 1).with(nextOrSame(MONDAY));
    static LocalDate anyFriday = LocalDate.of(2019, 1, 1).with(nextOrSame(FRIDAY));
    static LocalDate anySaturday = LocalDate.of(2019, 1, 1).with(nextOrSame(SATURDAY));
    static LocalDate anySunday = LocalDate.of(2019, 1, 1).with(nextOrSame(SUNDAY));

    @Test
    public void labelForThisMorning() {
        LocalTime originTime = LocalTime.of(7, 30);
        LocalDateTime origin = LocalDateTime.of(anyMonday, originTime);

        CharSequence label = SnoozeOptionProvider.getLabelForOptionDateTime(origin, origin.with(NextMorning));
        assertThat(label, is("This Morning"));
    }

    @Test
    public void labelForTomorrowMorning() {
        LocalTime originTime = LocalTime.of(22, 30);
        LocalDateTime origin = LocalDateTime.of(anyMonday, originTime);

        CharSequence label = SnoozeOptionProvider.getLabelForOptionDateTime(origin, origin.with(NextMorning));
        assertThat(label, is("Tomorrow Morning"));
    }

    @Test
    public void labelForTomorrowMorningOnFridayNight() {
        LocalTime originTime = LocalTime.of(22, 30);
        LocalDateTime origin = LocalDateTime.of(anyFriday, originTime);

        CharSequence label = SnoozeOptionProvider.getLabelForOptionDateTime(origin, origin.with(NextMorning));
        assertThat(label, is("Tomorrow Morning"));
        assertThat(label, is(not("This Weekend")));
    }

    @Test
    public void labelForThisMorningOnEarlySaturday() {
        LocalTime originTime = LocalTime.of(22, 30);
        LocalDateTime origin = LocalDateTime.of(anyFriday, originTime);

        CharSequence label = SnoozeOptionProvider.getLabelForOptionDateTime(origin, origin.with(NextMorning));
        assertThat(label, is("Tomorrow Morning"));
        assertThat(label, is(not("This Weekend")));
    }

    @Test
    public void labelForThisAfternoon() {
        LocalTime originTime = LocalTime.of(7, 30);
        LocalDateTime origin = LocalDateTime.of(anyMonday, originTime);

        CharSequence label = SnoozeOptionProvider.getLabelForOptionDateTime(origin, origin.with(NextAfternoon));
        assertThat(label, is("This Afternoon"));
    }

    @Test
    public void labelForTomorrowAfternoon() {
        LocalTime originTime = LocalTime.of(22, 30);
        LocalDateTime origin = LocalDateTime.of(anyMonday, originTime);

        CharSequence label = SnoozeOptionProvider.getLabelForOptionDateTime(origin, origin.with(NextAfternoon));
        assertThat(label, is("Tomorrow Afternoon"));
    }

    @Test
    public void labelForTomorrowAfternoonSat() {
        LocalTime originTime = LocalTime.of(22, 30);
        LocalDateTime origin = LocalDateTime.of(anySaturday, originTime);

        CharSequence label = SnoozeOptionProvider.getLabelForOptionDateTime(origin, origin.with(NextAfternoon));
        assertThat(label, is("Tomorrow Afternoon"));
    }

    @Test
    public void labelForThisWeekend() {
        LocalTime originTime = LocalTime.of(22, 30);
        LocalDateTime origin = LocalDateTime.of(anyFriday, originTime);

        CharSequence label = SnoozeOptionProvider.getLabelForOptionDateTime(origin, origin.with(WeekendAdjuster));
        assertThat(label, is("This Weekend"));
    }

    @Test
    public void labelForThisWeekendSatAM() {
        LocalTime originTime = LocalTime.of(07, 30);
        LocalDateTime origin = LocalDateTime.of(anySaturday, originTime);

        CharSequence label = SnoozeOptionProvider.getLabelForOptionDateTime(origin, origin.with(WeekendAdjuster));
        assertThat(label, is("This Weekend"));
    }

    @Test
    public void labelForNextWeekendSatPM() {
        LocalTime originTime = LocalTime.of(15, 30);
        LocalDateTime origin = LocalDateTime.of(anySaturday, originTime);

        CharSequence label = SnoozeOptionProvider.getLabelForOptionDateTime(origin, origin.with(WeekendAdjuster));
        assertThat(label, is("Next Weekend"));
    }

    @Test
    public void labelForNextWeekendSunPM() {
        LocalTime originTime = LocalTime.of(13, 00);
        LocalDateTime origin = LocalDateTime.of(anySunday, originTime);

        CharSequence label = SnoozeOptionProvider.getLabelForOptionDateTime(origin, origin.with(WeekendAdjuster));
        assertThat(label, is("Next Weekend"));
    }

    @Test
    public void labelForNextWeek() {
        LocalTime originTime = LocalTime.of(22, 30);
        LocalDateTime origin = LocalDateTime.of(anyFriday, originTime);

        CharSequence label = SnoozeOptionProvider.getLabelForOptionDateTime(origin, origin.with(StartOfWeekAdjuster));
        assertThat(label, is("Next Week"));
    }

    @Test
    public void labelForNextWeekSunAm() {
        LocalTime originTime = LocalTime.of(10, 30);
        LocalDateTime origin = LocalDateTime.of(anySunday, originTime);

        CharSequence label = SnoozeOptionProvider.getLabelForOptionDateTime(origin, origin.with(StartOfWeekAdjuster));
        assertThat(label, is("Next Week"));
    }

    @Test
    public void labelForNextWeekSunEve() {
        LocalTime originTime = LocalTime.of(22, 30);
        LocalDateTime origin = LocalDateTime.of(anySunday, originTime);

        CharSequence label = SnoozeOptionProvider.getLabelForOptionDateTime(origin, origin.with(StartOfWeekAdjuster));
        assertThat(label, is("Next Week"));
    }

    @Test
    public void labelForNextWeekMonEarly() {

        LocalTime originTime = LocalTime.of(8, 0);
        LocalDateTime origin = LocalDateTime.of(anyMonday, originTime);

        CharSequence label = SnoozeOptionProvider.getLabelForOptionDateTime(origin, origin.with(StartOfWeekAdjuster));
        assertThat(label, is("Next Week"));
    }

    @Test
    public void labelForNextWeekMon9AM() {
        LocalTime originTime = LocalTime.of(9, 0);
        LocalDateTime origin = LocalDateTime.of(anyMonday, originTime);

        CharSequence label = SnoozeOptionProvider.getLabelForOptionDateTime(origin, origin.with(StartOfWeekAdjuster));
        assertThat(label, is("Next Week"));
    }
*/
}
