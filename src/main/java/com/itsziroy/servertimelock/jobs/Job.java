package com.itsziroy.servertimelock.jobs;

import com.itsziroy.servertimelock.ServerTimeLock;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Job extends BukkitRunnable {

    private int tickrate = 20;

    protected ServerTimeLock plugin;

    public Job(ServerTimeLock plugin, int tickrate){
        this.plugin = plugin;
        this.tickrate = tickrate;
    }
    public Job(ServerTimeLock plugin){
        this.plugin = plugin;
    }

    public int getTickrate() {
        return tickrate;
    }

}
