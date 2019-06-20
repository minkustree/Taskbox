package home.westering56.taskbox;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;
import java.util.List;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.TemporalAdjusters.next;
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

    @Before
    public void initialiseDate() {
        date = LocalDateTime.now().truncatedTo(HOURS);
    }

    @Test
    public void expectedTimesForMonday8am() {
        date = date.withHour(8).with(next(MONDAY));

        List<LocalDateTime> options = SnoozeOptionProvider.getOptionsForDate(date);

        assertThat(options, IsCollectionWithSize.hasSize(5));
        assertThat(options.get(0), equalTo(date.withHour(9).withMinute(0)));
        assertThat(options.get(1), equalTo(date.withHour(13).withMinute(0)));
        assertThat(options.get(2), equalTo(date.withHour(18).withMinute(0)));
        assertThat(options.get(3), equalTo(date.with(next(SATURDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(4), equalTo(date.with(next(MONDAY)).withHour(9).withMinute(0))); // next monday is a week away - c.f. nextOrSame()

    }

    @Test
    public void expectedTimesForMonday11am() {
        date = date.withHour(11).with(next(MONDAY));

        List<LocalDateTime> options = SnoozeOptionProvider.getOptionsForDate(date);

        assertThat(options, IsCollectionWithSize.hasSize(5));

        assertThat(options.get(0), equalTo(date.withHour(13).withMinute(0)));
        assertThat(options.get(1), equalTo(date.withHour(18).withMinute(0)));
        assertThat(options.get(2), equalTo(date.with(next(TUESDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(3), equalTo(date.with(next(SATURDAY)).withHour(9).withMinute(0)));
        assertThat(options.get(4), equalTo(date.with(next(MONDAY)).withHour(9).withMinute(0)));

    }

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
}
