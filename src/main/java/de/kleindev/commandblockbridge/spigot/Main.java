package de.kleindev.commandblockbridge.spigot;

import de.kleindev.commandblockbridge.SentryIo;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        SentryIo.addBreadcrumbMessage("Enabling plugin...");
        if (!getDataFolder().exists()) {
            SentryIo.addBreadcrumbMessage("Saving default config");
            saveDefaultConfig();
        }
        SentryIo.addBreadcrumbMessage("Registering ServerCommandListener..");
        try {
            Bukkit.getPluginManager().registerEvents(new ServerCommandListener(), this);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Initializing listener failed! Restoring default config...");
            System.out.println("Previous variables\n" +
                    "ServerName: " + getConfig().getString("ServerName") + "\n" +
                    "Debug: " + getConfig().getString("Debug"));
            SentryIo.addBreadcrumbMessage("Initializing listener failed!");
            try {
                SentryIo.addBreadcrumbMessage("Restore default config");
                saveResource("config.yml", true);
                SentryIo.addBreadcrumbMessage("Reload config");
                reloadConfig();
                SentryIo.addBreadcrumbMessage("Unregister all listeners from CommandBlockBridge");
                HandlerList.unregisterAll(this);
                SentryIo.addBreadcrumbMessage("Register Listener");
                Bukkit.getPluginManager().registerEvents(new ServerCommandListener(), this);
            } catch (Exception e2) {
                System.out.println("This problem couldn't be fixed with restoring default config :(\n" +
                        "Reporting bug to SentryIo...");
                HashMap<String, String> extras = new HashMap<>();
                extras.put("Config | ServerName", getConfig().getString("ServerName"));
                extras.put("Config | Debug", getConfig().getString("Debug"));
                SentryIo.captureSpigot(e2, extras);
                System.out.println("Reported successfully!  --  Stopping plugin...");
                Bukkit.getPluginManager().disablePlugin(this);
            }
        }
    }
}
