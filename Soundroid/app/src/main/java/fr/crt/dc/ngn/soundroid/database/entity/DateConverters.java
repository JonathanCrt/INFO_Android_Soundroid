package fr.crt.dc.ngn.soundroid.database.entity;

import androidx.room.TypeConverter;

import java.util.Calendar;
import java.util.Date;

public class DateConverters {
    @TypeConverter
    public static Date toDate(Long dateLong){
        return dateLong == null ? null: new Date(dateLong);
    }

    @TypeConverter
    public static Long toLong(Date date){
        return date == null ? null : date.getTime();
    }
}
