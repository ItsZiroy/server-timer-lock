package com.itsziroy.servertimelock.jobs;

import com.itsziroy.servertimelock.HourMinute;
import com.itsziroy.servertimelock.OpeningHours;
import com.itsziroy.servertimelock.Permission;
import com.itsziroy.servertimelock.ServerTimeLock;
import com.itsziroy.servertimelock.events.ServerRemainingUptimeEvent;
import com.itsziroy.servertimelock.utils.UptimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import static com.itsziroy.servertimelock.utils.UptimeUtils.calculateTimeUntilClose;
import static com.itsziroy.servertimelock.utils.UptimeUtils.findLatestClosingTime;

public class CheckServerUptime extends Job {

    public static void main(String[] args) {
        CheckServerUptime checkServerUptime = new CheckServerUptime(new ServerTimeLock());
    }

    public CheckServerUptime(ServerTimeLock plugin) {
        super(plugin, 20);

    }

    @Override
    public void run() {
        plugin.getLogger().log(Level.FINEST, "Run Server Update");
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        HourMinute currentTime = UptimeUtils.getCurrentHourMinute();

        List<OpeningHours> openingHoursForDay = plugin.getOpeningTimes().get(day);

        boolean locked = true;

        for(OpeningHours openingHours: openingHoursForDay) {
            if(currentTime.afterEqual(openingHours.open()) && currentTime.before(openingHours.close())) {
                // Setting remaining uptime of server
                UptimeUtils.LatestClosingTimeReturn latestClosingTime = findLatestClosingTime(plugin, openingHours.close(), openingHoursForDay, day);
                long timeUntilClose = calculateTimeUntilClose(currentTime, latestClosingTime);

                if(ServerRemainingUptimeEvent.shouldSend(plugin.getConfig().getInt("remaining_uptime_intervals.redis"))) {
                    plugin.getRedis().getMessanger().send(new ServerRemainingUptimeEvent(timeUntilClose));
                }

                plugin.setRemainingTime(timeUntilClose);

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
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if(!player.hasPermission(Permission.BYPASS_SERVER_CLOSE)) {
                        player.kickPlayer("The Server has closed.");
                    }
                }
            });
        }
    }
}
