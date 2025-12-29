package org.dopelegend.FourInARow;

import org.bukkit.plugin.java.JavaPlugin;
import org.dopelegend.FourInARow.Listeners.EventListener;
import org.dopelegend.FourInARow.Listeners.CommandListener;

public final class FourInARow extends JavaPlugin {

    public static FourInARow plugin;
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        // Register eventListener
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        // Register commands
        new CommandListener(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
