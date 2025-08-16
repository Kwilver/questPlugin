package me.kwilver.questPlugin.glyphs;

import me.kwilver.questPlugin.QuestPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Curse extends Glyph implements Listener {

    private static final long AWAIT_WINDOW_TICKS   = 30 * 20L;  // 30‑sec window to hit
    private static final long SPHERE_DURATION_TICKS = 7 * 20L;   // player freeze bubble
    private static final long TOTAL_CURSE_TICKS    = 40 * 20L;  // FULL curse lasts 40 s from impact
    private static final long SWIRL_DURATION_TICKS = TOTAL_CURSE_TICKS - SPHERE_DURATION_TICKS; // 33 s swirl

    private boolean awaitingHit = false;

    private static final Set<UUID>              cursed   = new HashSet<>();
    private static final Map<UUID, Double>      oldMaxHp = new HashMap<>();

    private static final DustOptions[] SWIRL_COLORS = {
            new DustOptions(Color.fromRGB(255, 50, 50), 1f),
            new DustOptions(Color.fromRGB(255, 120, 30), 1f),
            new DustOptions(Color.fromRGB(200, 30, 0), 1f),
            new DustOptions(Color.fromRGB(255, 180, 50), 1f)
    };

    public Curse(Player player) {
        super(player);
        QuestPlugin.getInstance().getServer().getPluginManager().registerEvents(this, QuestPlugin.getInstance());
    }

    @Override
    protected boolean useGlyph() {
        awaitingHit = true;
        user.sendMessage(ChatColor.DARK_RED + "You have 30 seconds to curse a player. Strike now!");
        new BukkitRunnable() {
            @Override public void run() { awaitingHit = false; }
        }.runTaskLater(QuestPlugin.getInstance(), AWAIT_WINDOW_TICKS);
        return true;
    }

    public static void applyCurse(Player target) {
        UUID id = target.getUniqueId();
        if (cursed.contains(id)) return;
        cursed.add(id);

        oldMaxHp.put(id, target.getMaxHealth());

        Location center = target.getLocation().add(0, 1, 0);
        World world = target.getWorld();

        new BukkitRunnable() {
            long tick = 0;
            @Override public void run() {
                if (tick >= SPHERE_DURATION_TICKS || !target.isOnline() || !cursed.contains(id)) { cancel(); return; }
                target.teleport(center.clone().subtract(0, 1, 0));
                target.setVelocity(new Vector());
                drawParticleSphere(world, center, 3.5, 24);
                tick++;
            }
        }.runTaskTimer(QuestPlugin.getInstance(), 0L, 1L);

        new BukkitRunnable() {
            long tick = 0;
            final double CURSE_MAX_HP = 14.0;
            @Override public void run() {
                if (tick >= SWIRL_DURATION_TICKS || !target.isOnline() || !cursed.contains(id)) {
                    Double old = oldMaxHp.remove(id);
                    if (old != null) target.setMaxHealth(old);
                    cursed.remove(id);
                    cancel();
                    return;
                }
                for (int i = 0; i < 16; i++) {
                    double angle = tick * 0.3 + i * (2 * Math.PI / 16);
                    double x = Math.cos(angle) * 2;
                    double z = Math.sin(angle) * 2;
                    double y = 1 + Math.sin(tick * 0.2 + i) * 0.5;
                    Location loc = target.getLocation().clone().add(x, y, z);
                    world.spawnParticle(Particle.DUST, loc, 1, SWIRL_COLORS[i % SWIRL_COLORS.length]);
                }
                target.setMaxHealth(CURSE_MAX_HP);
                if (target.getHealth() > CURSE_MAX_HP) target.setHealth(CURSE_MAX_HP);
                tick++;
            }
        }.runTaskTimer(QuestPlugin.getInstance(), SPHERE_DURATION_TICKS, 1L);
    }

    private static void drawParticleSphere(World world, Location center, double radius, int count) {
        double inc = Math.PI * (3 - Math.sqrt(5));
        for (int i = 0; i < count; i++) {
            double y = 1 - (2.0 * i) / count;
            double r = Math.sqrt(1 - y * y);
            double theta = i * inc;
            double x = Math.cos(theta) * r;
            double z = Math.sin(theta) * r;
            Location loc = center.clone().add(x * radius, y * radius, z * radius);
            world.spawnParticle(Particle.DUST, loc, 1, new DustOptions(Color.fromRGB(139,0,0), 1));
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        if (!awaitingHit) return;
        if (e.getDamager() instanceof Player damager && e.getEntity() instanceof Player target && damager.getUniqueId().equals(user.getUniqueId())) {
            awaitingHit = false;
            user.sendMessage(ChatColor.DARK_RED + "You have cursed " + target.getName() + "!");
            target.sendMessage(ChatColor.DARK_PURPLE + "You have been cursed by " + user.getName() + "...");
            applyCurse(target);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if(cursed.contains(e.getPlayer().getUniqueId())) {
            e.getPlayer().setMaxHealth(20);
        }
    }
}
