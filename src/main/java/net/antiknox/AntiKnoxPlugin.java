package net.antiknox;

import net.antiknox.gson.GsonDeserializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ForkJoinPool;

public class AntiKnoxPlugin extends JavaPlugin implements Listener {

    private static final String DEFAULT_KICK_MSG = "[AntiKnox] You're not allowed to connect with a VPN or proxy.";

    private ForkJoinPool forkJoinPool = new ForkJoinPool();
    private AntiKnox antiKnox;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String key = getConfig().getString("key");

        if (key == null || key.length() != 64) {
            getLogger().warning("+----------------------------------------------------------------+");
            getLogger().warning("|                            AntiKnox                            |");
            getLogger().warning("+----------------------------------------------------------------+");
            getLogger().warning("| You have to set your AntiKnox key before protection can begin. |");
            getLogger().warning("| To do this, edit 'config.yml' in the 'AntiKnox' folder in your |");
            getLogger().warning("| plugins, and edit the key to your key.                         |");
            getLogger().warning("+----------------------------------------------------------------+");
        } else {
            antiKnox = new AntiKnox.Builder()
                    .jsonDeserializer(new GsonDeserializer())
                    .key(key).build();

            Bukkit.getPluginManager().registerEvents(this, this);
        }
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        forkJoinPool.submit(new Runnable() {

            @Override
            public void run() {
                String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
                Record lookup = antiKnox.lookup(ip);

                if (lookup.hasDirectMatch()) {
                    Record.Direct directMatch = lookup.getDirectMatch();
                    getLogger().info(String.format("Disallowing player %s to connect using IP %s (detected as %s from %s).",
                            event.getPlayer().getName(), ip, directMatch.getType().toString().toLowerCase(), directMatch.getProvider()));

                    event.getPlayer().kickPlayer(getConfig().getString("kickmessage", DEFAULT_KICK_MSG));
                }
            }

        });
    }

}
