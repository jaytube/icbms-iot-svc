package com.icbms.iot.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static String parseDate(long dateTime) {
        if (dateTime == 0L) {
            return "";
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return formatter.format(new Date(dateTime));
        }
    }

    public static Date parse(String date, String pattern) {
        SimpleDateFormat fmt = new SimpleDateFormat(pattern);

        try {
            return fmt.parse(date);
        } catch (ParseException var4) {
            var4.printStackTrace();
            return null;
        }
    }

}
