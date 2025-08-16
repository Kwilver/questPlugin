package me.kwilver.questPlugin.glyphs;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
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
        if (e.getEntity().equals(user) && active && e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            e.setCancelled(true);
        }
    }

    @Override
    protected boolean useGlyph() {
        Vector direction = user.getLocation().getDirection().normalize();
        Vector velocity = direction.multiply(6); // ~5-7 blocks forward
        velocity.setY(0.75); // ~2 blocks up
        user.setVelocity(velocity);

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        new BukkitRunnable() {
            private int groundTicks = 0;

            @Override
            public void run() {
                user.getWorld().spawnParticle(
                        Particle.SOUL_FIRE_FLAME,
                        user.getLocation(),
                        8,
                        0.2, 0.2, 0.2,
                        0.03
                );
                if (user.isOnGround()) {
                    if (++groundTicks > 5) {
                        active = false;
                        HandlerList.unregisterAll(Dash.this);
                        cancel();
                    }
                } else {
                    groundTicks = 0;
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }
}