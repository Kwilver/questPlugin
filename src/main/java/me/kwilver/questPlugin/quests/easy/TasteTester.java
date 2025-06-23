package me.kwilver.questPlugin.quests.easy;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.lootTables.Easy;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.*;

public class TasteTester extends Quest implements Listener {
    Set<Material> count = new HashSet<>();

    public TasteTester(Player player, QuestPlugin main) {
        super(3 * 60 * 60, player, main, Easy.class);

    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Eat 15 Unique Foods!");
        info.add("TIME: 2 Hours");
        info.add("PROGRESS: " + count.size() + "/15");
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.GREEN + "Taste Tester";
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
        if(e.getPlayer() == player) {
            if(!count.contains(e.getItem().getType())) {
                count.add(e.getItem().getType());
                if(count.size() >= 15) {
                    main.endQuest(player, true);
                }
            }
        }
    }
}
