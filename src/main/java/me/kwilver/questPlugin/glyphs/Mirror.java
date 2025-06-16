package me.kwilver.questPlugin.glyphs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class Mirror extends Glyph implements Listener {
    int hits = 0;
    boolean active = true;

    public Mirror(Player player) {
        super(player);
    }


    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if(e.getEntity().equals(user) && hits <= 10 && active) {
            if(e.getDamager() instanceof LivingEntity attacker) {
                attacker.damage(e.getDamage() / 2);
                hits++;
            }
        } else {
            HandlerList.unregisterAll(Mirror.this);
        }
    }

    public void spawnSphere(Player player, double radius, int points) {
        Location center = player.getLocation().add(0, 1, 0); // center of sphere

        for (double phi = 0; phi <= Math.PI; phi += Math.PI / points) {
            for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / points) {
                double x = radius * Math.sin(phi) * Math.cos(theta);
                double y = radius * Math.cos(phi);
                double z = radius * Math.sin(phi) * Math.sin(theta);

                Location particleLoc = center.clone().add(x, y, z);

                Particle particle = new Random().nextInt(2) == 0 ? Particle.WHITE_SMOKE : Particle.WHITE_ASH;

                player.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    protected boolean useGlyph() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        BukkitRunnable particleRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                spawnSphere(user, 2, 15);
            }
        };

        particleRunnable.runTaskTimer(plugin, 0, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                active = false;
                particleRunnable.cancel();
            }
        }.runTaskLater(plugin, 25 * 20);

        this.user = user;
        return true;
    }
}
