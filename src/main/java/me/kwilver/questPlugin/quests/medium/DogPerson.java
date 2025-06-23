package me.kwilver.questPlugin.quests.medium;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.lootTables.Medium;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

import java.util.ArrayList;
import java.util.List;

public class DogPerson extends Quest implements Listener {
    public DogPerson(Player player, QuestPlugin main) {
        super(60 * 60, player, main, Medium.class);
    }
    List<Wolf.Variant> wolfTypes = new ArrayList<>();

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Tame 3 different breeds of wolves!");
        info.add("TIME: 1 Hour");
        info.add("PROGRESS: " + "/3");
        return info;
    }

    @EventHandler
    public void onPlayerTame(EntityTameEvent e) {
        if (e.getEntity().getType() == EntityType.WOLF) {
            Wolf wolf = (Wolf) e.getEntity();
            if(!wolfTypes.contains(wolf.getVariant())) {
                wolfTypes.add(wolf.getVariant());
                if(wolfTypes.size() >= 3) {
                    main.endQuest(player, true);
                }
            }
        }
    }

    @Override
    public String displayName() {
        return ChatColor.YELLOW + "Dog Person";
    }
}
