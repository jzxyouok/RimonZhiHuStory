package com.developer.rimon.zhihudaily.utils;

import android.support.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Rimon on 2016/8/28.
 */
public class DateUtil {
    public static String getOtherDateString(@Nullable Date date, int num, SimpleDateFormat format) {
        Calendar calendar = Calendar.getInstance();
        if (date!=null){
            calendar.setTime(date);
        }
        calendar.roll(Calendar.DAY_OF_YEAR,num);
        return format.format(calendar.getTime());
    }

    public static String getDateWithMillis(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        Date date = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);

        return format.format(date);
    }

    public static String changeFormat(String yyyyMMdd) throws ParseException {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMdd", Locale.US);
        SimpleDateFormat format2 = new SimpleDateFormat("MM月dd日 EEEE", Locale.CHINA);
        return format2.format(format1.parse(yyyyMMdd));
    }
}
