package com.itsziroy.servertimelock.events;

import com.itsziroy.bukkitredis.events.Event;

public class ServerLockEvent extends Event {
    public ServerLockEvent() {
        super("server_lock");
    }
}
