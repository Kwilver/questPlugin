package me.kwilver.questPlugin.glyphs;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class Conductor extends Glyph {

    private final boolean testMode = true; // Set to false to return to normal behavior

    public Conductor(Player player) {
        super(player);
    }

    @Override
    protected boolean useGlyph() {
        Set<UUID> hitPlayers = new HashSet<>();
        List<Player> chain = new ArrayList<>();
        chain.add(user);

        new BukkitRunnable() {
            int hits = 0;
            Location currentLocation = user.getLocation();
            Player lastPlayer = user;

            @Override
            public void run() {
                if (hits >= 5) {
                    cancel();
                    return;
                }

                Player next = testMode
                        ? getNearestPlayerTestMode(currentLocation, lastPlayer)
                        : getNearestPlayer(currentLocation, hitPlayers, 10);

                if (next == null) {
                    cancel();
                    return;
                }

                if (!testMode) {
                    hitPlayers.add(next.getUniqueId());
                }

                hits++;
                double damage = 1 + (hits - 1) * 0.5;
                next.setHealth(next.getHealth() - damage);
                next.setVelocity(next.getVelocity().add(new Vector(0, 0.3, 0)));
                next.getWorld().playSound(next.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK,1 ,1);
                chain.add(next);

                spawnBoltEffect(currentLocation, next.getLocation());
                electrifyEffect(next);

                currentLocation = next.getLocation();
                lastPlayer = next;
            }
        }.runTaskTimer(plugin, 0, 10); // 10 ticks = 0.5 seconds

        return true;
    }

    private Player getNearestPlayer(Location origin, Set<UUID> exclude, double maxDistance) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> !exclude.contains(p.getUniqueId()) && !p.equals(user))
                .filter(p -> p.getLocation().distanceSquared(origin) <= maxDistance * maxDistance)
                .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(origin)))
                .orElse(null);
    }

    // Test mode version: bounce between user and nearest player (including user)
    private Player getNearestPlayerTestMode(Location origin, Player lastHit) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.equals(lastHit))
                .filter(p -> p.getLocation().distanceSquared(origin) <= 100) // 10 blocks
                .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(origin)))
                .orElse(null);
    }

    private void spawnBoltEffect(Location from, Location to) {
        int points = 10;
        for (int i = 0; i <= points; i++) {
            double t = i / (double) points;
            Location point = from.clone().add(to.clone().subtract(from).multiply(t));
            from.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.YELLOW, 1));
        }
    }

    private void electrifyEffect(Player p) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 20) {
                    cancel();
                    return;
                }
                p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 10, 0.5, 1, 0.5, 0,
                        new Particle.DustOptions(Color.YELLOW, 1));
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}
