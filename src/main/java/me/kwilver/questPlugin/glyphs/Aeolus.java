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

public class Aeolus extends Glyph {
    public Aeolus(Player player) {
        super(player);
    }

    @Override
    protected boolean useGlyph() {
        Location origin = user.getLocation().clone().add(0, 1, 0);
        Set<UUID> hitPlayers = new HashSet<>();

        new BukkitRunnable() {
            double radius = 1.5;
            final double maxRadius = 15.0;

            @Override
            public void run() {
                if (radius > maxRadius) {
                    cancel();
                    return;
                }

                int points = (int) (radius * 12);
                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location loc = origin.clone().add(x, 0, z);

                    DustOptions green = new DustOptions(Color.LIME, 1f);
                    DustOptions white = new DustOptions(Color.WHITE, 0.5f);

                    origin.getWorld().spawnParticle(Particle.DUST, loc, 1, green);
                    if (i % 5 == 0) {
                        origin.getWorld().spawnParticle(Particle.DUST, loc.clone().add(0, 0.2, 0), 1, white);
                    }

                    for (Entity e : origin.getWorld().getNearbyEntities(loc, 0.5, 1, 0.5)) {
                        if (e instanceof Player target
                                && !target.getUniqueId().equals(user.getUniqueId())
                                && !hitPlayers.contains(target.getUniqueId())) {

                            Vector dir = target.getLocation().toVector().subtract(origin.toVector()).normalize();
                            Vector horizontal = dir.clone().setY(0).normalize().multiply(2.5);
                            Vector launchVel = horizontal.clone().setY(3.5);
                            target.setVelocity(launchVel);

                            double newHp = target.getHealth() - 6.0;
                            target.setHealth(Math.max(newHp, 0.0));
                            hitPlayers.add(target.getUniqueId());
                        }
                    }
                }
                radius += 1.5;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        return true;
    }
}