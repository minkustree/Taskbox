package home.westering56.taskbox.room;


import java.util.Date;

import androidx.room.TypeConverter;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(long value) {
        return new Date(value);
    }

    @TypeConverter
    public static long toTimestamp(Date value) {
        return value.getTime();
    }
}
