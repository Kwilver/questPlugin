package me.kwilver.questPlugin.glyphs;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class Reaper extends Glyph implements Listener {

    public Reaper(Player player) {
        super(player);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    protected boolean useGlyph() {
        user.sendMessage(ChatColor.GOLD + "Hit a player to cut their potion effect in half!");

        new BukkitRunnable() {
            @Override
            public void run() {
                HandlerList.unregisterAll(Reaper.this);
            }
        }.runTaskLater(plugin, 30 * 20);

        return true;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player damager && damager.getUniqueId().equals(user.getUniqueId())) {
            if (e.getEntity() instanceof Player p) {
                p.sendMessage("cut");

                // Particle effect - stealing time
                Location from = p.getEyeLocation();
                Location to = damager.getEyeLocation();
                int steps = 10;
                for (int i = 0; i <= steps; i++) {
                    double t = i / (double) steps;
                    Location point = from.clone().add(to.clone().subtract(from).multiply(t));
                    p.getWorld().spawnParticle(Particle.SOUL, point, 2, 0.1, 0.1, 0.1, 0);
                }

                for (PotionEffect effect : new ArrayList<>(p.getActivePotionEffects())) {
                    PotionEffectType type = effect.getType();
                    int newDuration = effect.getDuration() / 2;
                    int amplifier = effect.getAmplifier();

                    p.removePotionEffect(type);
                    if (newDuration > 0) {
                        p.addPotionEffect(new PotionEffect(type, newDuration, amplifier, effect.isAmbient(), effect.hasParticles(), effect.hasIcon()));
                    }
                }

                HandlerList.unregisterAll(Reaper.this);
            }
        }
    }
}