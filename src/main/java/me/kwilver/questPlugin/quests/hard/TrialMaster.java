package me.kwilver.questPlugin.quests.hard;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.lootTables.Hard;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;

public class TrialMaster extends Quest implements Listener {
    int count1 = 0;
    int count2 = 0;

    public TrialMaster(Player player, QuestPlugin main) {
        super(60 * 60, player, main, Hard.class);
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Use 10 Trial Keys and 5 Ominous Keys");
        info.add("TIME: 1 Hour");
        info.add("PROGRESS: " + (count1 + count2) + "/15");
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.RED + "Trial Master";
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.VAULT && e.getPlayer() == player) {
            BlockState state = e.getClickedBlock().getState();
            if (state instanceof Vault vault) {
                if (!vault.hasRewardedPlayer(player.getUniqueId()) && e.getItem() != null && e.getItem().getType() == vault.getKeyItem().getType()) {
                    if (vault.getKeyItem().getType() == Material.TRIAL_KEY && count1 < 10) count1++;
                    if (vault.getKeyItem().getType() == Material.OMINOUS_TRIAL_KEY && count2 < 5) count2++;
                }
                if (count1 >= 10 && count2 >= 5) {
                    main.endQuest(player, true);
                }
            }
        }
    }
}