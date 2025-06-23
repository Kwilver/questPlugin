package me.kwilver.questPlugin.quests.medium;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.lootTables.Medium;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;

import java.util.ArrayList;
import java.util.List;

public class Aquaman extends Quest implements Listener {
    public Aquaman(Player player, QuestPlugin main) {
        super(60 * 60, player, main, Medium.class);
    }
    int count = 0;

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Kill 25 Guardians!");
        info.add("TIME: 1 Hour");
        info.add("PROGRESS: " + count + "/25");
        return info;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if(e.getEntity().getKiller() == player) {
            if(e.getEntity().getType() == EntityType.GUARDIAN) {
                count++;
                if(count >= 25) {
                    main.endQuest(player, true);
                }
            }
        }
    }

    @Override
    public String displayName() {
        return ChatColor.YELLOW + "Aquaman";
    }
}
