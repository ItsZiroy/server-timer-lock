package com.itsziroy.servertimelock;

import org.bukkit.entity.Horse;

public record HourMinute(int hour, int minute) implements Comparable<HourMinute> {


    public static HourMinute difference(HourMinute m1, HourMinute m2) {
        return new HourMinute(m1.hour() - m2.hour(), m1.minute() - m2.minute());
    }
    @Override
    public int compareTo(HourMinute m) {
        if(hour() - m.hour() != 0) {
            return hour() - m.hour();
        }
        return minute() - m.minute();
    }

    public boolean before(HourMinute m) {
        return compareTo(m) < 0;
    }

    public boolean after(HourMinute m) {
        return compareTo(m) > 0;
    }

    public boolean equals(HourMinute m) {
        return compareTo(m) == 0;
    }
    public boolean equals(int hour, int minute) {
        return compareTo(new HourMinute(hour, minute)) == 0;
    }

    public boolean afterEqual(HourMinute m) {
        return compareTo(m) > 0 || compareTo(m) == 0;
    }

    public boolean beforeEqual(HourMinute m) {
        return compareTo(m) < 0 || compareTo(m) == 0;
    }

    @Override
    public String toString() {
        return "" + hour() + ":" + minute();
    }
}
