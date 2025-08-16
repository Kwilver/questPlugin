package me.kwilver.questPlugin.glyphs;

import me.kwilver.questPlugin.QuestPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

import static me.kwilver.questPlugin.QuestPlugin.flashbackLocations;

public class Flashback extends Glyph {

    public Flashback(Player player) {
        super(player);
    }

    @Override
    protected boolean useGlyph() {
        List<Location> locations = flashbackLocations(user.getUniqueId());
        if (locations == null) {
            user.sendMessage(ChatColor.RED + "You have be online for " + ChatColor.BOLD + "at least 5 seconds " + ChatColor.RESET + ChatColor.RED + "to use this ability!");
            return false;
        }

        Location oldLoc = user.getLocation().clone();
        oldLoc.add(0, 1, 0);
        user.getWorld().spawnParticle(Particle.DUST, oldLoc, 20, 0.5, 0.5, 0.5, 0, new DustOptions(Color.fromRGB(200, 200, 200), 1f));
        user.getWorld().playSound(oldLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

        user.teleport(locations.get(locations.size() - 1));

        Location newLoc = user.getLocation().clone().add(0, 1, 0);
        user.getWorld().spawnParticle(Particle.PORTAL, newLoc, 20, 1, 1, 1, 0.1);
        user.getWorld().playSound(newLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);

        return true;
    }
}
