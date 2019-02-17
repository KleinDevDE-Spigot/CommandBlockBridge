package de.kleindev.commandblockbridge.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class Main extends Plugin implements Listener {

    @Override
    public void onEnable() {
        System.out.println("[CommandBlockBridge] Loading CommandBlockBridge Listener");
        ProxyServer.getInstance().getPluginManager().registerListener(this, this);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if (!e.getTag().equalsIgnoreCase("BungeeCord")) return;
        ByteArrayDataInput dis = ByteStreams.newDataInput(e.getData());

        String subchannel = dis.readUTF();
        if (subchannel.equalsIgnoreCase("ExecuteBungeeCommand")) {
            String server = dis.readUTF();
            String message = dis.readUTF();
            System.out.println("Received command from \"" + server + "\"-> " + message);
            ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole()
                    , message);
        }
    }
}
