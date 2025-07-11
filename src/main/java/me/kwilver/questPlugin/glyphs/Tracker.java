package me.kwilver.questPlugin.glyphs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Tracker extends Glyph implements Listener {
    List<UUID> tracked = new ArrayList<>();

    public Tracker(Player player) {
        super(player);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                List<String> output = new ArrayList<>();
                output.add(ChatColor.GOLD + "" + ChatColor.BOLD + "Tracked Players:");

                for(UUID id : tracked) {
                    if(Bukkit.getPlayer(id) != null) {
                        Player p = Bukkit.getPlayer(id);
                        assert p != null;
                        Location l = p.getLocation();
                        output.add(" - " + p.getDisplayName() + "'s Coordinates: " + l.getWorld().getEnvironment().name() + " " +
                                l.getBlockX() + " " +
                                l.getBlockY() + " " +
                                l.getBlockZ() + " ");
                    }
                }

                for(Player p : Bukkit.getOnlinePlayers()) {
                    for(String s : output) {
                        p.sendMessage(s);
                    }
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }

                ticks++;

                if(ticks >= 10) {
                    for(Player p : Bukkit.getOnlinePlayers()) {
                        for(String s : output) {
                            p.sendMessage(ChatColor.RED + "Tracking has ended!");
                        }
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20 * 30);
    }

    @Override
    protected boolean useGlyph() {
        new BukkitRunnable() {
            @Override
            public void run () {
                HandlerList.unregisterAll(Tracker.this);
            }
        }.runTaskLater(plugin, 30 * 20 * 60);

        return true;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if(e.getDamager() == user) {
            if(e.getEntity() instanceof Player p && !tracked.contains(p.getUniqueId())) {
                user.sendMessage("Now tracking " + ChatColor.GOLD + p.getName() + ChatColor.WHITE + "!");
                tracked.add(p.getUniqueId());
            }
        }
    }
}
