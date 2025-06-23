package me.kwilver.questPlugin.quests.medium;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.lootTables.Medium;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class CatPerson extends Quest implements Listener {
    public CatPerson(Player player, QuestPlugin main) {
        super(60 * 60, player, main, Medium.class);
    }

    List<Cat.Type> catTypes = new ArrayList<>();

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Tame 3 different breeds of cats!");
        info.add("TIME: 1 Hour");
        info.add("PROGRESS: " + catTypes.size() + "/3");
        return info;
    }

    @EventHandler
    public void onPlayerTame(EntityTameEvent e) {
        if (e.getEntity().getType() == EntityType.CAT) {
            Cat cat = (Cat) e.getEntity();
            if(!catTypes.contains(cat.getCatType())) {
                catTypes.add(cat.getCatType());
                if(catTypes.size() >= 3) {
                    main.endQuest(player, true);
                }
            }
        }
    }

    @Override
    public String displayName() {
        return ChatColor.YELLOW + "Cat Person";
    }
}
