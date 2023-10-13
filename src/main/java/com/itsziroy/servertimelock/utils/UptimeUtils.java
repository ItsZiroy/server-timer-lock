package com.itsziroy.servertimelock.utils;

import com.itsziroy.servertimelock.HourMinute;
import com.itsziroy.servertimelock.OpeningHours;
import com.itsziroy.servertimelock.ServerTimeLock;

import java.util.Calendar;
import java.util.List;

public class UptimeUtils {

    public static HourMinute getCurrentHourMinute() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return new HourMinute(hour, minute);
    }

    public static int getCurrentDayOfWeek() {
        Calendar calendar = Calendar.getInstance();

        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static LatestClosingTimeReturn findLatestClosingTime(ServerTimeLock plugin, HourMinute currentClosingTime, List<OpeningHours> openingHoursListForCurrentDay, int day) {
        if(currentClosingTime.equals(24, 0)) {
            currentClosingTime = new HourMinute(0,0);
        }
        if(currentClosingTime.equals(0,0)) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, day);
            calendar.add(Calendar.DAY_OF_WEEK, 1);
            openingHoursListForCurrentDay = plugin.getOpeningTimes().get(calendar.get(Calendar.DAY_OF_WEEK));
            day++;
            if(day == 8) day = 1;
        }
        for(OpeningHours openingHours: openingHoursListForCurrentDay) {
            if(openingHours.open().equals(currentClosingTime)) {
                return findLatestClosingTime(plugin, openingHours.close(), openingHoursListForCurrentDay, day);
            }
        }
        return new LatestClosingTimeReturn(currentClosingTime, day);
    }

    public static long calculateTimeUntilClose(HourMinute currentTime, LatestClosingTimeReturn closingTime) {
        Calendar currentCalendar = Calendar.getInstance();
        Calendar closingCalendar = Calendar.getInstance();

        // closing time is next dayOfWeek
        currentCalendar.set(Calendar.HOUR, currentTime.hour());
        currentCalendar.set(Calendar.MINUTE, currentTime.minute());
        if(closingTime.dayOfWeek() < Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            closingCalendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        closingCalendar.set(Calendar.DAY_OF_WEEK, closingTime.dayOfWeek());
        closingCalendar.set(Calendar.HOUR, closingTime.hourMinute().hour());
        closingCalendar.set(Calendar.MINUTE, closingTime.hourMinute().minute());
        closingCalendar.set(Calendar.SECOND, 0);
        closingCalendar.set(Calendar.MILLISECOND, 0);

        return (closingCalendar.getTimeInMillis() - currentCalendar.getTimeInMillis()) / 1000;
    }

    public record LatestClosingTimeReturn(HourMinute hourMinute, int dayOfWeek) {}
}
