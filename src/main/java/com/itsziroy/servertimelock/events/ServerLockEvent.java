package com.itsziroy.servertimelock.events;

import com.itsziroy.bukkitredis.events.Event;

public class ServerLockEvent extends Event {

    public long remaining_close_time;
    public ServerLockEvent(long remainingCloseTime) {
        super("server_lock");
        this.remaining_close_time = remainingCloseTime;
    }
}
