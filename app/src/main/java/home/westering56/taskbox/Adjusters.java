package home.westering56.taskbox;

import androidx.annotation.VisibleForTesting;

import java.time.DayOfWeek;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.TemporalAdjusters.next;


class Adjusters {

    // times within this tolerance before a target time will be considered to be at the target time
    private static final long TOLERANCE_MIN = 5;

    private static final TemporalAdjuster TopOfTheHourAdjuster = temporal -> temporal
            .with(ChronoField.MINUTE_OF_HOUR, 0)
            .with(ChronoField.SECOND_OF_MINUTE, 0)
            .with(ChronoField.NANO_OF_SECOND, 0);

    static final TemporalAdjuster MorningAdjuster = temporal -> temporal
            .with(HOUR_OF_DAY, 9)
            .with(TopOfTheHourAdjuster);

    static final TemporalAdjuster AfternoonAdjuster = temporal -> temporal
            .with(HOUR_OF_DAY, 13)
            .with(TopOfTheHourAdjuster);

    static final TemporalAdjuster EveningAdjuster = temporal -> temporal
            .with(HOUR_OF_DAY, 18)
            .with(TopOfTheHourAdjuster);

    static final TemporalAdjuster StartOfWeekAdjuster = temporal -> temporal
            .with(next(DayOfWeek.MONDAY))
            .with(MorningAdjuster);

    static final TemporalAdjuster WeekendAdjuster = temporal -> temporal
             .with(next(DayOfWeek.SATURDAY))
             .with(MorningAdjuster);

    static final TemporalAdjuster NextMorning = Next(MorningAdjuster);
    static final TemporalAdjuster NextAfternoon = Next(AfternoonAdjuster);
    static final TemporalAdjuster NextEvening = Next(EveningAdjuster);

    static final TemporalAdjuster WeekendNotTomorrowMorningAdjuster = NotTomorrowMorning(WeekendAdjuster);
    static final TemporalAdjuster NextWeekNotTomorrowMorningAdjuster = NotTomorrowMorning(StartOfWeekAdjuster);


    /**
     * Returns an adjuster that is guaranteed to adjust to value that is different to what
     * {@link #NextMorning} would adjust to.
     * <p>
     * If the same adjustment would be made, the adjuster will seek to a week's time.
     */
    @VisibleForTesting
    static TemporalAdjuster NotTomorrowMorning(final TemporalAdjuster adjuster) {
        return temporal -> {
            if (temporal.with(NextMorning).equals(temporal.with(adjuster))) {
                return temporal.with(adjuster).plus(1, WEEKS);
            } else {
                return temporal.with(adjuster);
            }
        };
    }

    /**
     * Returns an adjuster that makes the specified adjustment, then ensures it happens in the
     * future if needed.
     * <p>
     * Note: This implementation ensures things happen in the future by adding at least a day to the
     * original temporal, then adjusting. It probably won't work for adjusters that need more fine-
     * grained adjustment than that
     **/
    @VisibleForTesting
    static TemporalAdjuster Next(final TemporalAdjuster adjuster) {
        return temporal -> {
            final Temporal adjustedTemporal = temporal.with(adjuster);
            // if new time is within 5 min of the old time, add a day and try again
            if (MINUTES.between(temporal, adjustedTemporal) <= TOLERANCE_MIN) {
                return temporal.plus(1, DAYS).with(adjuster); // find the next one
            } else {
                return temporal.with(adjuster);
            }
        };
    }

}
