package me.kwilver.questPlugin.quests.medium;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.TickingQuest;
import me.kwilver.questPlugin.lootTables.Medium;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class Archaeologist extends Quest implements Listener {
    public Archaeologist(Player player, QuestPlugin main) {
        super(60 * 60, player, main, Medium.class);
    }

    int count = 0;

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("GOAL: Brush 3 Suspicious Sand or Gravel!");
        info.add("TIME: 1 Hour");
        info.add("PROGRESS: " + count + "/3");
        return info;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!e.getPlayer().equals(player)) return;

        if (e.getClickedBlock() != null &&
                (e.getClickedBlock().getType() == Material.SUSPICIOUS_GRAVEL ||
                        e.getClickedBlock().getType() == Material.SAND)) {

            Location targetBlock = e.getClickedBlock().getLocation();

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) return;

                    ItemStack mainHand = player.getPlayer().getInventory().getItemInMainHand();
                    ItemStack offHand = player.getPlayer().getInventory().getItemInOffHand();

                    boolean holdingBrush = (mainHand.getType() == Material.BRUSH || offHand.getType() == Material.BRUSH);

                    Block targetedBlock = player.getPlayer().getTargetBlockExact(5); // 5-block reach
                    boolean stillLookingAtTarget = targetedBlock != null && targetedBlock.getLocation().equals(targetBlock);

                    if (holdingBrush && stillLookingAtTarget) {
                        count++;
                    }
                }
            }.runTaskLater(QuestPlugin.getInstance(), 4 * 20);
        }
    }

    @Override
    public String displayName() {
        return ChatColor.YELLOW + "Archaeologist";
    }
}
