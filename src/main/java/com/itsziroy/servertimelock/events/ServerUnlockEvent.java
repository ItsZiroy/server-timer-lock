package com.itsziroy.servertimelock.events;

import com.itsziroy.bukkitredis.events.Event;

public class ServerUnlockEvent extends Event {


    public ServerUnlockEvent() {
        super("server_unlock");
    }
}
