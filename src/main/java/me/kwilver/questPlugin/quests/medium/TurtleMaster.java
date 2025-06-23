package me.kwilver.questPlugin.quests.medium;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.lootTables.Medium;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class TurtleMaster extends Quest implements Listener {
    int count = 0;

    public TurtleMaster(Player player, QuestPlugin main) {
        super(60 * 60, player, main, Medium.class);
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Craft 3 turtle helmets!");
        info.add("TIME: 1 Hour");
        info.add("PROGRESS: " + count + "/3");
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.YELLOW + "Turtle Master";
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent e) {
        ItemStack result = e.getInventory().getResult();
        if (result == null || result.getType() != Material.TURTLE_HELMET) return;

        CraftingInventory inv = e.getInventory();
        int totalScutes = 0;
        for (ItemStack slot : inv.getMatrix()) {
            if (slot != null && slot.getType() == Material.TURTLE_SCUTE) {
                totalScutes += slot.getAmount();
            }
        }

        int helmetsCrafted = totalScutes / 5;
        count += helmetsCrafted;
        if (count >= 3) {
            main.endQuest(player, true);
        }
    }
}
