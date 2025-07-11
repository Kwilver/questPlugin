package me.kwilver.questPlugin.lootTables;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.glyphs.GlyphTracker;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public abstract class LootTable implements Listener {
    Quest quest;
    QuestPlugin main;
    Player player;
    ArrayList<ItemStack> items = new ArrayList<>();
    GlyphTracker glyph;
    boolean reroll = false;


    public LootTable(Quest quest, QuestPlugin questPlugin) {
        this.quest = quest;
        main = questPlugin;
        player = quest.player.getPlayer();

        QuestPlugin.getInstance().getServer().getPluginManager().registerEvents(this, QuestPlugin.getInstance());
    }

    public void questCompletion() {
        if(player.isOnline()) {
            items = rollItems();
            glyph = rollGlyph() ? randomGlyph(QuestPlugin.equippedGlyphs.get(player.getUniqueId())) : null;
            reroll = rollReroll();

            Bukkit.getLogger().info("The quest was completed. Roll: " + glyph + ", " + reroll);

            rewardOrReroll();
        }
    }

    public void rewardOrReroll() {
        if(reroll) {
            ItemStack rerollItem = new ItemStack(Material.ORANGE_CONCRETE);
            ItemMeta rerollMeta = rerollItem.getItemMeta();
            rerollMeta.setDisplayName(ChatColor.RED + "Click To Reroll Rewards!");
            rerollItem.setItemMeta(rerollMeta);

            ItemStack keepItem = new ItemStack(Material.GREEN_CONCRETE);
            ItemMeta keepMeta = keepItem.getItemMeta();
            keepMeta.setDisplayName(ChatColor.RED + "Click To Claim Rewards!");
            keepItem.setItemMeta(keepMeta);

            Inventory gui = Bukkit.createInventory(null, 54, ChatColor.RED + "Reroll Option");
            gui.setItem(54 - 9, rerollItem);
            gui.setItem(53, keepItem);

            for(ItemStack item : items) {
                gui.addItem(item);
            }

            if(glyph != null) {
                gui.addItem(glyph.getItem());
            }

            player.openInventory(gui);

            return;
        }
        reward();
    }

    private void reward() {
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
        Bukkit.getLogger().info("Rewarding the player!");

        new BukkitRunnable() {
            public void run() {
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                for(ItemStack item : items) {
                    player.getWorld().dropItem(player.getLocation(), item).setVelocity(getRandomDirection(0.5, -30));
                }

                if(glyph != null) {
                    main.equipGlyph(player, glyph);
                }
            }
        }.runTaskLater(QuestPlugin.getInstance(), 70);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if(ChatColor.stripColor(e.getView().getTitle()).equals("Reroll Option")) {
            if(e.getReason() != InventoryCloseEvent.Reason.PLUGIN) {
                e.getPlayer().openInventory(e.getInventory());
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(ChatColor.stripColor(e.getView().getTitle()).equals("Reroll Option")) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null) {
                if (e.getCurrentItem().getType() == Material.GREEN_CONCRETE) {
                    e.getInventory().close();

                    reroll = false;
                    reward();
                    return;
                }
                if (e.getCurrentItem().getType() == Material.ORANGE_CONCRETE) {
                    Bukkit.getLogger().info("Adding to UUID set");
                    e.getInventory().close();

                    questCompletion();
                }
            }
        }
    }

















    protected abstract ArrayList<ItemStack> rollItems();

    protected abstract boolean rollGlyph();

    protected abstract boolean rollReroll();

    protected GlyphTracker randomGlyph(List<GlyphTracker> equippedGlyphs) {
        List<GlyphTracker> candidates = QuestPlugin.enabledGlyphs.stream()
                .filter(g -> !equippedGlyphs.contains(g))
                .toList();

        if (candidates.isEmpty()) return null;

        return candidates.get(new Random().nextInt(candidates.size()));
    }

    public Vector getRandomDirection(double speed, double pitchDegrees) {
        Random random = new Random();

        double yaw = random.nextDouble() * 360;

        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitchDegrees);

        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);

        Vector direction = new Vector(x, y, z);
        return direction.normalize().multiply(speed);
    }
}
