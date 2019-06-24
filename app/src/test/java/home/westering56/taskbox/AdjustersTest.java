package home.westering56.taskbox;

import androidx.test.filters.SmallTest;

import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;

import static home.westering56.taskbox.Adjusters.MorningAdjuster;
import static home.westering56.taskbox.Adjusters.Next;
import static home.westering56.taskbox.Adjusters.NextAfternoon;
import static home.westering56.taskbox.Adjusters.NextMorning;
import static home.westering56.taskbox.Adjusters.NotTomorrowMorning;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.WEEKS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

@SmallTest
public class AdjustersTest {

    private static TemporalAdjuster TenAM = temporal -> temporal
            .with(HOUR_OF_DAY, 10)
            .with(MINUTE_OF_HOUR, 0)
            .with(SECOND_OF_MINUTE, 0)
            .with(ChronoField.NANO_OF_SECOND,0);
    private static LocalDate originalDate = LocalDate.of(2019, 4, 30);
    private static LocalTime originalTime = LocalTime.of(3, 14, 15, 9);
    private static LocalTime tenAmTime = LocalTime.of(10, 0, 0, 0);
    private static Temporal original  = LocalDateTime.of(originalDate, originalTime);

    @BeforeClass
    public static void testTenAMAdjusterFunctionsCorrectly() {
        assertThat(original.with(TenAM), is(equalTo(LocalDateTime.of(originalDate, tenAmTime))));
    }

    @Test
    public void testNextGivesSameDayIfOriginalBeforeTargetTime() {
        assertThat(original.with(Next(TenAM)), is(equalTo(LocalDateTime.of(originalDate, tenAmTime))));
    }

    @Test
    public void testNextGivesNextDayIfOriginalSignificantlyAfterTargetTime() {
        Temporal later = original.plus(12, HOURS);
        assertThat(later.with(Next(TenAM)),
                is(equalTo(LocalDateTime.of(originalDate.plus(1, DAYS), tenAmTime))));
    }

    @Test
    public void testNextGivesNextDayIfOriginalJustAfterTargetTime() {
        Temporal threeMinAfterTenAM = LocalDateTime.of(originalDate, tenAmTime.plus(3, MINUTES));
        assertThat(threeMinAfterTenAM.with(Next(TenAM)),
                is(equalTo(LocalDateTime.of(originalDate.plus(1, DAYS), tenAmTime))));
    }

    @Test
    public void testNextGivesSameDayIfOriginalSignificantlyBeforeTargetTime() {
        Temporal sevenMinBeforeTenAm = LocalDateTime.of(originalDate, tenAmTime).minus(7, MINUTES);
        assertThat(sevenMinBeforeTenAm.with(Next(TenAM)),
                is(equalTo(LocalDateTime.of(originalDate, tenAmTime))));
    }

    @Test
    public void testNextGivesNextDayIfOriginalJustBeforeTargetTime() {
        Temporal twoMinBeforeTenAm = LocalDateTime.of(originalDate, tenAmTime).minus(2, MINUTES);
        assertThat(twoMinBeforeTenAm.with(Next(TenAM)),
                is(equalTo(LocalDateTime.of(originalDate.plus(1, DAYS), tenAmTime))));
    }

    @Test
    public void testNextGivesNextDayIfOriginalOnTheBoundaryBeforeTargetTime() {
        Temporal fiveMinBeforeTenAm = LocalDateTime.of(originalDate, tenAmTime).minus(5, MINUTES);
        assertThat(fiveMinBeforeTenAm.with(Next(TenAM)),
                is(equalTo(LocalDateTime.of(originalDate.plus(1, DAYS), tenAmTime))));
    }

    @Test
    public void testNotTomorrowMorningAddsAWeekIfItWouldCollideWithNextMorning() {
        // Without NotTomorrowMorning, the two would be the same
        assertThat(original.with(NextMorning), is(equalTo(original.with(MorningAdjuster))));
        // With NotTomorrowMorning, the two differ by one week
        assertThat(original.with(NotTomorrowMorning(NextMorning)),
                is(equalTo(original.with(MorningAdjuster).plus(1, WEEKS))));
    }

    @Test
    public void testNotTomorrowMorningPassesThroughNonCollidingCases() {
        // Without NotTomorrowMorning, the two are different
        assertThat(original.with(NextAfternoon), is(not(equalTo(original.with(MorningAdjuster)))));
        // In this case, NotTomorrowMorning has no effect
        assertThat(original.with(NotTomorrowMorning(NextAfternoon)), is(equalTo(original.with(NextAfternoon))));
    }
}