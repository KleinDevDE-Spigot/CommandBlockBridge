package de.kleindev.commandblockbridge;

import io.sentry.Sentry;
import io.sentry.SentryClient;
import io.sentry.context.Context;
import io.sentry.event.Breadcrumb;
import io.sentry.event.BreadcrumbBuilder;
import net.md_5.bungee.api.ProxyServer;
import org.bukkit.Bukkit;

import java.util.*;

public class SentryIo {
    private static SentryClient sentry = Sentry.init("XXX");
    private static List<Breadcrumb> breadcrumbs = new ArrayList<>();
    private static String version = "1.0";

    public static void captureSpigot(Exception e, HashMap<String, String> extras) {
        Context context = sentry.getContext();
        sentry.setRelease(version);

        for (Breadcrumb b : breadcrumbs) {
            context.recordBreadcrumb(b);
        }

        context.addExtra("Server Version", Bukkit.getServer().getVersion());
        context.addExtra("Bukkit Version", Bukkit.getServer().getBukkitVersion());
        for (String s : extras.keySet()) {
            context.addExtra(s, extras.get(s));
        }

        sentry.sendException(e);
    }

    public static void captureBungee(Exception e, HashMap<String, String> extras) {
        Context context = sentry.getContext();
        sentry.setRelease(version);

        for (Breadcrumb b : breadcrumbs) {
            context.recordBreadcrumb(b);
        }

        context.addExtra("Bungee Version", ProxyServer.getInstance().getVersion());
        for (String s : extras.keySet()) {
            context.addExtra(s, extras.get(s));
        }

        sentry.sendException(e);
    }

    public static void addBreadcrumb(Breadcrumb.Type type, String message, String category, Breadcrumb.Level level, Date timestamp, Map<String, String> data) {
        breadcrumbs.add(new BreadcrumbBuilder()
                .setMessage(message)
                .setType(type)
                .setCategory(category)
                .setLevel(level)
                .setTimestamp(timestamp)
                .setData(data)
                .build()
        );
    }

    public static void addBreadcrumbMessage(String message) {
        breadcrumbs.add(new BreadcrumbBuilder().setMessage(message).build());
    }
}
