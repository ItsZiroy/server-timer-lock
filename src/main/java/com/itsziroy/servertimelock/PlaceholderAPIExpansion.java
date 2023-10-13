package com.itsziroy.servertimelock;

import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final ServerTimeLock plugin;

    public PlaceholderAPIExpansion(ServerTimeLock plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return "mc-nations";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "servertime";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if(params.equalsIgnoreCase("remaining_time")){
            long remainingTime = plugin.getRemainingTime();
            int day = (int) TimeUnit.SECONDS.toDays(remainingTime);
            long hours = TimeUnit.SECONDS.toHours(remainingTime) - (day * 24L);
            long minute = TimeUnit.SECONDS.toMinutes(remainingTime) -
                    (TimeUnit.SECONDS.toHours(remainingTime)* 60);
            long second = TimeUnit.SECONDS.toSeconds(remainingTime) -
                    (TimeUnit.SECONDS.toMinutes(remainingTime) *60);
            return day + ":" + hours + ":" + minute + ":" + second;
        }

        return null; // Placeholder is unknown by the Expansion
    }
}