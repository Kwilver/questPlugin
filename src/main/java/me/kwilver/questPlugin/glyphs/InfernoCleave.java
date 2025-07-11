package me.kwilver.questPlugin.glyphs;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Color;

import java.util.ArrayList;
import java.util.List;

public class InfernoCleave extends Glyph {
    public InfernoCleave(Player player) {
        super(player);
    }

    @Override
    protected boolean useGlyph() {
        List<Player> damaged = new ArrayList<>();
        Location origin = user.getLocation().clone();
        origin.setY(user.getLocation().getY());
        Vector forward = origin.getDirection().setY(0).normalize();

        user.getWorld().playSound(user.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 0.6f);

        new BukkitRunnable() {
            int ticks = 1;

            @Override
            public void run() {
                int points = ticks * 2;
                double angleStep = Math.PI / (points - 1);
                double radius = ticks * 0.6;

                for (int i = 0; i < points; i++) {
                    double angle = angleStep * i - (Math.PI / 2);
                    Vector offset = forward.clone().rotateAroundY(angle).normalize().multiply(radius);
                    Location stepLoc = origin.clone().add(offset);
                    stepLoc.setY(origin.getY());

                    origin.getWorld().spawnParticle(
                            Particle.FLAME,
                            stepLoc,
                            0,
                            0.2, 0.2, 0.2, 0.01
                    );

                    origin.getWorld().spawnParticle(
                            Particle.LAVA,
                            stepLoc.clone().add(0, 0.2, 0),
                            1,
                            0.1, 0.1, 0.1, 0.02
                    );

                    DustOptions red = new DustOptions(Color.fromRGB(255, 64, 0), 1.2f);
                    origin.getWorld().spawnParticle(
                            Particle.DUST,
                            stepLoc.clone().subtract(forward.clone().multiply(0.2)),
                            1, 0, 0, 0, 0, red
                    );

                    for (Entity e : origin.getWorld().getNearbyEntities(stepLoc, 0.5, 1.0, 0.5)) {
                        if (e instanceof Player target && !target.getUniqueId().equals(user.getUniqueId()) && !damaged.contains(target)) {
                            target.setVelocity(target.getVelocity().add(new Vector(0, 0.5, 0)));
                            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.2f, 1.2f);
                            target.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
                            target.setHealth(Math.max(0, target.getHealth() - 6.0));
                            damaged.add(target);
                        }
                    }
                }

                ticks++;
                if (ticks > 25) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);

        return true;
    }
}
