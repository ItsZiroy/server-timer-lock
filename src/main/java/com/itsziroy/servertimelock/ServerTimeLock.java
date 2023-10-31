package com.itsziroy.servertimelock;

import com.itsziroy.bukkitredis.BukkitRedisPlugin;
import com.itsziroy.bukkitredis.events.player.PlayerEvent;
import com.itsziroy.servertimelock.events.ServerLockEvent;
import com.itsziroy.servertimelock.events.ServerUnlockEvent;
import com.itsziroy.servertimelock.exceptions.ConfigurationException;
import com.itsziroy.servertimelock.jobs.CheckServerUptime;
import com.itsziroy.servertimelock.listeners.PlayerListener;
import com.itsziroy.servertimelock.utils.UptimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public final class ServerTimeLock extends JavaPlugin {

    private boolean locked = true;

    private long remainingTime = 0;

    private BukkitRedisPlugin bukkitRedis;

    private final HashMap<Integer, List<OpeningHours>> openingTimes = new HashMap<>();
    @Override
    public void onEnable() {
        // Plugin startup logic
        registerConfig();


        PlayerListener playerListener = new PlayerListener(this);
        getServer().getPluginManager().registerEvents(playerListener, this);

        FileConfiguration config = this.getConfig();

        registerOpeningHours(config);

        CheckServerUptime checkServerUptimeJob = new CheckServerUptime(this);

        checkServerUptimeJob.runTaskTimerAsynchronously(this,0, checkServerUptimeJob.getTickrate());

        BukkitRedisPlugin bukkitRedis = (BukkitRedisPlugin) Bukkit.getPluginManager().getPlugin("BukkitRedis");
        if (bukkitRedis != null) {
            getLogger().info("BukkitRedis extenstion loaded.");
            this.bukkitRedis = bukkitRedis;
            this.bukkitRedis.eventManager().registerCallback(PlayerEvent.class, playerListener::onPlayerByPassClosed);
        }

        // Small check to make sure that PlaceholderAPI is installed
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Hooked into Placeholder API.");
            new PlaceholderAPIExpansion(this).register();
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
                bukkitRedis.getMessanger().send(new ServerLockEvent(this.getRemainingCloseTime()));
            } else {
                bukkitRedis.getMessanger().send(new ServerUnlockEvent(this.getRemainingTime()));
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

    public Calendar getNextOpeningTime() {
        if(this.isLocked()) {
            int currentDayOfWeek = UptimeUtils.getCurrentDayOfWeek();

            HourMinute currentHourMinute = UptimeUtils.getCurrentHourMinute();

            List<OpeningHours> openingHoursForDay = this.openingTimes.get(currentDayOfWeek);

            for(OpeningHours openingHours: openingHoursForDay) {
                if (openingHours.open().after(currentHourMinute)) {

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.DAY_OF_WEEK, currentDayOfWeek);
                    calendar.set(Calendar.HOUR_OF_DAY, openingHours.open().hour());
                    calendar.set(Calendar.MINUTE, openingHours.open().minute());


                    return calendar;
                }
            }
            int nextDay = currentDayOfWeek + 1;


            Calendar calendar = Calendar.getInstance();
            if(nextDay == 8) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                nextDay = 1;
            }
            OpeningHours openingHours = this.openingTimes.get(nextDay).get(0);

            calendar.set(Calendar.DAY_OF_WEEK, nextDay);
            calendar.set(Calendar.HOUR_OF_DAY, openingHours.open().hour());
            calendar.set(Calendar.MINUTE, openingHours.open().minute());

            return calendar;
        } else {
            return null;
        }
    }

    public Calendar getNextClosingTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, (int) this.getRemainingTime());

        return calendar;
    }

    /**
     * Get remaining Server close time in seconds.
     * <p>
     * Returns -1 if open.
     *
     * @return seconds
     */
    public long getRemainingCloseTime() {
        Calendar currentTime = Calendar.getInstance();
        Calendar nextOpeningTime = this.getNextOpeningTime();

        if(nextOpeningTime == null) {
            return -1;
        }

        return (nextOpeningTime.getTimeInMillis() - currentTime.getTimeInMillis()) / 1000;
    }

    public OpeningHours getCurrentOpeningHours() {
        int day = UptimeUtils.getCurrentDayOfWeek();

        List<OpeningHours> openingHoursForDay = this.getOpeningTimes().get(day);

        HourMinute currentTime = UptimeUtils.getCurrentHourMinute();

        for(OpeningHours openingHours: openingHoursForDay) {
            if(currentTime.afterEqual(openingHours.open()) && currentTime.before(openingHours.close())) {
               return openingHours;
            }
        }
        return null;
    }

    public HashMap<Integer, List<OpeningHours>> getOpeningTimes() {
        return openingTimes;
    }

    public BukkitRedisPlugin getRedis() {
        return bukkitRedis;
    }

    /**
     * Get remaining Server opening time in seconds.
     * <p>
     * Returns -1 if open.
     *
     * @return seconds
     */
    public long getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(long remainingTime) {
        this.remainingTime = remainingTime;
    }
}
