package me.kwilver.questPlugin.glyphs;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GlyphTracker {
    //properties
    final Class<? extends Glyph> glyph;
    public final String displayName;
    public final List<String> lore;
    public final int customModelData;
    public final long cooldown;

    //storage
    Map<UUID, Long> cooldowns = new HashMap<>();

    public GlyphTracker(Class<? extends Glyph> glyph, String displayName, List<String> lore, int customModelData, int cooldown) {
        this.glyph = glyph;
        this.displayName = displayName;
        this.lore = lore;
        this.customModelData = customModelData;
        this.cooldown = (long) cooldown * 1000;
    }

    public boolean activate(Player user) {
        if (cooldowns.containsKey(user.getUniqueId())) {
            long timeLeft = cooldown - (System.currentTimeMillis() - cooldowns.get(user.getUniqueId()));
            if (timeLeft > 0) {
                long seconds = timeLeft / 1000 % 60;
                long minutes = timeLeft / (1000 * 60) % 60;
                long hours = timeLeft / (1000 * 60 * 60) % 24;
                long days = timeLeft / (1000 * 60 * 60 * 24);

                StringBuilder timeString = new StringBuilder();
                if (days > 0) timeString.append(days).append("d ");
                if (hours > 0) timeString.append(hours).append("h ");
                if (minutes > 0) timeString.append(minutes).append("m ");
                if (seconds > 0) timeString.append(seconds).append("s");

                user.sendMessage("Glyph is on cooldown for " + timeString.toString().trim());
                return false;
            }
        }

        try {
            Glyph glyphInit = glyph.getDeclaredConstructor(Player.class).newInstance(user);

            boolean returnValue = glyphInit.useGlyph();

            if(returnValue) {
                cooldowns.put(user.getUniqueId(), System.currentTimeMillis());
                user.sendMessage(ChatColor.GREEN + "Used " + displayName);
            }

            return returnValue;
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(Material.HEAVY_CORE);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(displayName);
        meta.setLore(lore);
        meta.setCustomModelData(customModelData);
        item.setItemMeta(meta);

        return item;
    }
}
