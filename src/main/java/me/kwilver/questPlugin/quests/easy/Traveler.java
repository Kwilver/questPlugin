package me.kwilver.questPlugin.quests.easy;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.TickingQuest;
import me.kwilver.questPlugin.lootTables.Easy;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Traveler extends Quest implements Listener {

    public Traveler(Player player, QuestPlugin main) {
        super(3 * 60 * 60, player, main, Easy.class);
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Complete a raid!");
        info.add("TIME: 2 Hours");
        info.add("PROGRESS: Incomplete");
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.GREEN + "Traveler";
    }

    @EventHandler
    public void onRaidFinish(RaidFinishEvent e) {
        if(e.getWinners().contains(player.getPlayer())) {
            main.endQuest(player, true);
        }
    }
}
