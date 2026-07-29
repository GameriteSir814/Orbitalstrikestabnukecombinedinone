package com.orbitalstrike;

import org.bukkit.plugin.java.JavaPlugin;

public class OrbitalStrike extends JavaPlugin {

    private static OrbitalStrike instance;
    private CannonManager cannonManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        cannonManager = new CannonManager(this);

        getServer().getPluginManager().registerEvents(new CannonListener(this, cannonManager), this);
        getCommand("osc").setExecutor(new CannonCommand(cannonManager));

        getLogger().info("OrbitalStrike enabled.");
    }

    public static OrbitalStrike getInstance() { return instance; }
}
