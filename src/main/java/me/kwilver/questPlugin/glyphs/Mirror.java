package me.kwilver.questPlugin.glyphs;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Mirror extends Glyph implements Listener {
    private int hits = 0;
    private boolean active = true;

    public Mirror(Player player) {
        super(player);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!active) return;
        if (e.getEntity().equals(user) && hits < 10) {
            if (e.getDamager() instanceof LivingEntity attacker) {
                attacker.damage(e.getDamage() * 0.5);
                hits++;
            }
        } else if (e.getEntity().equals(user)) {
            HandlerList.unregisterAll(this);
        }
    }

    private void spawnCleanSphere(Player player, double radius, int points) {
        Location center = player.getLocation().add(0, 1.2, 0);
        double increment = Math.PI * (3 - Math.sqrt(5));  // golden angle
        double offset = 2.0 / points;

        for (int i = 0; i < points; i++) {
            double y = i * offset - 1 + (offset / 2);
            double r = Math.sqrt(1 - y * y);
            double phi = i * increment;
            double x = Math.cos(phi) * r;
            double z = Math.sin(phi) * r;

            Location spawnLoc = center.clone().add(x * radius, y * radius, z * radius);
            DustOptions dust = new DustOptions(Color.fromRGB(0, 200, 255), 0.6f);
            player.getWorld().spawnParticle(Particle.DUST, spawnLoc, 1, 0, 0, 0, dust);
        }
    }

    @Override
    protected boolean useGlyph() {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        BukkitRunnable particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) {
                    this.cancel();
                    return;
                }
                spawnCleanSphere(user, 2.0, 120);
            }
        };
        particleTask.runTaskTimer(plugin, 0L, 2L);

        new BukkitRunnable() {
            @Override
            public void run() {
                active = false;
                HandlerList.unregisterAll(Mirror.this);
            }
        }.runTaskLater(plugin, 5 * 20L);

        return true;
    }
}