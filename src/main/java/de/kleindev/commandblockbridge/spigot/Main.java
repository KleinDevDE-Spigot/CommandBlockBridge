package de.kleindev.commandblockbridge.spigot;

import de.kleindev.commandblockbridge.SentryIo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipFile;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {


        SentryIo.addBreadcrumbMessage("Enabling plugin...");
        if (!getDataFolder().exists()) {
            SentryIo.addBreadcrumbMessage("Saving default config");
            if (!checkConfigInJar()) {
                saveDefaultConfig();
            } else {
                System.out.println("Well, you want to cheat? Disabling CommandBlockBridge cause of manipulated jar file...");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
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
                if (checkConfigInJar()) {
                    saveResource("config.yml", true);
                } else {
                    System.out.println("Well, you want to cheat? Disabling CommandBlockBridge cause of manipulated jar file...");
                    Bukkit.getPluginManager().disablePlugin(this);
                    return;
                }
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

    private boolean checkConfigInJar() {
        try {
            ZipFile zipFile = new ZipFile(this.getFile());
            boolean end = zipFile.getEntry("config.yml") == null;
            zipFile.close();
            return end;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Well, there's a fatal bug, stopping plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }
    }

    private void createConfig() {
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        cfg.set("ServerName", "default");
        cfg.set("Debug", false);
        try {
            cfg.save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            System.out.println("Oups, there's a bug! Could not save config file");
            SentryIo.addBreadcrumbMessage("Could not save config file!");
            e.printStackTrace();
            HashMap<String, String> extras = new HashMap<>();
            SentryIo.captureSpigot(e, extras);
            System.out.println("Reported successfully!  --  Stopping plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
}
