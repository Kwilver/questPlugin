package me.kwilver.questPlugin.glyphs;

import com.comphenix.protocol.wrappers.EnumWrappers;
import me.kwilver.questPlugin.QuestPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Disabler extends Glyph {

    public Disabler(Player player) {
        super(player);
    }

    @Override
    protected boolean useGlyph() {
        Location center = user.getLocation();
        QuestPlugin.disabler(center, user.getUniqueId());
        user.sendMessage(ChatColor.GOLD + "Disabled all glyphs within a 100*100 range of your location!");
        user.getWorld().playSound(user.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1, 1);

        int radius = 15;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Location loc = center.clone().add(x, 0, z);
                if (loc.distanceSquared(center) <= radius * radius) {
                    for (int y = 0; y <= 6; y += 2) {
                        Location particleLoc = loc.clone().add(0.5, y, 0.5);
                        center.getWorld().spawnParticle(Particle.CRIT, particleLoc, 10, 0.25, 0.25, 0.25, 0);
                    }
                }
            }
        }

        return true;
    }
}
