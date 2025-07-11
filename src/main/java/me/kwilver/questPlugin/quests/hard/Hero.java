package me.kwilver.questPlugin.quests.hard;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.lootTables.Easy;
import me.kwilver.questPlugin.lootTables.Hard;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;

import java.util.ArrayList;

public class Hero extends Quest implements Listener {
    int count = 0;

    public Hero(Player player, QuestPlugin main) {
        super(3 * 60 * 60, player, main, Hard.class);
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Complete 3 raids!");
        info.add("TIME: 1 Hour");
        info.add("PROGRESS: " + count + "/3");
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.RED + "Hero";
    }

    @EventHandler
    public void onRaidFinish(RaidFinishEvent e) {
        if(e.getWinners().contains(player.getPlayer())) {
            count++;

            if(count >= 3) {
                main.endQuest(player, true);
            }
        }
    }
}
