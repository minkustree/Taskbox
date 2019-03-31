package home.westering56.taskbox.data.room;

import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.time.Instant;

import androidx.room.TypeConverter;

@SuppressWarnings("WeakerAccess")
class Converters {

    // Distinguish between no timestamp set (null) and timestamp set to epoch (0)
    // as I find it easier to reason about DB queries for 'IS NULL' rather than '= 0'

    @TypeConverter
    public static Long toEpochMilli(Instant instant) {
        return (instant == null) ? null : instant.toEpochMilli();
    }

    @TypeConverter
    public static Instant ofEpochMilli(Long epochMilli) {
        return (epochMilli == null) ? null : Instant.ofEpochMilli(epochMilli);
    }

    @TypeConverter
    public static String ofRecurrenceRule(RecurrenceRule rule) {
        return rule == null ? null : rule.toString();
    }

    /**
     * @return object representing the recurrence rule, or null if there was an error parsing
     */
    @TypeConverter
    public static RecurrenceRule toRecurrenceRule(String recur) {
        try {
            return recur == null ? null : new RecurrenceRule(recur);
        } catch (InvalidRecurrenceRuleException e) {
            return null;
        }
    }
}
