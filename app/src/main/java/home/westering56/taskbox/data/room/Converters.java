package home.westering56.taskbox.data.room;

import java.time.Instant;

import androidx.room.TypeConverter;

class Converters {

    @TypeConverter
    static long toTimestamp(Instant instant) {
        return (instant == null) ? 0 : instant.toEpochMilli();
    }

    @TypeConverter
    static Instant fromTimestamp(long epochMilli) {
        return (epochMilli == 0) ? null : Instant.ofEpochMilli(epochMilli);
    }

}
