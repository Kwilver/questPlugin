package me.kwilver.questPlugin.quests.medium;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.TickingQuest;
import me.kwilver.questPlugin.lootTables.Medium;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class Fisherman extends Quest implements TickingQuest {
    public Fisherman(Player player, QuestPlugin main) {
        super(60 * 60, player, main, Medium.class);
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Catch 1 Pufferfish, 1 Tropical fish, 10 Cod, and 10 Salmon!");
        info.add("TIME: 1 Hour");
        info.add("PROGRESS: " + getFishProgress() + "/4 fish types");
        return info;
    }

    public void tick(Player onlinePlayer) {
        if(onlinePlayer.getInventory().contains(Material.PUFFERFISH, 1) &&
                onlinePlayer.getInventory().contains(Material.TROPICAL_FISH, 1) &&
                onlinePlayer.getInventory().contains(Material.COD, 10) &&
                onlinePlayer.getInventory().contains(Material.SALMON, 10)) {
            main.endQuest(player, true);
        }
    }

    private int getFishProgress() {
        int progress = 0;
        if (countItem(Material.PUFFERFISH) >= 1) progress++;
        if (countItem(Material.TROPICAL_FISH) >= 1) progress++;
        if (countItem(Material.COD) >= 10) progress++;
        if (countItem(Material.SALMON) >= 10) progress++;
        return progress;
    }

    private int countItem(Material type) {
        int count = 0;
        for (ItemStack item : player.getPlayer().getInventory().getContents()) {
            if (item != null && item.getType() == type) {
                count += item.getAmount();
            }
        }
        return count;
    }

    @Override
    public String displayName() {
        return ChatColor.YELLOW + "Fisherman";
    }
}
