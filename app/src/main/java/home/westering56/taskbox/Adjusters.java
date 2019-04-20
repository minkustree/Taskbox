package home.westering56.taskbox;

import java.time.DayOfWeek;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

@SuppressWarnings("WeakerAccess")
public class Adjusters {
    public static final TemporalAdjuster WeekendAdjuster = new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
            return temporal.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).with(MorningAdjuster);
        }
    };
    public static final TemporalAdjuster StartOfWeekAdjuster = new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
            return temporal.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).with(MorningAdjuster);
        }
    };
    public static final TemporalAdjuster TopOfTheHourAdjuster = new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
            return temporal
                    .with(ChronoField.MINUTE_OF_HOUR, 0)
                    .with(ChronoField.SECOND_OF_MINUTE, 0)
                    .with(ChronoField.NANO_OF_SECOND, 0);
        }
    };
    public static final TemporalAdjuster EveningAdjuster = new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
            return temporal.with(ChronoField.HOUR_OF_DAY, 18).with(TopOfTheHourAdjuster);
        }
    };
    public static final TemporalAdjuster MorningAdjuster = new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
            return temporal.with(ChronoField.HOUR_OF_DAY, 9).with(TopOfTheHourAdjuster);
        }
    };
    public static final TemporalAdjuster TomorrowMorningAdjuster = new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
            return temporal.plus(1, ChronoUnit.DAYS).with(MorningAdjuster);
        }
    };
    public static final TemporalAdjuster AfternoonAdjuster = new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
            return temporal.with(ChronoField.HOUR_OF_DAY, 13).with(TopOfTheHourAdjuster);
        }
    };
}
