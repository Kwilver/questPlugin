package me.kwilver.questPlugin.commands;

import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.glyphs.GlyphTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class GlyphLoadoutCommand implements CommandExecutor, Listener {
    private static final int COLUMNS = 9;
    private static final int ROWS = 5;
    private static final int SIZE = COLUMNS * ROWS;
    private static final int ITEMS_PER_PAGE = 6;
    private static final int PREV_BUTTON_SLOT = 36;
    private static final int NEXT_BUTTON_SLOT = 44;

    private static final Map<UUID, UUID> viewerTargets = new HashMap<>();
    private static final Map<UUID, Integer> playerPages = new HashMap<>();

    public GlyphLoadoutCommand() {
        Bukkit.getPluginManager().registerEvents(this, QuestPlugin.getInstance());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("Usage: /glyphloadout <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || !target.hasPlayedBefore()) {
            player.sendMessage("Player not found or has never joined.");
            return true;
        }

        viewerTargets.put(player.getUniqueId(), target.getUniqueId());
        openLoadoutMenu(player, target);
        return true;
    }

    private void openLoadoutMenu(Player viewer, OfflinePlayer target) {
        Inventory inv = Bukkit.createInventory(null, SIZE, Component.text("Editing " + target.getName() + "'s Glyphs"));

        List<GlyphTracker> equipped = QuestPlugin.equippedGlyphs.getOrDefault(target.getUniqueId(), new ArrayList<>());

        int[] centeredSlots = {21, 23};
        for (int i = 0; i < 2; i++) {
            ItemStack item;
            if (i < equipped.size() && equipped.get(i) != null) {
                item = equipped.get(i).getItem().clone();
            } else {
                item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta meta = item.getItemMeta();
                meta.displayName(Component.text("Empty Slot", NamedTextColor.GRAY));
                item.setItemMeta(meta);
            }
            inv.setItem(centeredSlots[i], item);
        }

        viewer.openInventory(inv);
    }

    private void openGlyphSelect(Player viewer, OfflinePlayer target, int slotIndex, int page) {
        int totalGlyphs = QuestPlugin.allGlyphs.size();
        int maxPage = (totalGlyphs - 1) / ITEMS_PER_PAGE;
        page = Math.max(0, Math.min(page, maxPage));
        playerPages.put(viewer.getUniqueId(), page);

        Inventory inv = Bukkit.createInventory(null, SIZE, Component.text("Select Glyph (" + (page + 1) + "/" + (maxPage + 1) + ")"));

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

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, totalGlyphs);
        for (int i = start; i < end; i++) {
            GlyphTracker glyph = QuestPlugin.allGlyphs.get(i);
            inv.setItem(fillable.get(i - start), glyph.getItem());
        }

        if (page > 0) addNavArrow(inv, PREV_BUTTON_SLOT, "Previous Page");
        if (page < maxPage) addNavArrow(inv, NEXT_BUTTON_SLOT, "Next Page");

        viewer.setMetadata("glyph_slot_index", new FixedMetadataValue(QuestPlugin.getInstance(), slotIndex));
        viewer.openInventory(inv);
    }

    private void addNavArrow(Inventory inv, int slot, String name) {
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta meta = arrow.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.YELLOW));
        arrow.setItemMeta(meta);
        inv.setItem(slot, arrow);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID viewerId = player.getUniqueId();
        if (!viewerTargets.containsKey(viewerId)) return;

        UUID targetId = viewerTargets.get(viewerId);
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);
        if (target == null) return;

        String title = event.getView().title().toString();

        if (title.contains("Editing") && event.getRawSlot() < SIZE) {
            int slot = event.getRawSlot();
            int index = (slot == 21) ? 0 : (slot == 23) ? 1 : -1;
            if (index != -1) openGlyphSelect(player, target, index, 0);

            event.setCancelled(true);
            return;
        }

        if (title.contains("Select Glyph")) {
            int raw = event.getRawSlot();
            int page = playerPages.getOrDefault(player.getUniqueId(), 0);

            if (raw == PREV_BUTTON_SLOT) {
                int slotIndex = player.getMetadata("glyph_slot_index").get(0).asInt();
                openGlyphSelect(player, target, slotIndex, page - 1);
                return;
            }
            if (raw == NEXT_BUTTON_SLOT) {
                int slotIndex = player.getMetadata("glyph_slot_index").get(0).asInt();
                openGlyphSelect(player, target, slotIndex, page + 1);
                return;
            }

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            int slotIndex = player.getMetadata("glyph_slot_index").get(0).asInt();
            GlyphTracker selected = QuestPlugin.allGlyphs.stream()
                    .filter(g -> g.getItem().isSimilar(clicked))
                    .findFirst()
                    .orElse(null);

            if (selected == null) return;

            List<GlyphTracker> loadout = QuestPlugin.equippedGlyphs.getOrDefault(targetId, new ArrayList<>());
            while (loadout.size() <= slotIndex) loadout.add(null);
            loadout.set(slotIndex, selected);
            QuestPlugin.equippedGlyphs.put(targetId, loadout);

            player.sendMessage(Component.text("Set slot " + (slotIndex + 1) + " to " + selected.displayName, NamedTextColor.GREEN));
            Bukkit.getScheduler().runTask(QuestPlugin.getInstance(), () -> openLoadoutMenu(player, target));
            event.setCancelled(true);
        }
    }
}