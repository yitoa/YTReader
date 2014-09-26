
package com.yt.reader.utils;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

    /**
     * 将当前系统时间转换为greenwich时间
     * 
     * @return
     */
    public static Date getGreenwichDate(Date date) {
        if (null == date)
            date = new Date();
        try {
            TimeZone tz = TimeZone.getTimeZone("Etc/Greenwich");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E");
            sdf.setTimeZone(tz);
            String date_tz = sdf.format(date);
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.parse(date_tz);
        } catch (ParseException e) {
            return date;
        }
    }

    /**
     * 将greenwich时间转换为系统时间。
     * 
     * @param date
     * @return
     */
    public static Date getSystemDate(Date date) {
        Date localDate = new Date();
        Date greenwichDate = getGreenwichDate(null);
        long diff = (localDate.getTime() - greenwichDate.getTime()) / 3600000;
        return getAdjustDate(diff, date);
    }

    public static Date getAdjustDate(long adjust, Date date) {
        if (null == date)
            date = getGreenwichDate(null);
        date.setTime(date.getTime() + adjust * 3600000);// 60*60*1000
        return date;
    }

    public static Date getBejingDate() {
        return getAdjustDate(8, null);
    }

    /**
     * 将date转换为指定pattern的字符串
     * 
     * @param date
     * @param pattern
     * @return
     */
    public static String dateToString(Date date, String pattern) {
        if (null == date)
            return null;
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }

    /**
     * 将以给定pattern显示的date转换为Date类型
     * 
     * @param date
     * @param pattern
     * @return
     */
    public static Date stringToDate(String date, String pattern) {
        DateFormat f = new SimpleDateFormat(pattern);
        try {
            return f.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getWeek(Date date) {
        String pattern = "E";
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
        String week = format.format(date);
        if ("Mon".equals(week)) {
            return 1;
        } else if ("Tue".equals(week)) {
            return 2;
        } else if ("Wed".equals(week)) {
            return 3;
        } else if ("Thu".equals(week)) {
            return 4;
        } else if ("Fri".equals(week)) {
            return 5;
        } else if ("Sat".equals(week)) {
            return 6;
        }
        return 0;
    }

    public static boolean isLeapYear(Date date) {
        int year = Integer.parseInt(DateUtils.dateToString(date, "yyyy"));
        return (year % 400 == 0 || year % 4 == 0 && year % 100 > 0);
    }

    public static Date getFirstDayOfMonth(Date date) {
        String s = DateUtils.dateToString(date, "yyyy-MM") + "-01";
        return stringToDate(s, "yyyy-MM-dd");
    }

    public static int getDaysOfMonth(Date date) {
        int month = Integer.parseInt(DateUtils.dateToString(date, "M"));
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 2:
                if (isLeapYear(date))
                    return 29;
                else
                    return 28;
        }
        return 30;
    }

    public static Date addDay(Date date, int day) {
        BigInteger big = new BigInteger(date.getTime() + "");
        BigInteger big2 = new BigInteger(day + "");
        big2 = big2.multiply(new BigInteger(24 * 60 * 60 * 1000 + ""));
        big = big.add(big2);
        return new Date(big.longValue());
    }
}
