package me.kwilver.questPlugin.glyphs;

import me.kwilver.questPlugin.QuestPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Geo extends Glyph {
    Location startLoc;
    Location endLoc;
    List<Material> blocks = new ArrayList<>();

    public Geo(Player player) {
        super(player);
    }

    public static List<Location> getLocationsBetween(Location start, Location end) {
        List<Location> locations = new ArrayList<>();

        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();

        for (double i = 0; i <= length; i += 0.5) {
            Vector current = start.toVector().add(direction.clone().multiply(i));
            Location loc = current.toLocation(start.getWorld());

            // Optionally round to block centers
            loc.setX(loc.getBlockX() + 0.5);
            loc.setY(loc.getBlockY() + 0.5);
            loc.setZ(loc.getBlockZ() + 0.5);

            if (!locations.contains(loc)) {
                locations.add(loc);
            }
        }

        return locations;
    }

    public void drawSphere(Location center, int radius) {
        int index = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z <= radius * radius) {
                        Location loc = center.clone().add(x, y, z);
                        Block block = loc.getBlock();

                        Random random = new Random();
                        int i = random.nextInt(5);

                        if(blocks.size() == index) {
                            if(i == 0) blocks.add(Material.COBBLESTONE);
                            if(i == 1) blocks.add(Material.GRANITE);
                            if(i == 2) blocks.add(Material.STONE);
                            if(i == 3) blocks.add(Material.DIORITE);
                            if(i == 4) blocks.add(Material.NETHERRACK);
                        }

                        block.setType(blocks.get(index));

                        index++;

                        loc.getWorld().spawnParticle(Particle.CRIMSON_SPORE, loc, 10, null);
                    }
                }
            }
        }
    }


    public void eraseSphere(Location center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z <= radius * radius) {
                        Location loc = center.clone().add(x, y, z);
                        Block block = loc.getBlock();

                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }

    void dealDamage(Location l) {
        l.getWorld().createExplosion(l, 25);

        for (Player p : Bukkit.getOnlinePlayers()) {
            double distance = p.getLocation().distance(l);
            if (distance < 15) {
                double rawDamage = 30 - distance * 2;

                p.damage(rawDamage);

                for (ItemStack item : p.getInventory().getArmorContents()) {
                    if (item == null) continue;
                    if (item.getType().getMaxDurability() <= 0) continue;

                    ItemMeta meta = item.getItemMeta();
                    if (!(meta instanceof Damageable dmgMeta)) continue;

                    int armorDamage = (int) (45 - distance * 3);

                    int current = dmgMeta.getDamage();
                    int max    = item.getType().getMaxDurability();
                    int updated = current + armorDamage;
                    updated = Math.max(0, Math.min(updated, max));

                    dmgMeta.setDamage(updated);
                    item.setItemMeta(dmgMeta);
                }
                Bukkit.getScheduler().runTask(plugin, p::updateInventory);
            }
        }
    }

    protected boolean useGlyph() {
        user.getLineOfSight(null, 20);
        for(Block block : user.getLineOfSight(null, 20)) {
            endLoc = block.getLocation();

            if(block.getType() != Material.AIR) {
                break;
            }
        }

        if(endLoc.getBlock().getType() == Material.AIR) {
            Location checkLoc = endLoc.clone();

            while (checkLoc.getY() > -60) {
                checkLoc.subtract(0, 1, 0);
                Block block = checkLoc.getBlock();
                if (block.getType().isSolid()) {
                    endLoc = block.getLocation();
                    break;
                }
            }
        }

        startLoc = user.getLocation().clone().add(0, 10, 0);

        new GeoStepRunnable(null, 1, getLocationsBetween(startLoc, endLoc), 10).runTaskLater(plugin, 0);
        return true;
    }


    private class GeoStepRunnable extends BukkitRunnable {
        private final int currentIndex;
        private final List<Location> list;
        private final int tickDelay;

        public GeoStepRunnable(BukkitRunnable self, int currentIndex, List<Location> list, int tickDelay) {
            this.currentIndex = currentIndex;
            this.list = list;
            this.tickDelay = tickDelay;
        }

        @Override
        public void run() {
            if (currentIndex >= list.size()) return;

            eraseSphere(list.get(currentIndex - 1), 5);
            drawSphere(list.get(currentIndex), 5);

            if (currentIndex == list.size() - 1) {
                dealDamage(list.get(currentIndex));
                eraseSphere(list.get(currentIndex), 5);
                return;
            }

            new GeoStepRunnable(null, currentIndex + 1, list, 1).runTaskLater(plugin, 1);
        }
    }


}
