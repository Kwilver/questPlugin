package me.kwilver.questPlugin.glyphs;
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
        user.sendMessage("used " + displayName);
        if(cooldowns.containsKey(user.getUniqueId())) {
            if(System.currentTimeMillis() - cooldowns.get(user.getUniqueId()) < cooldown) {
                user.sendMessage("cooldown tim");
                return false;
            }
        }

        try {
            Glyph glyphInit = glyph.getDeclaredConstructor(Player.class).newInstance(user);

            boolean returnValue = glyphInit.useGlyph();

            if(returnValue) cooldowns.put(user.getUniqueId(), System.currentTimeMillis());

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
