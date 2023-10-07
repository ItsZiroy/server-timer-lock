package com.itsziroy.servertimelock;

import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final ServerTimeLockPlugin plugin;

    public PlaceholderAPIExpansion(ServerTimeLockPlugin plugin) {
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
            LocalTime timeOfDay = LocalTime.ofSecondOfDay(plugin.getRemainingTime());
            return timeOfDay.toString();
        }

        return null; // Placeholder is unknown by the Expansion
    }
}