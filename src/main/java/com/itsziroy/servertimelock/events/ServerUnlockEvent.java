package com.itsziroy.servertimelock.events;

import com.itsziroy.bukkitredis.events.Event;

public class ServerUnlockEvent extends Event {

    public long remaining_open_time;
    public ServerUnlockEvent(long remainingOpenTime) {
        super("server_unlock");
        this.remaining_open_time = remainingOpenTime;
    }
}
