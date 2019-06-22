package home.westering56.taskbox;

import androidx.annotation.DrawableRes;
import androidx.test.filters.SmallTest;

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;

import home.westering56.taskbox.SnoozeOptionProvider.SnoozeOption;

import static home.westering56.taskbox.Adjusters.NextAfternoon;
import static home.westering56.taskbox.Adjusters.NextMorning;
import static home.westering56.taskbox.Adjusters.StartOfWeekAdjuster;
import static home.westering56.taskbox.Adjusters.WeekendAdjuster;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.TemporalAdjusters.next;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

//@RunWith(AndroidJUnit4.class)
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
    private static final @DrawableRes int NEXTWEEK_ID = R.drawable.ic_next_week_black_24dp;

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
                new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXTWEEK_ID)
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
            new SnoozeOption(date.with(next(MONDAY)).withHour(9).withMinute(0), "Next Week", NEXTWEEK_ID)
        ))));
    }
/*
    @Test
    public void expectedTimesForMonday3pm() {
        date = date.withHour(15).with(next(MONDAY));

        List<LocalDateTime> options = SnoozeOptionProvider.getOptionsForDate(date);

        assertThat(options, IsCollectionWithSize.hasSize(5));

        assertThat(options.get(0), equalTo(date.withHour(18).withMinute(0)));
        assertThat(options.get(1), equalTo(date.with(next(TUESDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(2), equalTo(date.with(next(TUESDAY)).withHour(13).withMinute(0)));
        assertThat(options.get(3), equalTo(date.with(next(SATURDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(4), equalTo(date.with(next(MONDAY)).withHour(9).withMinute(0)));

    }

    @Test
    public void expectedTimesForMonday9pm() {
        date = date.withHour(21).with(next(MONDAY));

        List<LocalDateTime> options = SnoozeOptionProvider.getOptionsForDate(date);

        assertThat(options, IsCollectionWithSize.hasSize(5));


        assertThat(options.get(0), equalTo(date.with(next(TUESDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(1), equalTo(date.with(next(TUESDAY)).withHour(13).withMinute(0)));
        assertThat(options.get(2), equalTo(date.with(next(TUESDAY)).withHour(18).withMinute(0)));
        assertThat(options.get(3), equalTo(date.with(next(SATURDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(4), equalTo(date.with(next(MONDAY)).withHour(9).withMinute(0)));

    }

    @Test
    public void expectedTimesForMonday9am() {
        date = date.withHour(9).with(next(MONDAY));

        List<LocalDateTime> options = SnoozeOptionProvider.getOptionsForDate(date);

        assertThat(options, is(equalTo(Arrays.asList(
                date.withHour(13).withMinute(0),
                date.withHour(18).withMinute(0),
                date.with(next(TUESDAY)).withHour(9).withMinute(0),
                date.with(next(SATURDAY)).withHour(9).withMinute(0),
                date.with(next(MONDAY)).withHour(9).withMinute(0))
        )));
    }


    // todo: Expected times just before, on and just after one of our changeover periods

    @Test
    public void expectedTimesForMonday1pmPrecisely() {
        date = date.withHour(13).with(next(MONDAY));

        List<LocalDateTime> options = SnoozeOptionProvider.getOptionsForDate(date);

        assertThat(options, IsCollectionWithSize.hasSize(5));

        assertThat(options.get(0), equalTo(date.withHour(18).withMinute(0)));
        assertThat(options.get(1), equalTo(date.with(next(TUESDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(2), equalTo(date.with(next(TUESDAY)).withHour(13).withMinute(0)));
        assertThat(options.get(3), equalTo(date.with(next(SATURDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(4), equalTo(date.with(next(MONDAY)).withHour(9).withMinute(0)));

    }

    @Test
    public void expectedTimesForMonday1pmPlusSeconds() {
        date = date.withHour(13).with(next(MONDAY)).plus(30, SECONDS);

        List<LocalDateTime> options = SnoozeOptionProvider.getOptionsForDate(date);



        assertThat(options, IsCollectionWithSize.hasSize(5));

        date = date.truncatedTo(MINUTES);
        assertThat(options.get(0), equalTo(date.withHour(18).withMinute(0)));
        assertThat(options.get(1), equalTo(date.with(next(TUESDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(2), equalTo(date.with(next(TUESDAY)).withHour(13).withMinute(0)));
        assertThat(options.get(3), equalTo(date.with(next(SATURDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(4), equalTo(date.with(next(MONDAY)).withHour(9).withMinute(0)));

    }

    @Test
    public void expectedTimesForMonday1pmPlusMinutes() {
        date = date.withHour(13).with(next(MONDAY)).plus(1, MINUTES);

        List<LocalDateTime> options = SnoozeOptionProvider.getOptionsForDate(date);

        assertThat(options, IsCollectionWithSize.hasSize(5));

        assertThat(options.get(0), equalTo(date.withHour(18).withMinute(0)));
        assertThat(options.get(1), equalTo(date.with(next(TUESDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(2), equalTo(date.with(next(TUESDAY)).withHour(13).withMinute(0)));
        assertThat(options.get(3), equalTo(date.with(next(SATURDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(4), equalTo(date.with(next(MONDAY)).withHour(9).withMinute(0)));

    }

    @Test
    public void expectedTimesForMonday1pmMinusWithinTolerance() {
        date = date.withHour(13).with(next(MONDAY)).minus(30, SECONDS);

        List<LocalDateTime> options = SnoozeOptionProvider.getOptionsForDate(date);

        assertThat(options, IsCollectionWithSize.hasSize(5));

        date = date.truncatedTo(MINUTES); // needed to ignore the seconds part

        assertThat(options.get(0), equalTo(date.withHour(18).withMinute(0)));
        assertThat(options.get(1), equalTo(date.with(next(TUESDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(2), equalTo(date.with(next(TUESDAY)).withHour(13).withMinute(0)));
        assertThat(options.get(3), equalTo(date.with(next(SATURDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(4), equalTo(date.with(next(MONDAY)).withHour(9).withMinute(0)));

    }

    @Test
    public void expectedTimesForMonday1pmMinusBeyondTolerance() {
        date = date.withHour(13).with(next(MONDAY)).minus(6, MINUTES);

        List<LocalDateTime> options = SnoozeOptionProvider.getOptionsForDate(date);

        assertThat(options, IsCollectionWithSize.hasSize(5));

        assertThat(options.get(0), equalTo(date.withHour(13).withMinute(0)));
        assertThat(options.get(1), equalTo(date.withHour(18).withMinute(0)));
        assertThat(options.get(2), equalTo(date.with(next(TUESDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(3), equalTo(date.with(next(SATURDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(4), equalTo(date.with(next(MONDAY)).withHour(9).withMinute(0)));

    }

    @Test
    public void expectedTimesForMonday1pmMinusAtTolerance() {
        date = date.withHour(13).with(next(MONDAY)).minus(5, MINUTES);

        List<LocalDateTime> options = SnoozeOptionProvider.getOptionsForDate(date);

        assertThat(options, IsCollectionWithSize.hasSize(5));

        assertThat(options.get(0), equalTo(date.withHour(18).withMinute(0)));
        assertThat(options.get(1), equalTo(date.with(next(TUESDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(2), equalTo(date.with(next(TUESDAY)).withHour(13).withMinute(0)));
        assertThat(options.get(3), equalTo(date.with(next(SATURDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(4), equalTo(date.with(next(MONDAY)).withHour(9).withMinute(0)));
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

        List<LocalDateTime> options = SnoozeOptionProvider.getOptionsForDate(date);

        assertThat(options, IsCollectionWithSize.hasSize(5));

        assertThat(options.get(0), equalTo(date.withHour(18).withMinute(0)));
        assertThat(options.get(1), equalTo(date.with(next(FRIDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(2), equalTo(date.with(next(FRIDAY)).withHour(13).withMinute(0)));
        assertThat(options.get(3), equalTo(date.with(next(SATURDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(4), equalTo(date.with(next(MONDAY)).withHour(9).withMinute(0)));
    }

    // TODO: Should this de-duplicate Tommorrow Morning (Sat, 9am) and 'This Weekend' (Sat, 9am)?
    @Test
    public void expectedTimesForFriday3pm() {
        date = date.withHour(15).with(next(FRIDAY));

        List<LocalDateTime> options = SnoozeOptionProvider.getOptionsForDate(date);

        assertThat(options, IsCollectionWithSize.hasSize(5));

        assertThat(options.get(0), equalTo(date.withHour(18).withMinute(0)));
        assertThat(options.get(1), equalTo(date.with(next(SATURDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(2), equalTo(date.with(next(SATURDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(3), equalTo(date.with(next(SATURDAY)).withHour(13).withMinute(0)));
        assertThat(options.get(4), equalTo(date.with(next(MONDAY)).withHour(9).withMinute(0)));
    }

    @Test
    public void expectedTimesForFriday9pm() {
        date = date.withHour(21).with(next(FRIDAY));

        List<LocalDateTime> options = SnoozeOptionProvider.getOptionsForDate(date);

        assertThat(options, is(equalTo(Arrays.asList(
                date.with(next(SATURDAY)).withHour(9).withMinute(0), // Tomorrow Morning
                date.with(next(SATURDAY)).withHour(13).withMinute(0), // Tomorrow Afternoon
                date.with(next(SATURDAY)).withHour(18).withMinute(0), // Tomorrow Evening
                date.with(next(MONDAY)).withHour(9).withMinute(0), // Next Week
                date.with(next(SATURDAY)).plusWeeks(1).withHour(9).withMinute(0) // Next Weekend
        ))));
    }


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
