package de.kleindev.commandblockbridge.spigot;

import de.kleindev.commandblockbridge.SentryIo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.ChannelNotRegisteredException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;

public class ServerCommandListener implements Listener {
    private Plugin plugin = Main.getPlugin(Main.class);
    private String servername = plugin.getConfig().getString("ServerName");
    private boolean debug = plugin.getConfig().getBoolean("Debug");

    public ServerCommandListener() {
        System.out.println("Init");
    }

    @EventHandler
    public void onCommand(ServerCommandEvent e) {
        if (e.getSender() instanceof BlockCommandSender) {
            if (e.getCommand().startsWith("[Bridge]")) {
                run(e);
            } else if (e.getCommand().startsWith("[Bridge-async]")) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> run(e));
            }
        }
    }

    private void run(ServerCommandEvent e) {
        try {
            e.setCancelled(true);
            SentryIo.addBreadcrumbMessage("ServerCommandListener triggered and CommandBlock with bridge detected");
            BlockCommandSender sender = (BlockCommandSender) e.getSender();
            Location block_loc = sender.getBlock().getLocation();
            String cmd = e.getCommand().substring(9);

            //Replace player names
            if (cmd.contains("@p")) {
                SentryIo.addBreadcrumbMessage("Command contains \"@p\"");
                try {
                    for (String s : cmd.split(" ")) {
                        if (s.startsWith("@p"))
                            cmd.replace(s, CommandUtils.getTarget(e.getSender(), s).getName());
                    }
                } catch (NullPointerException ex1) {
                    //There is no player in range
                    SentryIo.addBreadcrumbMessage("Error: No player in range");
                    e.getSender().sendMessage("There is no player in range!");
                }
            }


            if (cmd.contains("@a")) {
                SentryIo.addBreadcrumbMessage("Command contains \"@a\"");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(b);
                    out.writeUTF("ExecuteBungeeCommand");
                    out.writeUTF(servername);
                    out.writeUTF(cmd.replace("@a", p.getName()));
                    out.flush();
                    if (Bukkit.getOnlinePlayers().size() > 0) {
                        Bukkit.getOnlinePlayers().iterator().next().sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
                        if (debug)
                            System.out.println("CommandBlock (" +
                                    block_loc.getBlockX() + ", " +
                                    block_loc.getBlockY() + ", " +
                                    block_loc.getBlockZ() + ", " +
                                    block_loc.getWorld().getName() + ") " +
                                    "sending following command to Bungee:\n" +
                                    cmd);
                    } else if (debug)
                        System.out.println("Cannot send command to Bungee, cause there is no online player on server!");
                }
            } else {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                out.writeUTF("ExecuteBungeeCommand");
                out.writeUTF(servername);
                out.writeUTF(cmd);
                out.flush();
                if (Bukkit.getOnlinePlayers().size() > 0) {
                    Bukkit.getOnlinePlayers().iterator().next().sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
                    if (debug)
                        System.out.println("CommandBlock (" +
                                block_loc.getBlockX() + ", " +
                                block_loc.getBlockY() + ", " +
                                block_loc.getBlockZ() + ", " +
                                block_loc.getWorld().getName() + ") " +
                                "sending following command to Bungee:\n" +
                                cmd);
                } else if (debug)
                    System.out.println("Cannot send command to Bungee, cause there is no online player on server!");
            }
        } catch (ChannelNotRegisteredException ex2) {
            e.setCancelled(true);
            e.getSender().sendMessage("No bungee detected, make sure you have BungeeCord!");
            SentryIo.addBreadcrumbMessage("No Bungeecord detected. May the player connected directly to server");
        } catch (Exception ex3) {
            new Thread(() -> {
                System.out.println("AN error occured while sending command to bungee. Reporting to SentryIo...");
                SentryIo.addBreadcrumbMessage("Reporting to SentryIo...");
                ex3.printStackTrace();

                HashMap<String, String> extras = new HashMap<>();
                extras.put("CommandBlock content", e.getCommand());
                SentryIo.captureSpigot(ex3, extras);
                System.out.println("Reported successfully!");
            }).start();
        }
    }
}
