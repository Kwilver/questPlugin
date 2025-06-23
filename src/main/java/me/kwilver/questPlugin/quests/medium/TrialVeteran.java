package me.kwilver.questPlugin.quests.medium;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.lootTables.Easy;
import me.kwilver.questPlugin.lootTables.Medium;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;

public class TrialVeteran extends Quest implements Listener {
    int count1 = 0;
    int count2 = 0;

    public TrialVeteran(Player player, QuestPlugin main) {
        super(2 * 60 * 60, player, main, Medium.class);
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Use 5 Trial Keys and 3 Ominous Keys!");
        info.add("TIME: 2 Hours");
        info.add("PROGRESS: " + (count1 + count2) + "/8");
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.YELLOW + "Trial Veteran";
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if(e.getClickedBlock().getType() == Material.VAULT && e.getPlayer() == player) {
            BlockState state = e.getClickedBlock().getState();
            if(state instanceof Vault vault) {
                if(!vault.hasRewardedPlayer(player.getUniqueId()) && e.getItem() != null && e.getItem().getType() == vault.getKeyItem().getType()) {
                    if(vault.getKeyItem().getType() == Material.TRIAL_KEY && count1 < 5) count1++;
                    if(vault.getKeyItem().getType() == Material.OMINOUS_TRIAL_KEY && count2 < 3) count2++;
                }
                if(count1 >= 5 && count2 >= 3) {
                    main.endQuest(player, true);
                }
            }
        }
    }
}
