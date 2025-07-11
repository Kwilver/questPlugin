package me.kwilver.questPlugin.glyphs;

import me.kwilver.questPlugin.QuestPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Curse extends Glyph implements Listener {

    private static final long DURATION_MS = 30 * 1000L;
    private static final long CURSE_LENGTH_MS = 45 * 1000L;
    private static final long SPHERE_DURATION_MS = 7 * 1000L;

    private long activationTime;
    private boolean awaitingHit = false;
    private static final Set<UUID> cursedPlayers = new HashSet<>();

    public Curse(Player player) {
        super(player);
        Bukkit.getServer().getPluginManager().registerEvents(this, QuestPlugin.getInstance());
    }

    @Override
    protected boolean useGlyph() {
        activationTime = System.currentTimeMillis();
        awaitingHit = true;
        user.sendMessage(ChatColor.DARK_RED + "You have 30 seconds to curse a player. Strike now!");

        new BukkitRunnable() {
            @Override
            public void run() {
                awaitingHit = false;
            }
        }.runTaskLater(QuestPlugin.getInstance(), DURATION_MS / 50);

        return true;
    }

    public static void applyCurse(Player target) {
        if (cursedPlayers.contains(target.getUniqueId())) return;
        cursedPlayers.add(target.getUniqueId());

        Location originalLoc = target.getLocation();
        Location center = originalLoc.clone().add(0, 1, 0);
        World world = target.getWorld();

        BukkitRunnable freezeTask = new BukkitRunnable() {
            long elapsed = 0;

            @Override
            public void run() {
                if (!target.isOnline() || !cursedPlayers.contains(target.getUniqueId())) {
                    cancel();
                    return;
                }
                if (elapsed >= SPHERE_DURATION_MS) {
                    cancel();
                    return;
                }
                target.teleport(center.clone().subtract(0, 1, 0));
                target.setVelocity(new Vector(0, 0, 0));
                drawParticleSphere(center, 3.5, 25);

                elapsed += 50;
            }
        };
        freezeTask.runTaskTimer(QuestPlugin.getInstance(), 0L, 1L);

        new BukkitRunnable() {
            long elapsed = 0;
            final double cursedMaxHealth = 14.0;

            @Override
            public void run() {
                if (!target.isOnline() || !cursedPlayers.contains(target.getUniqueId())) {
                    cancel();
                    cursedPlayers.remove(target.getUniqueId());
                    target.setMaxHealth(20.0);
                    return;
                }
                if (elapsed >= CURSE_LENGTH_MS) {
                    target.setMaxHealth(20.0);
                    cursedPlayers.remove(target.getUniqueId());
                    cancel();
                    return;
                }

                world.spawnParticle(
                        Particle.DUST,
                        target.getLocation().add(0, 1, 0),
                        10,
                        0.5, 0.5, 0.5,
                        0,
                        new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1)
                );

                target.setMaxHealth(cursedMaxHealth);
                if (target.getHealth() > cursedMaxHealth) {
                    target.setHealth(cursedMaxHealth);
                }

                elapsed += 1000;
            }
        }.runTaskTimer(QuestPlugin.getInstance(), SPHERE_DURATION_MS / 50, 20L);
    }

    private static void drawParticleSphere(Location center, double radius, int count) {
        World world = center.getWorld();
        for (double theta = 0; theta < Math.PI; theta += Math.PI / count) {
            for (double phi = 0; phi < 2 * Math.PI; phi += Math.PI / count) {
                double x = radius * Math.sin(theta) * Math.cos(phi);
                double y = radius * Math.cos(theta);
                double z = radius * Math.sin(theta) * Math.sin(phi);

                Vector offset = new Vector(x, y, z);
                Location particleLoc = center.clone().add(offset);

                world.spawnParticle(
                        Particle.DUST,
                        particleLoc,
                        1,
                        new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.2f)
                );
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        if (!awaitingHit) return;
        if (!(e.getDamager() instanceof Player damager)) return;
        if (!(e.getEntity() instanceof Player target)) return;
        if (!damager.getUniqueId().equals(user.getUniqueId())) return;

        awaitingHit = false;
        user.sendMessage(ChatColor.DARK_RED + "You have cursed " + target.getName() + "!");
        target.sendMessage(ChatColor.DARK_PURPLE + "You have been cursed by " + user.getName() + "...");
        applyCurse(target);
    }
}
