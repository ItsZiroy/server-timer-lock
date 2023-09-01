package com.itsziroy.servertimelock.events;


import com.itsziroy.bukkitredis.events.Event;

import java.util.Calendar;

public class ServerRemainingUptimeEvent extends Event {

    private static Calendar latestCall;
    public long remainingTime;
    public ServerRemainingUptimeEvent(long remainingTime) {
        super("server_remaining_uptime");

        latestCall = Calendar.getInstance();

        this.remainingTime = remainingTime;
    }

    public static boolean shouldSend(int interval) {
        if(latestCall == null) {
            return true;
        }
        Calendar currentTime = Calendar.getInstance();
        long timeDiffInSeconds = (currentTime.getTimeInMillis() - latestCall.getTimeInMillis()) / 1000;

        return timeDiffInSeconds >= interval;
    }

}
