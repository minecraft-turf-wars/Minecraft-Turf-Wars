package com.example.turfwars;

import org.bukkit.plugin.java.JavaPlugin;

public class TurfWars extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("TurfWars has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("TurfWars has been disabled!");
    }
}
