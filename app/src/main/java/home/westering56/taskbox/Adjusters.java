package home.westering56.taskbox;

import org.dmfs.rfc5545.Weekday;

import java.time.DayOfWeek;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.TemporalAdjusters.next;
import static java.time.temporal.TemporalAdjusters.nextOrSame;


class Adjusters {

    // times within this tolerance before a target time will be considered to be at the target time
    private static final long TOLERANCE_MINS = 5;

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

    static final TemporalAdjuster TomorrowMorningAdjuster = temporal -> temporal
            .plus(1, DAYS)
            .with(MorningAdjuster);

    static final TemporalAdjuster StartOfWeekAdjuster = temporal -> temporal
            .with(next(DayOfWeek.MONDAY))
            .with(MorningAdjuster);

    static final TemporalAdjuster WeekendAdjuster = temporal -> temporal
             .with(next(DayOfWeek.SATURDAY))
             .with(MorningAdjuster);

    static final TemporalAdjuster WeekendNotTomorrowMorningAdjuster = temporal -> {
        if (temporal.with(TomorrowMorningAdjuster).equals(temporal.with(WeekendAdjuster))) {
            return temporal.with(WeekendAdjuster).plus(1, WEEKS);
        } else {
            return temporal.with(WeekendAdjuster);
        }
    };


    static final TemporalAdjuster NextMorning = Next(MorningAdjuster);
    static final TemporalAdjuster NextAfternoon = Next(AfternoonAdjuster);
    static final TemporalAdjuster NextEvening = Next(EveningAdjuster);

    /**
     * Returns an adjuster that makes the specified adjustment, then ensures it happens in the
     * future if needed.
     * <p>
     * Note: This implementation ensures things happen in the future by adding at least a day to the
     * original temporal, then adjusting. It probably won't work for adjusters that need more fine-
     * grained adjustement than that
     **/
    static TemporalAdjuster Next(final TemporalAdjuster adjuster) {
        return temporal -> {
            final Temporal adjustedTemporal = temporal.with(adjuster);
            // if new time is within 5 mins of the old time, add a day and try again
            if (MINUTES.between(temporal, adjustedTemporal) <= TOLERANCE_MINS) {
                return temporal.plus(1, DAYS).with(adjuster); // find the next one
            } else {
                return temporal.with(adjuster);
            }
        };
    }

}
