package com.itsziroy.servertimelock.jobs;

import com.itsziroy.servertimelock.HourMinute;
import com.itsziroy.servertimelock.OpeningHours;
import com.itsziroy.servertimelock.ServerTimeLockPlugin;
import com.itsziroy.servertimelock.events.ServerRemainingUptimeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
                // Setting remaining uptime of server
                HourMinute latestClosingTime = findLatestClosingTime(openingHours.close(), openingHoursForDay, day);
                long timeUntilClose = calculateTimeUntilClose(currentTime, latestClosingTime);
                plugin.getLogger().finest("Latest closing time: "+ latestClosingTime);
                if(ServerRemainingUptimeEvent.shouldSend(plugin.getConfig().getInt("remaining_uptime_intervals.redis"))) {
                    plugin.getRedis().getMessanger().send(new ServerRemainingUptimeEvent(timeUntilClose));
                }
                plugin.getLogger().finest("Time until close: " + timeUntilClose);
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
                    player.kickPlayer("The Server has closed.");
                }
            });
        }
    }

    private HourMinute findLatestClosingTime(HourMinute hourMinute, List<OpeningHours> openingHoursList, int day) {
        if(hourMinute.equals(24, 0)) {
            hourMinute = new HourMinute(0,0);
        }
        if(hourMinute.equals(0,0)) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, day);
            calendar.add(Calendar.DAY_OF_WEEK, 1);
            openingHoursList = plugin.getOpeningTimes().get(calendar.get(Calendar.DAY_OF_WEEK));
            day++;
        }
        for(OpeningHours openingHours: openingHoursList) {
           if(openingHours.open().equals(hourMinute)) {
               return findLatestClosingTime(openingHours.close(), openingHoursList, day);
           }
        }
        return hourMinute;
    }

    private long calculateTimeUntilClose(HourMinute currentTime, HourMinute closingTime) {
        Calendar closingCalendar = Calendar.getInstance();
        // closing time is next day
        if(currentTime.after(closingTime)) {
            closingCalendar.add(Calendar.DATE, 1);
        }
        closingCalendar.set(Calendar.HOUR, closingTime.hour());
        closingCalendar.set(Calendar.MINUTE, closingTime.minute());
        closingCalendar.set(Calendar.SECOND, 0);
        closingCalendar.set(Calendar.MILLISECOND, 0);

        return (closingCalendar.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 1000;
    }
}
