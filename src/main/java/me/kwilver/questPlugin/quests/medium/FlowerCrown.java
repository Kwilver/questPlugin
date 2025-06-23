package me.kwilver.questPlugin.quests.medium;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.TickingQuest;
import me.kwilver.questPlugin.lootTables.Medium;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class FlowerCrown extends Quest implements TickingQuest {
    Map<Material, Integer> requiredItems = new HashMap<>();

    public FlowerCrown(Player player, QuestPlugin main) {
        super(2 * 60 * 60, player, main, Medium.class);

        requiredItems.put(Material.LILY_OF_THE_VALLEY, 10);
        requiredItems.put(Material.DANDELION, 20);
        requiredItems.put(Material.POPPY, 20);
        requiredItems.put(Material.BLUE_ORCHID, 5);
        requiredItems.put(Material.LILAC, 1);
        requiredItems.put(Material.ROSE_BUSH, 1);
        requiredItems.put(Material.PEONY, 1);
        requiredItems.put(Material.TORCHFLOWER, 1);
    }

    @Override
    public void tick(Player onlinePlayer) {
        for(Material m : requiredItems.keySet()) {
            int amount = 0;
            for(ItemStack i : onlinePlayer.getInventory()) {
                if(i != null && i.getType() == m) {
                    amount += i.getAmount();
                }
            }
            if(amount < requiredItems.get(m)) return;
        }

        main.endQuest(player, true);
    }

    @Override
    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Collect the following flowers:");
        for(Material m : requiredItems.keySet()) {
            info.add(" - " + m.name() + " (" + requiredItems.get(m) + ")");
        }
        info.add("TIME: 2 Hours");

        int total = 0;
        for(Material m : requiredItems.keySet()) {
            int amount = 0;
            for(ItemStack i : player.getPlayer().getInventory()) {
                if(i != null && i.getType() == m) {
                    amount += i.getAmount();
                }
            }

            if(amount >= requiredItems.get(m)) total += requiredItems.get(m);
            else total += amount;
        }

        int required = 0;
        for(Material m : requiredItems.keySet()) {
            required += requiredItems.get(m);
        }

        info.add("PROGRESS: " + total + "/" + required);

        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.YELLOW + "Flower Crown";
    }
}
