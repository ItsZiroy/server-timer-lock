package com.itsziroy.servertimelock;

import com.itsziroy.servertimelock.jobs.CheckServerUptime;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.DayOfWeek;
import java.util.*;
import java.util.logging.Level;

public final class ServerTimeLockPlugin extends JavaPlugin {

    private boolean locked = false;



    private final HashMap<Integer, List<OpeningHours>> openingTimes = new HashMap<>();
    @Override
    public void onEnable() {
        // Plugin startup logic

        FileConfiguration config = this.getConfig();

        registerOpeningHours(config);

        new CheckServerUptime(this).runTaskTimerAsynchronously(this,0, 300);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    private void registerOpeningHours(FileConfiguration config) {

        String[] weekdays = {"sunday", "monday",
                "tuesday", "wednesday","thursday",
                "friday", "saturday"};

        for (int i = 0; i < weekdays.length; i++) {

            openingTimes.put(i + 1, new ArrayList<>());

            List<Map<?, ?>> list = config.getMapList("opening_hours." + weekdays[i]);
            for(Map<?, ?> item: list) {
                getLogger().log(Level.INFO, item.toString());
                String open = (String) item.get("open");
                String[] openSplit = open.split(":");
                HourMinute openConfig = new HourMinute(Integer.parseInt(openSplit[0]), Integer.parseInt(openSplit[1]));

                String close = (String) item.get("close");
                this.getLogger().log(Level.INFO, open + "-"+close);
                String[] closeSplit = close.split(":");
                int closeHour = Integer.parseInt(closeSplit[0]) == 0 ? 24 : Integer.parseInt(closeSplit[0]);
                HourMinute closeConfig = new HourMinute(closeHour, Integer.parseInt(closeSplit[1]));

                List<OpeningHours> openingHoursList = openingTimes.get(i + 1);
                openingHoursList.add(new OpeningHours(openConfig, closeConfig));
            }



        }
    }

    public HashMap<Integer, List<OpeningHours>> getOpeningTimes() {
        return openingTimes;
    }
}
