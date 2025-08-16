package me.kwilver.questPlugin.lootTables;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Hard extends LootTable {
    Random random = new Random();
    public Hard(Quest quest, QuestPlugin questPlugin) {
        super(quest, questPlugin);
    }

    @Override
    protected ArrayList<ItemStack> rollItems() {
        ArrayList<ItemStack> items = new ArrayList<>();
        List<Runnable> selections = new ArrayList<>();

        selections.add(() -> items.add(new ItemStack(Material.COBWEB,
                16 + random.nextInt(49))));
        selections.add(() -> items.add(new ItemStack(Material.GOLDEN_APPLE,
                16 + random.nextInt(49))));
        selections.add(() -> items.add(new ItemStack(Material.EXPERIENCE_BOTTLE,
                64 + random.nextInt(1))));
        selections.add(() -> items.add(new ItemStack(Material.BREEZE_ROD,
                4 + random.nextInt(5))));
        selections.add(() -> items.add(new ItemStack(Material.TOTEM_OF_UNDYING,
                1 + random.nextInt(2))));
        selections.add(() -> items.add(new ItemStack(Material.DIAMOND,
                32 + random.nextInt(33))));
        selections.add(() -> items.add(new ItemStack(Material.IRON_BLOCK,
                8 + random.nextInt(9))));
        selections.add(() -> items.add(new ItemStack(Material.GOLD_BLOCK,
                8 + random.nextInt(9))));
        selections.add(() -> items.add(new ItemStack(Material.EMERALD,
                32 + random.nextInt(33))));
        selections.add(() -> items.add(new ItemStack(Material.EMERALD_BLOCK,
                8 + random.nextInt(9))));
        selections.add(() -> items.add(randomBook()));
        selections.add(() -> items.add(new ItemStack(Material.TRIDENT)));
        selections.add(() -> {
            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            meta.addStoredEnchant(Enchantment.MENDING, 1, true);
            item.setItemMeta(meta);
            items.add(item);
        });
        selections.add(() -> items.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE,
                1 + random.nextInt(2))));
        selections.add(() -> items.add(new ItemStack(Material.BEACON)));
        selections.add(() -> items.add(new ItemStack(Material.NETHERITE_SCRAP,
                3 + random.nextInt(2))));
        selections.add(() -> items.add(new ItemStack(Material.NETHERITE_INGOT,
                1 + random.nextInt(2))));

        Collections.shuffle(selections);

        for (int i = 0; i < 5 && i < selections.size(); i++) {
            selections.get(i).run();
        }

        return items;
    }

    @Override
    protected boolean rollGlyph() {
        return random.nextInt(4) != 0;
    }

    @Override
    protected boolean rollReroll() {
        return random.nextInt(10) == 0;
    }

    private ItemStack randomBook() {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();

        List<Enchantment> enchantments = List.of(
                Enchantment.AQUA_AFFINITY,
                Enchantment.DEPTH_STRIDER,
                Enchantment.EFFICIENCY,
                Enchantment.FEATHER_FALLING,
                Enchantment.FORTUNE,
                Enchantment.LOOTING,
                Enchantment.POWER,
                Enchantment.PROTECTION,
                Enchantment.RESPIRATION,
                Enchantment.SHARPNESS,
                Enchantment.SWEEPING_EDGE,
                Enchantment.UNBREAKING
        );

        Enchantment chosen = enchantments.get(random.nextInt(enchantments.size()));
        int finalLevel = Math.min(4, chosen.getMaxLevel());
        meta.addStoredEnchant(chosen, finalLevel, true);

        item.setItemMeta(meta);
        return item;
    }
}
