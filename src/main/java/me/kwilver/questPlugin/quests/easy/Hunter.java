package me.kwilver.questPlugin.quests.easy;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.lootTables.Easy;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Hunter extends Quest implements Listener {
    EntityType selection;
    int count = 0;

    public Hunter(Player player, QuestPlugin main) {
        super(3 * 60 * 60, player, main, Easy.class);

        List<EntityType> choices = new ArrayList<>();
        choices.add(EntityType.WITHER_SKELETON);
        choices.add(EntityType.SKELETON);
        choices.add(EntityType.SPIDER);
        choices.add(EntityType.BLAZE);
        choices.add(EntityType.CREEPER);
        choices.add(EntityType.BREEZE);
        choices.add(EntityType.DROWNED);
        choices.add(EntityType.STRAY);
        choices.add(EntityType.BOGGED);
        choices.add(EntityType.PILLAGER);

        selection = choices.get(new Random().nextInt(choices.size()));
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Track down and kill 50 " + ChatColor.YELLOW + selection.name() + ChatColor.WHITE + "!");
        info.add("TIME: 3 Hours");
        info.add("PROGRESS: " + count + "/50");
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.GREEN + "Hunter";
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if(e.getEntity().getKiller() != null && e.getEntity().getKiller() == player) {
            if(e.getEntity().getType() == selection) {
                count++;
                if(count >= 50) {
                    main.endQuest(player, true);
                }
            }
        }
    }
}
