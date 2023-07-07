package com.itsziroy.servertimelock;

public record HourMinute(int hour, int minute) implements Comparable<HourMinute> {


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

    public boolean equal(HourMinute m) {
        return compareTo(m) > 0;
    }

    public boolean afterEqual(HourMinute m) {
        return compareTo(m) > 0 || compareTo(m) == 0;
    }

    public boolean beforeEqual(HourMinute m) {
        return compareTo(m) < 0 || compareTo(m) == 0;
    }



}
