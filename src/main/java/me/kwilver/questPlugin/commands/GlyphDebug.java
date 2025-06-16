package me.kwilver.questPlugin.commands;

import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.glyphs.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class GlyphDebug implements CommandExecutor, Listener {
    QuestPlugin main;
    private final String GUI_TITLE = ChatColor.DARK_PURPLE + "Select a Glyph to use!";

    public GlyphDebug(QuestPlugin main) {
        Bukkit.getServer().getPluginManager().registerEvents(this, QuestPlugin.getInstance());
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        Player selectedPlayer = (Player) commandSender;

        int size = 36;
        Inventory gui = Bukkit.createInventory(null, size, GUI_TITLE);

        int index = 0;
        for(Map.Entry<String, GlyphTracker> entry : main.stringToGlyph.entrySet()) {
            gui.setItem(index++, entry.getValue().getItem());
        }

        selectedPlayer.openInventory(gui);

        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        for(Map.Entry<String, GlyphTracker> entry : main.stringToGlyph.entrySet()) {
            if(entry.getValue().customModelData == clickedItem.getItemMeta().getCustomModelData()) {
                entry.getValue().activate(player);
            }
        }
    }
}
