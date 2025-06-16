package me.kwilver.questPlugin.glyphs;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class SizeSync extends Glyph {

    public SizeSync(Player player) {
        super(player);
    }

    void grow(Player player, double targetSize, int speedInSeconds) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 0.5f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 7 * 20, 0, false, false, false));
        new BukkitRunnable() {
            final double ticks = speedInSeconds * 20.0;
            final double startSize = player.getAttribute(Attribute.SCALE).getBaseValue();
            final double interval = (targetSize - startSize) / ticks;
            double currentTick = 0;

            @Override
            public void run() {
                currentTick++;

                double newSize = player.getAttribute(Attribute.SCALE).getBaseValue() + interval;
                player.getAttribute(Attribute.SCALE).setBaseValue(newSize);

                if (currentTick >= ticks || (interval > 0 && newSize >= targetSize) || (interval < 0 && newSize <= targetSize)) {
                    player.getAttribute(Attribute.SCALE).setBaseValue(targetSize);

                    player.getWorld().spawnParticle(
                            Particle.FLASH,
                            player.getLocation(),
                            30,
                            0.3, 0.5, 0.3,
                            0.01
                    );

                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1.5f, 0.9f);
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHISELED_BOOKSHELF_INSERT, 1.0f, 1.1f);

                    cancel();
                }

                player.getWorld().spawnParticle(
                        Particle.WITCH,
                        player.getLocation().add(0, 1, 0),
                        10,
                        0.3, 0.5, 0.3,
                        0.01
                );
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @Override
    protected boolean useGlyph() {
        double originalValue = user.getAttribute(Attribute.SCALE).getBaseValue();

        if(user.isSneaking()) grow(user, 1.5, 3);
        else grow(user, 0.5, 3);

        new BukkitRunnable() {
            @Override
            public void run () {
                grow(user, originalValue, 3);
            }
        }.runTaskLater(plugin, 15 * 20);
        return true;
    }
}
