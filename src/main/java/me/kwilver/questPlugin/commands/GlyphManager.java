package me.kwilver.questPlugin.commands;

import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.glyphs.GlyphTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GlyphManager implements CommandExecutor, Listener {
    private static final int COLUMNS = 9;
    private static final int ROWS = 5;
    private static final int SIZE = COLUMNS * ROWS;
    private static final int ITEMS_PER_PAGE = 6;
    private static final int PREV_BUTTON_SLOT = 36;
    private static final int NEXT_BUTTON_SLOT = 44;

    // Track pages per player
    private static final Map<UUID, Integer> playerPages = new HashMap<>();

    public GlyphManager() {
        Bukkit.getServer().getPluginManager().registerEvents(this, QuestPlugin.getInstance());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        openGlyphMenu(player, 0);
        return true;
    }

    private static void openGlyphMenu(Player player, int page) {
        int totalGlyphs = QuestPlugin.allGlyphs.size();
        int maxPage = (totalGlyphs - 1) / ITEMS_PER_PAGE;
        page = Math.max(0, Math.min(page, maxPage));
        playerPages.put(player.getUniqueId(), page);

        Inventory inv = Bukkit.createInventory(null, SIZE, Component.text("Glyph Manager (" + (page + 1) + "/" + (maxPage + 1) + ")"));

        // build fillable slots
        List<Integer> fillable = new ArrayList<>();
        for (int slot = 0; slot < SIZE; slot++) {
            int row = slot / COLUMNS;
            int col = slot % COLUMNS;
            if (row == 0 || row == ROWS - 1) continue;
            if (col == 0 || col == COLUMNS - 1) continue;
            if (((row - 1) % 2) != 0) continue;
            if (((col - 1) % 2) != 0) continue;
            fillable.add(slot);
        }

        // place glyph slice
        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, totalGlyphs);
        for (int i = start; i < end; i++) {
            GlyphTracker glyph = QuestPlugin.allGlyphs.get(i);
            ItemStack item = glyph.getItem().clone();
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            boolean enabled = QuestPlugin.enabledGlyphs.contains(glyph);
            lore.add(Component.empty());
            lore.add(Component.text(enabled ? "Enabled" : "Disabled",
                    enabled ? NamedTextColor.GREEN : NamedTextColor.RED));
            meta.lore(lore);
            item.setItemMeta(meta);
            inv.setItem(fillable.get(i - start), item);
        }

        // navigation arrows
        if (page > 0) addNavArrow(inv, PREV_BUTTON_SLOT, "Previous Page");
        if (page < maxPage) addNavArrow(inv, NEXT_BUTTON_SLOT, "Next Page");

        player.openInventory(inv);
    }

    private static void addNavArrow(Inventory inv, int slot, String name) {
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta meta = arrow.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.YELLOW));
        arrow.setItemMeta(meta);
        inv.setItem(slot, arrow);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        // exact title match to detect our menu
        if (!event.getView().title().toString().contains("Glyph Manager")) return;

        // cancel all clicks in our GUI
        event.setCancelled(true);

        int raw = event.getRawSlot();
        UUID uuid = player.getUniqueId();
        int page = playerPages.getOrDefault(uuid, 0);
        int totalGlyphs = QuestPlugin.allGlyphs.size();
        int maxPage = (totalGlyphs - 1) / ITEMS_PER_PAGE;

        // navigation
        if (raw == PREV_BUTTON_SLOT && page > 0) {
            openGlyphMenu(player, page - 1);
            return;
        }
        if (raw == NEXT_BUTTON_SLOT && page < maxPage) {
            openGlyphMenu(player, page + 1);
            return;
        }

        List<Integer> fillable = new ArrayList<>();
        for (int slot = 0; slot < SIZE; slot++) {
            int row = slot / COLUMNS;
            int col = slot % COLUMNS;
            if (row == 0 || row == ROWS - 1) continue;
            if (col == 0 || col == COLUMNS - 1) continue;
            if (((row - 1) % 2) != 0) continue;
            if (((col - 1) % 2) != 0) continue;
            fillable.add(slot);
        }

        int idx = fillable.indexOf(raw);
        if (idx < 0 || idx >= ITEMS_PER_PAGE) return;
        int glyphIndex = page * ITEMS_PER_PAGE + idx;
        if (glyphIndex >= totalGlyphs) return;

        GlyphTracker glyph = QuestPlugin.allGlyphs.get(glyphIndex);
        if (QuestPlugin.enabledGlyphs.contains(glyph)) {
            QuestPlugin.enabledGlyphs.remove(glyph);
            player.sendMessage(Component.text("Disabled " + glyph.displayName, NamedTextColor.RED));
        } else {
            QuestPlugin.enabledGlyphs.add(glyph);
            player.sendMessage(Component.text("Enabled " + glyph.displayName, NamedTextColor.GREEN));
        }

        Bukkit.getScheduler().runTask(QuestPlugin.getInstance(), () -> openGlyphMenu(player, page));
    }
}
