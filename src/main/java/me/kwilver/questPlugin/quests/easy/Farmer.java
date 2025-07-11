package me.kwilver.questPlugin.quests.easy;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.TickingQuest;
import me.kwilver.questPlugin.lootTables.Easy;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Farmer extends Quest implements TickingQuest {
    Material selection;
    int amount = 0;

    public Farmer(Player player, QuestPlugin main) {
        super(3 * 60 * 60, player, main, Easy.class);

        List<Material> choices = new ArrayList<>();
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

        selection = choices.get(new Random().nextInt(choices.size()));
    }

    public void tick(Player onlinePlayer) {
        for(ItemStack i : onlinePlayer.getInventory()) {
            if(i != null && i.getType() == selection) {
                amount = i.getAmount();
                if(i.getAmount() >= 30) {
                    main.endQuest(onlinePlayer, true);
                }
            }
        }
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Collect 30 " + ChatColor.YELLOW + selection.name() + ChatColor.WHITE + "!");
        info.add("TIME: 3 Hours");
        info.add("PROGRESS: " + amount + "/30");
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.GREEN + "Farmer";
    }
}
