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

public class Debug extends LootTable {
    Random random = new Random();
    public Debug(Quest quest, QuestPlugin questPlugin) {
        super(quest, questPlugin);
    }

    @Override
    protected ArrayList<ItemStack> rollItems() {
        ArrayList<ItemStack> items = new ArrayList<>();
        List<Runnable> selections = new ArrayList<>();

        selections.add(() -> items.add(new ItemStack(Material.COBWEB,
                16 + random.nextInt(17))));
        selections.add(() -> items.add(new ItemStack(Material.GOLDEN_APPLE,
                16 + random.nextInt(17))));
        selections.add(() -> items.add(new ItemStack(Material.EXPERIENCE_BOTTLE,
                32 + random.nextInt(33))));
        selections.add(() -> items.add(new ItemStack(Material.BREEZE_ROD,
                8 + random.nextInt(5))));
        selections.add(() -> items.add(new ItemStack(Material.TOTEM_OF_UNDYING)));
        selections.add(() -> items.add(new ItemStack(Material.DIAMOND,
                16 + random.nextInt(17))));
        selections.add(() -> items.add(new ItemStack(Material.IRON_BLOCK,
                4 + random.nextInt(5))));
        selections.add(() -> items.add(new ItemStack(Material.GOLD_BLOCK,
                4 + random.nextInt(5))));
        selections.add(() -> items.add(new ItemStack(Material.EMERALD,
                16 + random.nextInt(17))));
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
        selections.add(() -> items.add(new ItemStack(Material.NETHERITE_SCRAP,
                2 + random.nextInt(2))));

        Collections.shuffle(selections);

        for (int i = 0; i < 3 && i < selections.size(); i++) {
            selections.get(i).run();
        }

        return items;
    }

    @Override
    protected boolean rollGlyph() {
        return true;
    }

    @Override
    protected boolean rollReroll() {
        return true;
    }

    private ItemStack randomBook() {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();

        List<Runnable> actions = List.of(
                () -> meta.addStoredEnchant(Enchantment.AQUA_AFFINITY, 3, true),
                () -> meta.addStoredEnchant(Enchantment.DEPTH_STRIDER, 3, true),
                () -> meta.addStoredEnchant(Enchantment.EFFICIENCY, 3, true),
                () -> meta.addStoredEnchant(Enchantment.FEATHER_FALLING, 3, true),
                () -> meta.addStoredEnchant(Enchantment.FORTUNE, 3, true),
                () -> meta.addStoredEnchant(Enchantment.LOOTING, 3, true),
                () -> meta.addStoredEnchant(Enchantment.POWER, 3, true),
                () -> meta.addStoredEnchant(Enchantment.PROTECTION, 3, true),
                () -> meta.addStoredEnchant(Enchantment.RESPIRATION, 3, true),
                () -> meta.addStoredEnchant(Enchantment.SHARPNESS, 3, true),
                () -> meta.addStoredEnchant(Enchantment.SWEEPING_EDGE, 3, true),
                () -> meta.addStoredEnchant(Enchantment.UNBREAKING, 3, true)
        );

        Random random = new Random();
        actions.get(random.nextInt(actions.size())).run();

        item.setItemMeta(meta);
        return item;
    }

}
