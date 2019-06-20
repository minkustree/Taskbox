package home.westering56.taskbox;

import java.time.DayOfWeek;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.TemporalAdjusters.next;


class Adjusters {

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



    static final TemporalAdjuster NextMorning = Next(MorningAdjuster);
    static final TemporalAdjuster NextAfternoon = Next(AfternoonAdjuster);
    static final TemporalAdjuster NextEvening = Next(EveningAdjuster);

    /**
     * Returns an adjuster that makes the specified adjustment, then ensures it happens in the
     * future if needed.
     *
     * Note: This implementation ensures things happen in the future by adding at least a day to the
     * original temporal, then adjusting. It won't therefore work for adjusters that need more fine-
     * grained adjustement than that
     * @param adjuster
     * @return
     */
    static final TemporalAdjuster Next(final TemporalAdjuster adjuster) {
        return (temporal) -> {
            // Temporal implementations MUST implement Comparable, according to
            // https://developer.android.com/reference/java/time/temporal/Temporal
            //noinspection unchecked
            final Comparable<Temporal> adjustedTemporal = (Comparable<Temporal>) temporal.with(adjuster);
            if (adjustedTemporal.compareTo(temporal) <= 0) { // adjusted time is before or equal to the original time
                return temporal.plus(1, DAYS).with(adjuster); // find the next one
            } else {
                return temporal.with(adjuster);
            }
        };
    }


}
