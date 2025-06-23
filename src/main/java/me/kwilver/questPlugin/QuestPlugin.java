package me.kwilver.questPlugin;

import me.kwilver.questPlugin.commands.*;
import me.kwilver.questPlugin.glyphs.*;
import me.kwilver.questPlugin.lootTables.LootTable;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.bukkit.util.Vector;

import java.util.logging.Level;

public final class QuestPlugin extends JavaPlugin implements Listener {
    public Map<OfflinePlayer, Quest> activeQuests = new HashMap<>();
    private static Plugin instance;
    static Map<UUID, List<Location>> flashbackLocations = new HashMap<>();

    public Map<String, GlyphTracker> stringToGlyph = new HashMap<>();
    public Map<GlyphTracker, String> glyphToString = new HashMap<>();
    public List<GlyphTracker> glyphs = new ArrayList<>();

    public Map<UUID, List<GlyphTracker>> equippedGlyphs = new HashMap<>();
    public Map<UUID, GlyphTracker> pendingGlyphs = new HashMap<>();

    private File playerDataFile;
    private FileConfiguration playerDataConfig;
    public Map<UUID, Long> lastCompletions = new HashMap<>();

    public static Oracle oracle;

    @Override
    public void onEnable() {
        //stringToGlyph.put("Aeolus", new GlyphTracker(Aeolus.class,
        //        "Aeolus",
        //        List.of("Release a burst of wind that knocks back enemies.",
        //                ChatColor.YELLOW + "Cooldown: 3m"),
        //        5000,
        //        3 * 60));
        stringToGlyph.put("Dash", new GlyphTracker(Dash.class,
                "Dash",
                List.of("Gain a burst of energy allowing you to soar through the air.",
                        ChatColor.YELLOW + "Cooldown: 15s"),
                5001,
                15));
        stringToGlyph.put("Dripstone", new GlyphTracker(Dripstone.class,
                "Dripstone",
                List.of("Call upon sharp pillars from below the ground to stun your opponents.",
                        ChatColor.YELLOW + "Cooldown: 2m"),
                5002,
                2 * 60));
        //stringToGlyph.put("Flashback", new GlyphTracker(Flashback.class,
        //        "Flashback",
        //        List.of("Harness the power of time itself to \"flashback\" to a previous location.",
        //                ChatColor.YELLOW + "Cooldown: 10m"),
        //        5003,
        //        2 * 60));
        stringToGlyph.put("Geo", new GlyphTracker(Geo.class,
                "Geo",
                List.of("Summon a meteor from the heavens to rain terror upon the ground below.",
                        ChatColor.YELLOW + "Cooldown: 3m"),
                5004,
                3 * 60));
        stringToGlyph.put("GrandSlam", new GlyphTracker(GrandSlam.class,
                "Grand Slam",
                List.of("Leap up and slam down, blasting enemies with a shockwave.",
                        ChatColor.YELLOW + "Cooldown: 3m 30s"),
                5005,
                3 * 60 + 30));
        stringToGlyph.put("Guardian", new GlyphTracker(Guardian.class,
                "Guardian",
                List.of("Summon a shield of ash that absorbs any and all enemy fire.",
                        ChatColor.YELLOW + "Cooldown: 3m 30s"),
                5006,
                3 * 60 + 30));
        stringToGlyph.put("Mirror", new GlyphTracker(Mirror.class,
                "Mirror",
                List.of("Reflect any and all enemy fire.",
                        ChatColor.YELLOW + "Cooldown: 4m"),
                5007,
                4 * 60));
        stringToGlyph.put("SalmonCannon", new GlyphTracker(SalmonCannon.class,
                "Salmon Cannon",
                List.of("Release a trojan-salmon with deadly firepower.",
                        ChatColor.YELLOW + "Cooldown: 3m 30s"),
                5008,
                3 * 60 + 30));
        stringToGlyph.put("SizeSync", new GlyphTracker(SizeSync.class,
                "Size Sync",
                List.of("Shrink (or grow, when crouched) to an... unfamiliar size... this is opposite i gotta fix it",
                        ChatColor.YELLOW + "Cooldown: 2m"),
                5009,
                2 * 60));
        stringToGlyph.put("Vampiric", new GlyphTracker(Vampiric.class,
                "Vampiric",
                List.of("Instead of simply inflicting damage upon your adversary, take the health for yourself...",
                        ChatColor.YELLOW + "Cooldown: 3m"),
                50010,
                3 * 60));

        for(String key : stringToGlyph.keySet()) {
            glyphToString.put(stringToGlyph.get(key), key);
            glyphs.add(stringToGlyph.get(key));
        }

        instance = this;

        getCommand("debugQuest").setExecutor(new DebugQuest(this));
        getCommand("questInfo").setExecutor(new QuestInfo(this));
        getCommand("GlyphDebug").setExecutor(new GlyphDebug(this));
        getCommand("SpawnOracle").setExecutor(new SpawnOracle(this));
        getCommand("EquipGlyph").setExecutor(new EquipGlyph(this));
        getCommand("UseGlyph").setExecutor(new Ability(this));
        getCommand("Remove").setExecutor(new Remove(this));

        Bukkit.getPluginManager().registerEvents(this, this);

        // quest ticker
        new BukkitRunnable() {
            @Override
            public void run () {
                for(OfflinePlayer p : activeQuests.keySet()) {
                    Quest quest = activeQuests.get(p);

                    Player player = p.getPlayer();

                    if(player != null) {
                        if(quest instanceof TickingQuest) {
                            quest.tick(player); //TODO quest ticking might not be necessary, consider removing after doing all of the quest templates
                        }

                        String output = "Quest: " + quest.displayName() + ChatColor.WHITE + " | " + ChatColor.AQUA + formatTimeRemaining(quest.msRemaining()) + ChatColor.WHITE +  " | /questinfo for requirements";

                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(output));
                    }

                    if(!quest.questActive()) {
                        endQuest(p, false);
                    }
                }
            }
        }.runTaskTimer(this, 0, 20);

        new BukkitRunnable() {
            @Override
            public void run () {
                for(Player p : Bukkit.getOnlinePlayers()) {
                    List<Location> list = flashbackLocations.get(p.getUniqueId());
                    if(list == null) {
                        list = new ArrayList<>();
                    }
                    if(list.size() >= 60) {
                        list.remove(list.getLast());
                    }

                    list.add(p.getLocation());
                    flashbackLocations.put(p.getUniqueId(), list);
                }
            }
        }.runTaskTimer(this, 0, 5 * 20);

        playerDataFile = new File(getDataFolder(), "playerdata.yml");
        if (!playerDataFile.exists()) {
            playerDataFile.getParentFile().mkdirs();
            saveResource("playerdata.yml", false);
        }

        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);

        for(String string : playerDataConfig.getKeys(false)) {
            List<GlyphTracker> equipped = new ArrayList<>();
            for(String string1 : playerDataConfig.getStringList(string)) {
                equipped.add(stringToGlyph.get(string1));
            }

            equippedGlyphs.put(UUID.fromString(string), equipped);
        }
    }

    public static Oracle getOracle() {
        return oracle;
    }

    public void newQuest(Class<? extends Quest> questClass, Player player) {
        try {
            Constructor<? extends Quest> constructor = questClass.getConstructor(Player.class, QuestPlugin.class);

            Quest quest = constructor.newInstance(player, this);

            startSpiralEffect(player);
            startNetherParticles(player.getLocation());
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 30, 0));
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_CHARGE, 1, 1);

            new BukkitRunnable() {
                @Override
                public void run() {
                    activeQuests.put(player, quest);
                    player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1, 1);
                    player.sendTitle(ChatColor.RED + "A new quest begins...", quest.displayName() + ChatColor.WHITE + " | /questinfo");
                }
            }.runTaskLater(this, 30);
        } catch (RuntimeException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to instantiate quest " + questClass.getSimpleName(), e);//TODO Remove
        }
    }

    public void endQuest(OfflinePlayer p, boolean success) {
        LootTable questTable = activeQuests.get(p).getLootTable();
        activeQuests.get(p).cleanup();
        activeQuests.remove(p);

        if(p.isOnline()) {
            Player player = p.getPlayer();
            if(success) {
                player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1, 1);
                player.sendTitle(ChatColor.GREEN + "Quest Completed!", "The Oracle smiles upon your triumph.");

                questTable.questCompletion();
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_FALL, 1, 1);
                player.sendTitle(ChatColor.RED + "Quest Failed", "Even the Oracle cannot alter lost time.");
            }
        }

        lastCompletions.put(p.getUniqueId(), System.currentTimeMillis());
    }

    @Override
    public void onDisable() {
        for (UUID id : equippedGlyphs.keySet()) {
            List<String> glyphStrings = new ArrayList<>();
            for (GlyphTracker glyph : equippedGlyphs.get(id)) {
                glyphStrings.add(glyphToString.get(glyph));
            }
            playerDataConfig.set(id.toString(), glyphStrings); // Set full list at once
        }

        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Plugin pluginMain() {
        return Bukkit.getPluginManager().getPlugin("questPlugin");
    }

    public String formatTimeRemaining(long msRemaining) {
        long totalSeconds = msRemaining / 1000;

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.isEmpty()) sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    public void startSpiralEffect(Player player) {
        new BukkitRunnable() {
            final int duration = 30; // 3 seconds (20 ticks per second)
            int tick = 0;

            @Override
            public void run() {
                if (tick >= duration) {
                    cancel();
                    return;
                }

                double progress = (double) tick / duration * 3;
                double angle = progress * 2 * Math.PI * 2; // 2 full rotations
                double radius = 0.5;

                // Y position: up and down sinusoidal wave
                double height = Math.sin(progress * Math.PI) * 2; // up then down over 3 seconds

                // Spiral around player
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;

                Vector offset = new Vector(x, height, z);
                player.getWorld().spawnParticle(
                        Particle.FIREWORK,
                        player.getLocation().add(0, 1, 0).add(offset.getX(), offset.getY(), offset.getZ()),
                        5, 0, 0, 0, 0 // Spawn 5 particles at that spot
                );

                tick++;
            }
        }.runTaskTimer(this, 0, 1); // Run every tick
    }
    public void startNetherParticles(Location location) {
        new BukkitRunnable() {
            final int duration = 30;
            int tick = 0;

            @Override
            public void run() {
                if (tick >= duration) {
                    cancel();
                    return;
                }

                location.getWorld().spawnParticle(
                        Particle.CRIMSON_SPORE,
                        location,
                        20, 0, 0, 0, 0
                );

                tick++;
            }
        }.runTaskTimer(this, 0, 1); // Run every tick
    }
    public void startWarpedParticles(Location location) {
        new BukkitRunnable() {
            final int duration = 30;
            int tick = 0;

            @Override
            public void run() {
                if (tick >= duration) {
                    cancel();
                    return;
                }

                location.getWorld().spawnParticle(
                        Particle.WARPED_SPORE,
                        location,
                        20, 0, 0, 0, 0
                );

                tick++;
            }
        }.runTaskTimer(this, 0, 1); // Run every tick
    }

    public static final List<ItemStack> RARE_ITEMS = createRareItems();

    private static List<ItemStack> createRareItems() {
        List<ItemStack> items = new ArrayList<>();

        items.add(new ItemStack(Material.NETHER_STAR));
        items.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
        items.add(new ItemStack(Material.TOTEM_OF_UNDYING));
        items.add(new ItemStack(Material.ELYTRA));
        items.add(new ItemStack(Material.BEACON));
        items.add(new ItemStack(Material.MUSIC_DISC_5));
        items.add(new ItemStack(Material.MUSIC_DISC_PIGSTEP));
        items.add(new ItemStack(Material.HEART_OF_THE_SEA));
        items.add(new ItemStack(Material.NAUTILUS_SHELL));
        items.add(new ItemStack(Material.TRIDENT));
        items.add(new ItemStack(Material.ANCIENT_DEBRIS));
        items.add(new ItemStack(Material.NETHERITE_INGOT));
        items.add(new ItemStack(Material.END_CRYSTAL));
        items.add(new ItemStack(Material.WITHER_ROSE));
        items.add(new ItemStack(Material.SHULKER_SHELL));
        items.add(new ItemStack(Material.GHAST_TEAR));
        items.add(new ItemStack(Material.BLAZE_ROD));

        // Fully enchanted diamond gear
        items.add(makeEnchanted(Material.DIAMOND_HELMET));
        items.add(makeEnchanted(Material.DIAMOND_CHESTPLATE));
        items.add(makeEnchanted(Material.DIAMOND_LEGGINGS));
        items.add(makeEnchanted(Material.DIAMOND_BOOTS));
        items.add(makeEnchanted(Material.DIAMOND_SWORD));
        items.add(makeEnchanted(Material.DIAMOND_PICKAXE));
        items.add(makeEnchanted(Material.DIAMOND_AXE));
        items.add(makeEnchanted(Material.SHIELD));

        return items;
    }

    private static ItemStack makeEnchanted(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            switch (material) {
                case DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS -> {
                    meta.addEnchant(Enchantment.PROTECTION, 4, true);
                    meta.addEnchant(Enchantment.UNBREAKING, 3, true);
                    meta.addEnchant(Enchantment.MENDING, 1, true);
                    if (material == Material.DIAMOND_BOOTS) {
                        meta.addEnchant(Enchantment.DEPTH_STRIDER, 3, true);
                        meta.addEnchant(Enchantment.FEATHER_FALLING, 4, true);
                    }
                    if (material == Material.DIAMOND_HELMET) {
                        meta.addEnchant(Enchantment.RESPIRATION, 3, true);
                        meta.addEnchant(Enchantment.AQUA_AFFINITY, 1, true);
                    }
                }
                case DIAMOND_SWORD -> {
                    meta.addEnchant(Enchantment.SHARPNESS, 5, true);
                    meta.addEnchant(Enchantment.LOOTING, 3, true);
                    meta.addEnchant(Enchantment.UNBREAKING, 3, true);
                    meta.addEnchant(Enchantment.MENDING, 1, true);
                }
                case DIAMOND_PICKAXE -> {
                    meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
                    meta.addEnchant(Enchantment.FORTUNE, 3, true);
                    meta.addEnchant(Enchantment.UNBREAKING, 3, true);
                    meta.addEnchant(Enchantment.MENDING, 1, true);
                }
                case DIAMOND_AXE -> {
                    meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
                    meta.addEnchant(Enchantment.SHARPNESS, 5, true);
                    meta.addEnchant(Enchantment.UNBREAKING, 3, true);
                    meta.addEnchant(Enchantment.MENDING, 1, true);
                }
                case SHIELD -> {
                    meta.addEnchant(Enchantment.UNBREAKING, 3, true);
                    meta.addEnchant(Enchantment.MENDING, 1, true);
                }
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    private static final Random random = new Random();

    public static void dropRandomLoot(Location location) {
        List<ItemStack> lootPool = createRareItems();
        Collections.shuffle(lootPool);

        int amount = 3 + random.nextInt(3); // 3–5 items

        for (int i = 0; i < amount && i < lootPool.size(); i++) {
            ItemStack stack = lootPool.get(i).clone();
            Item dropped = location.getWorld().dropItemNaturally(location, stack);
            dropped.setPickupDelay(10);
        }

        location.getWorld().playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.8f);
        location.getWorld().spawnParticle(Particle.END_ROD, location.add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.01);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        double baseValue = e.getPlayer().getAttribute(Attribute.SCALE).getBaseValue();
        if(baseValue != 1.0) {
            e.getPlayer().sendMessage("Changing from " + baseValue + " to 1.0");
            e.getPlayer().getAttribute(Attribute.SCALE).setBaseValue(1.0);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        List<GlyphTracker> glyphs = equippedGlyphs.getOrDefault(id, new ArrayList<>());

        if(glyphs.isEmpty()) return;

        if(glyphs.size() == 1) glyphs.clear();

        if(glyphs.size() == 2) glyphs.remove(random.nextInt(1));

        equippedGlyphs.put(id, glyphs);
    }

    public static Plugin getInstance() {
        return instance;
    }

    public static List<Location> flashbackLocations(UUID id) {
        return flashbackLocations.get(id);
    }

    public void equipGlyph(Player player, GlyphTracker glyphTracker) {
        List<GlyphTracker> list = equippedGlyphs.getOrDefault(player.getUniqueId(), new ArrayList<>());

        if(list.size() < 2) {
            list.add(glyphTracker);
            player.sendMessage(ChatColor.GREEN + "Glyph equipped in slot " + list.size());

            equippedGlyphs.put(player.getUniqueId(), list);
            return;
        }

        pendingGlyphs.put(player.getUniqueId(), glyphTracker);

        Inventory gui = Bukkit.createInventory(null, 54, Component.text(ChatColor.DARK_PURPLE + "Select a glyph to trade out or discard the new glyph and receive loot!"));

        ItemStack instructions = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta instmeta = instructions.getItemMeta();
        instmeta.setDisplayName(ChatColor.YELLOW + "Instructions");
        instmeta.setLore(List.of("You have the opportunity to equip a new glyph.", "You can only have 2 glyphs equipped at once, so you can either", "replace a glyph you have currently with the new one, or", "trade the new one in for some extra loot."));
        instructions.setItemMeta(instmeta);

        ItemStack loot = new ItemStack(Material.CHEST);
        ItemMeta lootmeta = loot.getItemMeta();
        lootmeta.setDisplayName(ChatColor.GREEN + "Take loot instead!");
        loot.setItemMeta(lootmeta);

        gui.setItem(0, instructions);
        gui.setItem(13, glyphTracker.getItem());
        gui.setItem(29, equippedGlyphs.get(player.getUniqueId()).get(0).getItem());
        gui.setItem(33, equippedGlyphs.get(player.getUniqueId()).get(1).getItem());
        gui.setItem(49, loot);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getView().getTitle() == null || !ChatColor.stripColor(e.getView().getTitle()).equals("Select a glyph to trade out or discard the new glyph and receive loot!")) return;

        e.setCancelled(true);

        UUID uuid = player.getUniqueId();
        GlyphTracker newGlyph = pendingGlyphs.get(uuid);
        if (newGlyph == null) return;

        int slot = e.getRawSlot();

        if (slot == 49) {
            pendingGlyphs.remove(uuid);
            player.sendMessage(ChatColor.GOLD + "You chose to take loot instead of equipping the glyph.");
            dropRandomLoot(player.getLocation());

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            player.closeInventory();
            return;
        }

        int replaceIndex = -1;

        if (slot == 29) {
            replaceIndex = 0;
        } else if (slot == 33) {
            replaceIndex = 1;
        } else {
            return;
        }

        List<GlyphTracker> current = equippedGlyphs.get(uuid);
        if (current == null || current.size() < 2) return;

        GlyphTracker oldGlyph = current.get(replaceIndex);
        current.set(replaceIndex, newGlyph);

        equippedGlyphs.put(uuid, current);
        pendingGlyphs.remove(uuid);

        player.sendMessage(ChatColor.YELLOW + "Replaced " + ChatColor.RED + oldGlyph.displayName + ChatColor.YELLOW + " with " + ChatColor.GREEN + newGlyph.displayName);
        player.closeInventory();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if(e.getEntity() == oracle.oracle) {
            e.setCancelled(true);
        }
    }
}
