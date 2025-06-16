package me.kwilver.questPlugin.glyphs;

import me.kwilver.questPlugin.QuestPlugin;
import org.bukkit.Location;
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

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class Guardian extends Glyph implements Listener {
    public Guardian(Player player) {
        super(player);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if(e.getEntity().equals(user) && e.getCause() != EntityDamageEvent.DamageCause.VOID) {
            e.setCancelled(true);
            float minPitch = 0.8f;
            float maxPitch = 1.2f;

            float randomPitch = ThreadLocalRandom.current().nextFloat() * (maxPitch - minPitch) + minPitch;

            user.getWorld().playSound(
                    user.getLocation(),
                    Sound.BLOCK_ANVIL_LAND,
                    0.3f,
                    randomPitch
            );

            if(e instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;

                if(event.getDamager() instanceof LivingEntity attacker) {
                    Location userLoc = user.getLocation();
                    Location attackerLoc = attacker.getLocation().clone().add(0, attacker.getEyeHeight(), 0);

                    Vector userVector = userLoc.toVector();
                    Vector attackerVector = attackerLoc.toVector();

                    Vector knockbackDirection = attackerVector.subtract(userVector).normalize();
                    attacker.setVelocity(knockbackDirection.multiply(0.4).setY(0.4));
                }
            }
        }
    }

    @Override
    protected boolean useGlyph() {
        QuestPlugin.getInstance().getServer().getPluginManager().registerEvents(this, QuestPlugin.getInstance());

        new BukkitRunnable() {
            @Override
            public void run() {
                HandlerList.unregisterAll(Guardian.this);
            }
        }.runTaskLater(QuestPlugin.getInstance(), 8 * 20);
        return true;
    }
}
