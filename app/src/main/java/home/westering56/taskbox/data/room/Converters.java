package home.westering56.taskbox.data.room;

import java.time.Instant;

import androidx.room.TypeConverter;

@SuppressWarnings("WeakerAccess")
class Converters {

    @TypeConverter
    public static long toTimestamp(Instant instant) {
        return (instant == null) ? 0 : instant.toEpochMilli();
    }

    @TypeConverter
    public static Instant fromTimestamp(long epochMilli) {
        return (epochMilli == 0) ? null : Instant.ofEpochMilli(epochMilli);
    }

}
