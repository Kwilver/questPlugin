package me.kwilver.questPlugin.glyphs;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
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
        List<Location> l = flashbackLocations(user.getUniqueId());
        if(l == null) {
            user.sendMessage(ChatColor.RED + "You have be online for " + ChatColor.BOLD + "at least 5 seconds " + ChatColor.RESET + ChatColor.RED + "to use this ability!");
            return false;
        }

        user.getWorld().playSound(user.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

        user.teleport(l.getLast());
        return true;
    }
}
