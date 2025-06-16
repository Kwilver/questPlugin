package me.kwilver.questPlugin;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

import static me.kwilver.questPlugin.QuestPlugin.pluginMain;

public abstract class Quest {
    Plugin plugin = pluginMain();
    protected QuestPlugin main;
    protected OfflinePlayer player;

    public Quest(int questLength, Player player, QuestPlugin main) {
        this.main = main;
        this.player = player;

        if (this instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) this, main);
        }
    }

    //time will be in ms!
    public long questEndTime = System.currentTimeMillis() + 90 * 1000; // 1.5 minutes

    protected void tick(Player onlinePlayer) {

    } //to check for completion

    public abstract ArrayList<String> questInfo();

    public ArrayList<String> info() {
        ArrayList<String> info = new ArrayList<>();
        info.add(ChatColor.AQUA + displayName());

        info.addAll(questInfo());

        return info;
    }

    public abstract String displayName();

    public boolean questActive() {
        return System.currentTimeMillis() <= questEndTime;
    }

    public void cleanup() {
        // Unregister events if this quest is a listener
        if (this instanceof Listener) {
            HandlerList.unregisterAll((Listener) this);
        }
    }

    public long msRemaining() {
        if(questActive()) {
            return questEndTime - System.currentTimeMillis();
        }
        return 0L;
    }
}
