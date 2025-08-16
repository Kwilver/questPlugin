package me.kwilver.questPlugin.quests.easy;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.lootTables.Easy;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Miner extends Quest implements Listener {
    Material selection;
    int count = 0;

    public Miner(Player player, QuestPlugin main) {
        super(3 * 60 * 60, player, main, Easy.class);

        List<Material> choices = new ArrayList<>();
        choices.add(Material.COAL);
        choices.add(Material.RAW_IRON);
        choices.add(Material.RAW_COPPER);
        choices.add(Material.REDSTONE);
        choices.add(Material.RAW_GOLD);
        choices.add(Material.LAPIS_LAZULI);

        selection = choices.get(new Random().nextInt(choices.size()));
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Mine 64 ores worth of " + ChatColor.YELLOW + selection.name() + ChatColor.WHITE + "!");
        info.add("TIME: 3 Hours");
        info.add("PROGRESS: " + count + "/64");
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.GREEN + "Miner";
    }

    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent e) {
        if(e.getPlayer() == player) {
            for(Item item : e.getItems()) {
                if(item.getItemStack().getType() == selection) {
                    count += item.getItemStack().getAmount();
                    if(count >= 64) {
                        main.endQuest(player, true);
                    }
                }
            }
        }
    }
}
