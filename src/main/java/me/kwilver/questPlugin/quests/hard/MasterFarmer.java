package me.kwilver.questPlugin.quests.hard;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.TickingQuest;
import me.kwilver.questPlugin.lootTables.Hard;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MasterFarmer extends Quest implements TickingQuest {
    List<Material> choices = new ArrayList<>();
    int amount = 0;

    public MasterFarmer(Player player, QuestPlugin main) {
        super(1 * 60 * 60, player, main, Hard.class);

        choices.add(Material.WHEAT);
        choices.add(Material.CARROT);
        choices.add(Material.POTATO);
        choices.add(Material.BEETROOT);
        choices.add(Material.MELON);
        choices.add(Material.PUMPKIN);
        choices.add(Material.TORCHFLOWER);
        choices.add(Material.PITCHER_PLANT);
        choices.add(Material.SUGAR_CANE);
        choices.add(Material.SWEET_BERRIES);
        choices.add(Material.BAMBOO);
        choices.add(Material.COCOA_BEANS);
    }

    public void tick(Player onlinePlayer) {
        for(Material m : choices) {
            if(getAmountComplete(onlinePlayer, m) < 10) {
                return;
            }
        }
        main.endQuest(onlinePlayer, true);
    }

    private int getAmountComplete(Player p, Material material) {
        int amount = 0;
        if(p.getInventory().isEmpty()) return 0;

        for(ItemStack i : p.getInventory()) {
            if(i != null && i.getType() == material) {
                amount += i.getAmount();
            }
        }
        return Math.min(amount, 10);
    }

    private int completion(Player p) {
        int amount = 0;
        for(Material m : choices) {
            amount += getAmountComplete(p, m);
        }
        return amount;
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Collect 10 of the following: ");
        for(Material m : choices) {
            ItemStack stack = new ItemStack(m);
            String name = stack.getItemMeta().getDisplayName();

            info.add(" - " + name);
        }
        info.add("TIME: 1 Hour");
        info.add("PROGRESS: " + completion(player.getPlayer()) + "/" + 10 * choices.size());
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.RED + "Master Farmer";
    }
}
