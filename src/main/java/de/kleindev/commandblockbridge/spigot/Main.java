package de.kleindev.commandblockbridge.spigot;

import de.kleindev.commandblockbridge.SentryIo;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.error.YAMLException;

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
        } catch (YAMLException e) {
            System.out.println("Miau");
            e.printStackTrace();
        }
    }
}
