package com.itsziroy.servertimelock.jobs;

import com.itsziroy.servertimelock.HourMinute;
import com.itsziroy.servertimelock.OpeningHours;
import com.itsziroy.servertimelock.ServerTimeLockPlugin;
import org.bukkit.entity.Player;

import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

public class CheckServerUptime extends Job {

    public static void main(String[] args) {
        CheckServerUptime checkServerUptime = new CheckServerUptime(new ServerTimeLockPlugin());
    }

    public CheckServerUptime(ServerTimeLockPlugin plugin) {
        super(plugin, 20);

    }

    @Override
    public void run() {
        plugin.getLogger().log(Level.FINEST, "Run Server Update");
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        HourMinute currentTime = new HourMinute(hour, minute);

        List<OpeningHours> openingHoursForDay = plugin.getOpeningTimes().get(day);

        boolean locked = true;

        for(OpeningHours openingHours: openingHoursForDay) {
            if(currentTime.afterEqual(openingHours.open()) && currentTime.before(openingHours.close())) {
                locked = false;
            }
        }
        plugin.setLocked(locked);
        plugin.getLogger().log(Level.FINEST, "Server is currently locked: " + plugin.isLocked());

       /* if (previousOpeningHours.close().before(previousOpeningHours.open())) {
            // Current time is before closing hours
            if (currentTime.before(previousOpeningHours.close())) {
                plugin.setLocked(false);
            }
        }

        // Opening Times Monday: 22 - 02
        // Opening Times Tuesday 22 - 23
        // Current Time: Monday 23
        // Current Time : Tuesday 1
        // Current Time Wed: 1

        // Time is 16:00 opening time was 12:00 close time is 18:00
        if (currentTime.after(currentOpeningHours.open()) && currentTime.before(currentOpeningHours.close())) {
            plugin.setLocked(false);
        } else {
            if(currentOpeningHours.close().before(currentOpeningHours.open())) {
                if(currentTime.after(currentOpeningHours.open())) {
                    plugin.setLocked(false);
                } else {
                    // Opening time was 23:00 close time is 02:00 (2 < 23)
                    if (previousOpeningHours.close().before(previousOpeningHours.open())) {
                            // If current time is 1: Lock is false. If current time is 3: Lock is true
                            plugin.setLocked(currentTime.before(previousOpeningHours.close()));
                    } else {
                        plugin.setLocked(true);
                    }
                }
            } else {
                // Opening time was 23:00 close time is 02:00 (2 < 23)
                if (previousOpeningHours.close().before(previousOpeningHours.open())) {
                    // If current time is 1: Lock is false. If current time is 3: Lock is true
                    plugin.setLocked(currentTime.before(previousOpeningHours.close()));
                } else {
                    plugin.setLocked(true);
                }
            }

        }*/

        kickPlayersOrNot();
    }

    private void kickPlayersOrNot() {
        if (plugin.isLocked()) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.kickPlayer("The Server has closed.");
            }

        }
    }
}
