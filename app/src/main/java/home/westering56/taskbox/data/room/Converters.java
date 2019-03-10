package home.westering56.taskbox.data.room;

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

}
