package me.kwilver.questPlugin.quests.easy;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.lootTables.Easy;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.VaultDisplayItemEvent;
import org.bukkit.event.entity.TrialSpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TrialEnthusiast extends Quest implements Listener {
    int count = 0;

    public TrialEnthusiast(Player player, QuestPlugin main) {
        super(2 * 60 * 60, player, main, Easy.class);
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Use 5 Trial Keys!");
        info.add("TIME: 2 Hours");
        info.add("PROGRESS: " + count + "/5");
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.GREEN + "Trial Enthusiast";
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if(e.getClickedBlock().getType() == Material.VAULT && e.getPlayer() == player) {
            BlockState state = e.getClickedBlock().getState();
            if(state instanceof Vault vault) {
                if(!vault.hasRewardedPlayer(player.getUniqueId()) && e.getItem() != null && e.getItem().getType() == Material.TRIAL_KEY) {
                    count++;
                    if(count >= 5) {
                        main.endQuest(player, true);
                    }
                }
            }
        }
    }
}
