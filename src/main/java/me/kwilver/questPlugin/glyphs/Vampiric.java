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

        BukkitRunnable swirl = new BukkitRunnable() {
            double angle = 0;
            final double radius = 1.2;
            final int particles = 12;
            final Player p = user;

            @Override
            public void run() {
                if (!p.isOnline()) {
                    cancel();
                    return;
                }

                Location loc = p.getLocation().add(0, 1.0, 0);

                for (int i = 0; i < particles; i++) {
                    double currentAngle = angle + (2 * Math.PI * i / particles);
                    double x = Math.cos(currentAngle) * radius;
                    double z = Math.sin(currentAngle) * radius;

                    Location particleLoc = loc.clone().add(x, 0, z);
                    particleLoc.getWorld().spawnParticle(Particle.ASH, particleLoc, 1, 0, 0, 0, 0.01);
                }

                angle += Math.PI / 16;
            }
        };

        swirl.runTaskTimer(plugin, 0, 2);

        new BukkitRunnable() {
            public void run () {
                HandlerList.unregisterAll(Vampiric.this);
                swirl.cancel();
            }
        }.runTaskLater(plugin, 10 * 20);

        return true;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager().equals(user) && e.getEntity() instanceof LivingEntity) {
            user.heal(e.getDamage() / 2);
            user.getWorld().playSound(user.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1, 1);

            Location victimLocation = ((LivingEntity) e.getEntity()).getEyeLocation();
            Location attackerLocation = user.getEyeLocation();

            Vector direction = victimLocation.toVector().subtract(attackerLocation.toVector());
            double distance = direction.length();
            Vector unit = direction.normalize();

            new BukkitRunnable() {
                double i = 0;
                @Override
                public void run() {
                    if (i > distance) {
                        cancel();
                        return;
                    }

                    Vector step = unit.clone().multiply(i);
                    Location loc = attackerLocation.clone().add(step);

                    loc.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 1, 0, 0, 0, 0);
                    loc.getWorld().spawnParticle(Particle.SOUL, loc, 1, 0, 0, 0, 0.01);
                    loc.getWorld().spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, new Particle.DustOptions(org.bukkit.Color.fromRGB(120, 0, 0), 1.5F));

                    i += 0.5;
                }
            }.runTaskTimer(plugin, 0, 1);

            new BukkitRunnable() {
                double t = 0;
                @Override
                public void run() {
                    if (t > Math.PI * 2) {
                        cancel();
                        return;
                    }

                    double radius = 0.5;
                    double x = radius * Math.cos(t);
                    double z = radius * Math.sin(t);
                    Location swirlLoc = user.getLocation().clone().add(x, 1, z);
                    swirlLoc.getWorld().spawnParticle(Particle.CRIMSON_SPORE, swirlLoc, 1, 0, 0, 0, 0.01);

                    t += Math.PI / 8;
                }
            }.runTaskTimer(plugin, 0, 1);
        }
    }

}
