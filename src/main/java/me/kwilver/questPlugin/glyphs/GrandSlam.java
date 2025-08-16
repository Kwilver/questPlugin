package me.kwilver.questPlugin.glyphs;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
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
                target.setHealth(target.getHealth() - (RADIUS - dist));
                target.setVelocity(target.getVelocity().add(new Vector(0, .4, 0)));
            }
        }
    }


    public void spawnParticles() {
        new BukkitRunnable() {
            double radius = 1;
            final Particle.DustOptions[] rainbow = new Particle.DustOptions[] {
                    new Particle.DustOptions(Color.RED, 1f),
                    new Particle.DustOptions(Color.ORANGE, 1f),
                    new Particle.DustOptions(Color.YELLOW, 1f),
                    new Particle.DustOptions(Color.GREEN, 1f),
                    new Particle.DustOptions(Color.BLUE, 1f),
                    new Particle.DustOptions(Color.PURPLE, 1f)
            };

            @Override
            public void run() {
                radius += 0.5;
                if (radius >= 10) {
                    cancel();
                    return;
                }

                // Pulsing ring
                boolean flash = ((int) radius) % 2 == 0;
                Particle ringParticle = flash ? Particle.FIREWORK : Particle.SMOKE;
                for (int deg = 0; deg < 360; deg += 10) {
                    double rad = Math.toRadians(deg);
                    double x = radius * Math.cos(rad);
                    double z = radius * Math.sin(rad);
                    double y = Math.sin(radius) + 1;
                    Location loc = user.getLocation().clone().add(x, y, z);

                    user.getWorld().spawnParticle(ringParticle, loc, 1, 0, 0, 0, 0);
                    user.getWorld().spawnParticle(Particle.CRIT, loc, 1, 0, 0, 0, 0);
                }

                // Center blast after halfway
                if (radius > 5) {
                    user.getWorld().spawnParticle(Particle.EXPLOSION, user.getLocation(), 1);
                }

                // Rainbow upward swirl
                for (int i = 0; i < rainbow.length; i++) {
                    double swirlRadius = radius * 0.2;
                    double angle = radius * 2 + i * (2 * Math.PI / rainbow.length);
                    double x = swirlRadius * Math.cos(angle);
                    double z = swirlRadius * Math.sin(angle);
                    double y = radius * 0.5; // rising effect
                    Location loc = user.getLocation().clone().add(x, y, z);

                    user.getWorld().spawnParticle(Particle.DUST, loc, 1, rainbow[i]);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
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