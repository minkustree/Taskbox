package home.westering56.taskbox.data.room;

import java.time.Instant;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;

public class Converters {

    @TypeConverter
    public static long toTimestamp(@NonNull Instant instant) {
        return instant.toEpochMilli();
    }

    @TypeConverter
    public static Instant fromTimestamp(long msTimestamp) {
        return Instant.ofEpochMilli(msTimestamp);
    }

}
