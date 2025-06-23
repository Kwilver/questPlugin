package me.kwilver.questPlugin.quests.medium;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.lootTables.Easy;
import me.kwilver.questPlugin.lootTables.Medium;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;

import java.util.ArrayList;

public class Warrior extends Quest implements Listener {
    int count = 0;

    public Warrior(Player player, QuestPlugin main) {
        super(60 * 60 + 30 * 60, player, main, Medium.class);
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Complete a raid!");
        info.add("TIME: 2 Hours");
        info.add("PROGRESS: " + count + "/2");
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.YELLOW + "Warrior";
    }

    @EventHandler
    public void onRaidFinish(RaidFinishEvent e) {
        if(e.getWinners().contains(player.getPlayer())) {
            count++;
            if(count >= 2) {
                main.endQuest(player, true);
            }
        }
    }
}
