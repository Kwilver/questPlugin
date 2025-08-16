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

public class Easy extends LootTable {
    Random random = new Random();
    public Easy(Quest quest, QuestPlugin questPlugin) {
        super(quest, questPlugin);
    }

    @Override
    protected ArrayList<ItemStack> rollItems() {
        ArrayList<ItemStack> items = new ArrayList<>();
        List<Runnable> selections = new ArrayList<>();

        selections.add(() -> items.add(new ItemStack(Material.COBWEB,
                16 + random.nextInt(17))));
        selections.add(() -> items.add(new ItemStack(Material.GOLDEN_APPLE,
                8 + random.nextInt(9))));
        selections.add(() -> items.add(new ItemStack(Material.EXPERIENCE_BOTTLE,
                16 + random.nextInt(17))));
        selections.add(() -> items.add(new ItemStack(Material.BREEZE_ROD,
                1 + random.nextInt(2))));
        selections.add(() -> items.add(new ItemStack(Material.TOTEM_OF_UNDYING)));
        selections.add(() -> items.add(new ItemStack(Material.DIAMOND,
                1 + random.nextInt(16))));
        selections.add(() -> items.add(new ItemStack(Material.IRON_BLOCK,
                4 + random.nextInt(5))));
        selections.add(() -> items.add(new ItemStack(Material.GOLD_BLOCK,
                4 + random.nextInt(5))));
        selections.add(() -> items.add(new ItemStack(Material.EMERALD,
                8 + random.nextInt(9))));
        selections.add(() -> items.add(new ItemStack(Material.EMERALD_BLOCK,
                4 + random.nextInt(5))));
        selections.add(() -> items.add(randomBook()));
        selections.add(() -> {
            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            meta.addStoredEnchant(Enchantment.MENDING, 1, true);
            item.setItemMeta(meta);
            items.add(item);
        });

        Collections.shuffle(selections);

        for (int i = 0; i < 3 && i < selections.size(); i++) {
            selections.get(i).run();
        }

        return items;
    }

    @Override
    protected boolean rollGlyph() {
        return random.nextInt(4) == 0;
    }

    @Override
    protected boolean rollReroll() {
        return false;
    }

    private ItemStack randomBook() {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();

        List<Runnable> actions = List.of(
                () -> meta.addStoredEnchant(Enchantment.AQUA_AFFINITY, 1, true),
                () -> meta.addStoredEnchant(Enchantment.DEPTH_STRIDER, 1, true),
                () -> meta.addStoredEnchant(Enchantment.EFFICIENCY, 1, true),
                () -> meta.addStoredEnchant(Enchantment.FEATHER_FALLING, 1, true),
                () -> meta.addStoredEnchant(Enchantment.FORTUNE, 1, true),
                () -> meta.addStoredEnchant(Enchantment.LOOTING, 1, true),
                () -> meta.addStoredEnchant(Enchantment.POWER, 1, true),
                () -> meta.addStoredEnchant(Enchantment.PROTECTION, 1, true),
                () -> meta.addStoredEnchant(Enchantment.RESPIRATION, 1, true),
                () -> meta.addStoredEnchant(Enchantment.SHARPNESS, 1, true),
                () -> meta.addStoredEnchant(Enchantment.SWEEPING_EDGE, 1, true),
                () -> meta.addStoredEnchant(Enchantment.UNBREAKING, 1, true)
        );

        actions.get(random.nextInt(actions.size())).run();

        item.setItemMeta(meta);
        return item;
    }
}