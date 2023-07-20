package com.itsziroy.servertimelock;

import com.itsziroy.bukkitredis.BukkitRedisPlugin;
import com.itsziroy.servertimelock.events.ServerLockEvent;
import com.itsziroy.servertimelock.events.ServerUnlockEvent;
import com.itsziroy.servertimelock.exceptions.ConfigurationException;
import com.itsziroy.servertimelock.jobs.CheckServerUptime;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public final class ServerTimeLockPlugin extends JavaPlugin {

    private boolean locked = true;

    private BukkitRedisPlugin bukkitRedis;

    private final HashMap<Integer, List<OpeningHours>> openingTimes = new HashMap<>();
    @Override
    public void onEnable() {
        // Plugin startup logic
        registerConfig();

        FileConfiguration config = this.getConfig();

        registerOpeningHours(config);

        CheckServerUptime checkServerUptimeJob = new CheckServerUptime(this);

        checkServerUptimeJob.runTaskTimerAsynchronously(this,0, checkServerUptimeJob.getTickrate());

        BukkitRedisPlugin bukkitRedis = (BukkitRedisPlugin) Bukkit.getPluginManager().getPlugin("bukkit-redis");
        if (bukkitRedis != null) {
            getLogger().info("BukkitRedis extenstion loaded.");
            this.bukkitRedis = bukkitRedis;

        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        if(locked != this.locked && bukkitRedis != null) {
            if(locked) {
                bukkitRedis.getMessanger().send(new ServerLockEvent());
            } else {
                bukkitRedis.getMessanger().send(new ServerUnlockEvent());
            }
        }
        this.locked = locked;
    }

    public void registerConfig(){
        File config = new File(this.getDataFolder(), "config.yml");
        if(!config.exists()){
            this.getConfig().options().copyDefaults(true);
            saveConfig();
        }
    }

    private void registerOpeningHours(FileConfiguration config) {

        String[] weekdays = {"sunday", "monday",
                "tuesday", "wednesday","thursday",
                "friday", "saturday"};

        for (int i = 0; i < weekdays.length; i++) {

            openingTimes.put(i + 1, new ArrayList<>());
            try {
                List<Map<?, ?>> list = config.getMapList("opening_hours." + weekdays[i]);
                for (Map<?, ?> item : list) {
                    getLogger().log(Level.INFO, item.toString());
                    String open = (String) item.get("open");
                    String[] openSplit = open.split(":");
                    HourMinute openConfig = new HourMinute(Integer.parseInt(openSplit[0]), Integer.parseInt(openSplit[1]));

                    String close = (String) item.get("close");
                    this.getLogger().log(Level.INFO, open + "-" + close);
                    String[] closeSplit = close.split(":");
                    int closeHour = Integer.parseInt(closeSplit[0]) == 0 ? 24 : Integer.parseInt(closeSplit[0]);
                    HourMinute closeConfig = new HourMinute(closeHour, Integer.parseInt(closeSplit[1]));

                    List<OpeningHours> openingHoursList = openingTimes.get(i + 1);
                    openingHoursList.add(new OpeningHours(openConfig, closeConfig));
                }
            } catch (NullPointerException | ClassCastException | NumberFormatException e) {
                throw new ConfigurationException();
            }
        }
    }

    public HashMap<Integer, List<OpeningHours>> getOpeningTimes() {
        return openingTimes;
    }
}
