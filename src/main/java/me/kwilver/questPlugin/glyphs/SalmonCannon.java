package me.kwilver.questPlugin.glyphs;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class SalmonCannon extends Glyph implements Listener {
    Location explosion;

    public SalmonCannon(Player player) {
        super(player);
    }

    void explode(Location l, Entity salmon) {
        explosion = l;

        World world = l.getWorld();
        double explosionRadius = 6.0;
        int baseDamage = 4;

        world.spawnParticle(Particle.FALLING_WATER, l, 600, 1, 1, 1, 0.3);

        for (Entity entity : world.getNearbyEntities(l, explosionRadius, explosionRadius, explosionRadius)) {
            if (entity instanceof LivingEntity) {
                Location loc = entity.getLocation();
                double distance = loc.distance(l);
                if (distance > explosionRadius) continue;

                int maxDamage = baseDamage;
                if (loc.getBlock().getType() == Material.WATER) {
                    maxDamage = (int) (maxDamage * 1.5);
                }

                double damage = Math.ceil(maxDamage - distance) * 8;
                if (damage > 0) {
                    ((LivingEntity) entity).damage(damage);
                }
            }
        }

        salmon.remove();
    }

    @Override
    protected boolean useGlyph() {
        //launching salmon
        Entity salmon = user.getWorld().spawnEntity(
                user.getLocation().clone().add(0, 1.5, 0),
                EntityType.SALMON);

        Vector direction = user.getLocation().getDirection().normalize();

        Vector velocity = direction.multiply(2);

        salmon.setVelocity(velocity);

        //visuals and audio
        user.getWorld().playSound(user.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1, 1);
        user.getWorld().playSound(user.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);

        Random random = new Random();
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (salmon.isDead() || tick >= 5 * 20) {
                    explode(salmon.getLocation(), salmon);
                    cancel();
                }

                //explode on collision
                for(Entity e : salmon.getNearbyEntities(0.1, 0.1, 0.1)) {
                    if(e instanceof LivingEntity && (!e.equals(user))) {
                        explode(salmon.getLocation(), salmon);
                        cancel();
                    }
                }

                if (random.nextInt(2) == 1) {
                    salmon.getWorld().spawnParticle(
                            Particle.CAMPFIRE_COSY_SMOKE,
                            salmon.getLocation(),
                            3,
                            0, 0, 0, // offsets
                            0        // speed
                    );

                    salmon.getWorld().spawnParticle(
                            Particle.SMOKE,
                            salmon.getLocation(),
                            3,
                            0, 0, 0, // offsets
                            0        // speed
                    );
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0, 1);
        return true;
    }
}
