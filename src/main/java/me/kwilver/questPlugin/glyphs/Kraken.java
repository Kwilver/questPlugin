package me.kwilver.questPlugin.glyphs;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Particle.DustOptions;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Kraken extends Glyph {
    public Kraken(Player player) {
        super(player);
    }

    @Override
    protected boolean useGlyph() {
        Location center = user.getLocation().clone();
        center.setY(user.getLocation().getY());
        Set<UUID> affectedPlayers = new HashSet<>();

        new BukkitRunnable() {
            int ticks = 0;
            final int durationTicks = 60; // 3 seconds

            @Override
            public void run() {
                if (ticks >= durationTicks) {
                    for (UUID uuid : affectedPlayers) {
                        Player target = Bukkit.getPlayer(uuid);
                        if (target != null && target.isOnline()) {
                            Vector launch = target.getLocation().toVector().subtract(center.toVector()).normalize();
                            launch.setY(1.0);
                            target.setVelocity(launch.multiply(1.5));

                            double newHp = target.getHealth() - 6.0;
                            target.setHealth(Math.max(newHp, 0.0));
                        }
                    }
                    cancel();
                    return;
                }

                int particles = 50;
                double angleOffset = ticks * 0.3;
                double yOffset = 0.5 + (Math.sin(ticks * 0.2) * 0.5);

                for (int i = 0; i < particles; i++) {
                    double angle = 2 * Math.PI * i / particles + angleOffset;
                    double radius = 5 + (Math.sin(ticks * 0.1 + i) * 2);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location particleLoc = center.clone().add(x, yOffset, z);

                    DustOptions magenta = new DustOptions(Color.FUCHSIA, 1);
                    DustOptions blue = new DustOptions(Color.NAVY, 0.8f);
                    center.getWorld().spawnParticle(Particle.DUST, particleLoc, 0, 0, 0, 0, 1, magenta);
                    if (i % 10 == 0)
                        center.getWorld().spawnParticle(Particle.DUST, particleLoc, 0, 0, 0, 0, 1, blue);
                }

                for (Entity e : center.getWorld().getNearbyEntities(center, 15, 5, 15)) {
                    if (e instanceof Player target && !target.getUniqueId().equals(user.getUniqueId())) {
                        affectedPlayers.add(target.getUniqueId());

                        Vector pull = center.toVector().subtract(target.getLocation().toVector()).normalize();
                        pull.setY(0.1);
                        target.setVelocity(target.getVelocity().multiply(0.5).add(pull.multiply(0.6)));
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);

        return true;
    }
}