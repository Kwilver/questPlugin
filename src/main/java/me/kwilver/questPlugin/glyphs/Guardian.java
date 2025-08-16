package me.kwilver.questPlugin.glyphs;

import me.kwilver.questPlugin.QuestPlugin;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class Guardian extends Glyph implements Listener {
    public Guardian(Player player) {
        super(player);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity().equals(user) && e.getCause() != EntityDamageEvent.DamageCause.VOID) {
            e.setCancelled(true);
            float min = 0.8f, max = 1.2f;
            float pitch = ThreadLocalRandom.current().nextFloat() * (max - min) + min;
            user.getWorld().playSound(user.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3f, pitch);
            Location loc = user.getLocation().add(0, 1, 0);
            user.getWorld().spawnParticle(Particle.WITCH, loc, 10, 0.5, 1.0, 0.5, 0.1);

            if (e instanceof EntityDamageByEntityEvent ev && ev.getDamager() instanceof LivingEntity attacker) {
                Vector dir = attacker.getLocation().clone().add(0, attacker.getEyeHeight(), 0)
                        .toVector().subtract(user.getLocation().toVector()).normalize();
                attacker.setVelocity(dir.multiply(0.4).setY(0.4));
                attacker.getWorld().spawnParticle(Particle.CRIT, attacker.getLocation(), 5, 0, 0, 0, 0.1);
            }
        }
    }

    @Override
    protected boolean useGlyph() {
        QuestPlugin.getInstance().getServer().getPluginManager()
                .registerEvents(this, QuestPlugin.getInstance());

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 8 * 20) {
                    HandlerList.unregisterAll(Guardian.this);
                    cancel();
                    return;
                }
                Location loc = user.getLocation().add(0, 1, 0);
                user.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 8, 0.3, 0.5, 0.3, 0.05);
            }
        }.runTaskTimer(QuestPlugin.getInstance(), 0L, 4L);

        return true;
    }
}