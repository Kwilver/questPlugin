package me.kwilver.questPlugin.quests.medium;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.lootTables.Medium;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.raid.RaidFinishEvent;

import java.util.ArrayList;

public class Condiut extends Quest implements Listener {
    int count = 0;

    public Condiut(Player player, QuestPlugin main) {
        super(4 * 60 * 60, player, main, Medium.class);
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Complete a raid!");
        info.add("TIME: 4 Hours");
        info.add("PROGRESS: " + count + "/2");
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.YELLOW + "Conduit";
    }

    @EventHandler
    public void onPlayerCraft(CraftItemEvent e) {
        if(e.getWhoClicked() == player) {
            if(e.getCurrentItem().getType() == Material.CONDUIT) {
                main.endQuest(player, true);
            }
        }
    }
}
