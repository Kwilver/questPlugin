package me.kwilver.questPlugin.glyphs;

import me.kwilver.questPlugin.QuestPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import static org.bukkit.Bukkit.getServer;

public class Vampiric extends Glyph implements Listener {
    public Vampiric(Player player) {
        super(player);
    }

    @Override
    protected boolean useGlyph() {
        getServer().getPluginManager().registerEvents(this, plugin);

        new BukkitRunnable() {
            public void run () {
                HandlerList.unregisterAll(Vampiric.this);
            }
        }.runTaskLater(plugin, 10 * 20);

        return true;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if(e.getDamager().equals(user) && e.getEntity() instanceof LivingEntity) {
            user.heal(e.getDamage() / 2);

            user.getWorld().playSound(user.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1, 1);

            Location victimLocation = e.getEntity().getLocation().clone().add(0, ((LivingEntity) e.getEntity()).getEyeHeight(), 0);
            Location attackerLocation = user.getLocation().clone().add(0, ((LivingEntity) user).getEyeHeight(), 0);

            Vector endVector = victimLocation.toVector();
            Vector startVector = attackerLocation.toVector();
            Vector vector = startVector.clone().subtract(endVector);

            double distance = vector.length();

            Vector unit = vector.normalize();

            new BukkitRunnable() {
                double i = 0;
                @Override
                public void run () {
                    if(i > distance) {
                        cancel();
                    }

                    Vector step = unit.clone().multiply(i);
                    Location location = e.getEntity().getLocation().clone().add(step);

                    location.getWorld().spawnParticle(
                            Particle.DAMAGE_INDICATOR,
                            location,                // loc
                            1,                    // count
                            0.0, 0.0, 0.0,        // offset
                            0.0                   // extra spe ed
                    );

                    i += 0.5;
                }
            }.runTaskTimer(plugin, 0, 1);
        }
    }
}
