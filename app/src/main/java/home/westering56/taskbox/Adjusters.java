package home.westering56.taskbox;

import java.time.DayOfWeek;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;

import static java.time.temporal.TemporalAdjusters.next;


class Adjusters {

    private static final TemporalAdjuster TopOfTheHourAdjuster = temporal -> temporal
            .with(ChronoField.MINUTE_OF_HOUR, 0)
            .with(ChronoField.SECOND_OF_MINUTE, 0)
            .with(ChronoField.NANO_OF_SECOND, 0);

    static final TemporalAdjuster MorningAdjuster = temporal -> temporal
            .with(ChronoField.HOUR_OF_DAY, 9)
            .with(TopOfTheHourAdjuster);

    static final TemporalAdjuster AfternoonAdjuster = temporal -> temporal
            .with(ChronoField.HOUR_OF_DAY, 13)
            .with(TopOfTheHourAdjuster);

    static final TemporalAdjuster EveningAdjuster = temporal -> temporal
            .with(ChronoField.HOUR_OF_DAY, 18)
            .with(TopOfTheHourAdjuster);

    static final TemporalAdjuster TomorrowMorningAdjuster = temporal -> temporal
            .plus(1, ChronoUnit.DAYS)
            .with(MorningAdjuster);

    static final TemporalAdjuster StartOfWeekAdjuster = temporal -> temporal
            .with(next(DayOfWeek.MONDAY))
            .with(MorningAdjuster);

    static final TemporalAdjuster WeekendAdjuster = temporal -> temporal
            .with(next(DayOfWeek.SATURDAY))
            .with(MorningAdjuster);


}
