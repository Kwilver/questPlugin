package me.kwilver.questPlugin;

import me.kwilver.questPlugin.lootTables.LootTable;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import java.util.ArrayList;

public abstract class Quest {
    LootTable lootTable;
    protected QuestPlugin main;
    public OfflinePlayer player;
    public long questEndTime;

    public Quest(int questLength, Player player, QuestPlugin main, Class<? extends LootTable> difficulty) {
        this.main = main;
        this.player = player;
        questEndTime = System.currentTimeMillis() + questLength * 1000L;
        try {
            this.lootTable = difficulty.getDeclaredConstructor(Quest.class, QuestPlugin.class).newInstance(this, main);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (this instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) this, main);
        }
    }

    public LootTable getLootTable() {
        return lootTable;
    }

    protected void tick(Player onlinePlayer) {

    }

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
