package me.kwilver.questPlugin.glyphs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Dash extends Glyph implements Listener {
    boolean active = true;

    public Dash(Player player) {
        super(player);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if(e.getEntity().equals(user) && active && e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            e.setCancelled(true);
        }
    }

    @Override
    protected boolean useGlyph() {
        Vector direction = user.getLocation().getDirection().normalize();
        Vector velocity = direction.multiply(10);
        velocity.setY(2);

        user.setVelocity(velocity);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (user.isOnGround()) {
                    ticks++;
                    if (ticks > 10) {
                        HandlerList.unregisterAll(Dash.this);
                        cancel();
                    }
                } else {
                    ticks = 0;
                }

            }
        }.runTaskTimer(plugin, 0, 1);

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        return true;
    }
}
