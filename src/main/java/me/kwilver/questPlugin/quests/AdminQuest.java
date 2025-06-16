package me.kwilver.questPlugin.quests;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.TickingQuest;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class AdminQuest extends Quest implements TickingQuest {
    public AdminQuest(Player player, QuestPlugin main) {
        super(30 * 1000, player, main);
    }

    public void tick(Player onlinePlayer) {
        if(onlinePlayer.getInventory().contains(Material.LADDER)) {
            main.endQuest(player, true);
        }
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Collect ");
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.GREEN + "Farmer";
    }
}
