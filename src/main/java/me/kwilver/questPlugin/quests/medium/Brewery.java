package me.kwilver.questPlugin.quests.medium;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.lootTables.Medium;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Brewery extends Quest implements Listener {
    List<Location> stands = new ArrayList<>();
    int count = 0;

    public Brewery(Player player, QuestPlugin main) {
        super(4 * 60 * 60, player, main, Medium.class);
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Brew 5 sets of 3 potions");
        info.add("TIME: 1 Hour");
        info.add("PROGRESS: " + count + "/5");
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.YELLOW + "Brewery";
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getInventory().getType() == InventoryType.BREWING) {
            if(e.getWhoClicked() == player && !stands.contains(e.getInventory().getLocation())) {
                stands.add(e.getInventory().getLocation());
            }
        }
    }

    @EventHandler
    public void onPotionBrew(BrewEvent e) {
        if(stands.remove(e.getBlock().getLocation())) {
            if(e.getResults().size() < 3) return;
            count++;
            if(count >= 5) {
                main.endQuest(player, true);
            }
        }
    }
}
