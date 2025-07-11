package me.kwilver.questPlugin.quests.hard;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.lootTables.Easy;
import me.kwilver.questPlugin.lootTables.Hard;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrueHunter extends Quest implements Listener {
    EntityType selection;

    public TrueHunter(Player player, QuestPlugin main) {
        super(3 * 60 * 60, player, main, Hard.class);

        List<EntityType> choices = new ArrayList<>();
        choices.add(EntityType.WITHER);
        choices.add(EntityType.ENDER_DRAGON);
        choices.add(EntityType.WARDEN);

        selection = choices.get(new Random().nextInt(choices.size()));
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Kill the " + ChatColor.YELLOW + selection.name() + ChatColor.WHITE + "...");
        info.add("TIME: 1 Hour");
        info.add("PROGRESS: 0/1");
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.RED + "True Hunter";
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if(e.getEntity().getKiller() != null && e.getEntity().getKiller() == player) {
            if(e.getEntity().getType() == selection) {
                main.endQuest(player, true);
            }
        }
    }
}
