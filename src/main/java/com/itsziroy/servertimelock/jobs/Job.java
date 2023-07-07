package com.itsziroy.servertimelock.jobs;

import com.itsziroy.servertimelock.ServerTimeLockPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Job extends BukkitRunnable {

    private int tickrate = 20;

    protected ServerTimeLockPlugin plugin;

    public Job(ServerTimeLockPlugin plugin, int tickrate){
        this.plugin = plugin;
        this.tickrate = tickrate;
    }

    public int getTickrate() {
        return tickrate;
    }

}
