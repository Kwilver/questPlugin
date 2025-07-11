package me.kwilver.questPlugin.glyphs;

import org.bukkit.*;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class GrandSlam extends Glyph implements Listener {
    boolean slamming = true;

    public GrandSlam(Player player) {
        super(player);
    }

    void dealDamage() {
        final double RADIUS = 15;
        final double VERT_THRESHOLD = 3;

        Location src = user.getLocation();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target == user) continue;

            double dx = target.getX() - src.getX();
            double dy = target.getY() - src.getY();
            double dz = target.getZ() - src.getZ();

            if (Math.abs(dy) <= VERT_THRESHOLD) {
                dy = 0;
            } else {
                dy = Math.signum(dy) * (Math.abs(dy) - VERT_THRESHOLD);
            }

            double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
            if (dist < RADIUS) {
                target.damage(RADIUS - dist);
                target.setVelocity(target.getVelocity().add(new Vector(0, .4, 0)));
            }
        }
    }


    void spawnParticles() {
        new BukkitRunnable() {
            double radius = 1;
            @Override
            public void run() {
                radius+= 0.5;

                if(radius >= 10) {
                    cancel();
                    return;
                }

                for(int i = 0; i < 100; i++) {
                    double angle = 2 * Math.PI * i / 100;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    Location loc = user.getLocation().clone().add(x, 0 , z);

                    user.getWorld().spawnParticle(Particle.CRIT, loc, 1);
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if(e.getEntity() == user) {
            e.setCancelled(slamming);
        }
    }

    @Override
    protected boolean useGlyph() {
        getServer().getPluginManager().registerEvents(this, plugin);
        user.setVelocity(new Vector(0, 10, 0));
        user.getWorld().playSound(user.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 1, 1);

        new BukkitRunnable() {
            @Override
            public void run () {
                user.setVelocity(new Vector(0, -20, 0));
            }
        }.runTaskLater(plugin, 20);

        BukkitRunnable slammingrunnable = new BukkitRunnable() {
            @Override
            public void run() {
                HandlerList.unregisterAll(GrandSlam.this);
            }
        };

        new BukkitRunnable() {
            @Override
            public void run() {
                if (user.isOnGround()) {
                    spawnParticles();
                    dealDamage();
                    user.getWorld().playSound(user.getLocation(), Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 1, 1);
                    user.getWorld().playSound(user.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1, 1);

                    slammingrunnable.runTaskLater(plugin, 20);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 21, 1);
        return true;
    }
}
