package me.kwilver.questPlugin.glyphs;

import me.kwilver.questPlugin.QuestPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Geo extends Glyph {
    private final Player user;
    private final Plugin plugin;

    public Geo(Player player) {
        super(player);
        this.user = player;
        this.plugin = QuestPlugin.getInstance();
    }

    @Override
    protected boolean useGlyph() {
        // Scan line of sight (20 blocks)
        List<Block> sight = user.getLineOfSight(null, 20);
        if (sight.isEmpty()) {
            user.sendMessage(ChatColor.RED + "No valid sight blocks.");
            return false;
        }
        Block lastSeen = sight.get(sight.size() - 1);
        Block targetBlock = null;
        for (Block b : sight) {
            if (b.getType() != Material.AIR) {
                targetBlock = b;
                break;
            }
        }
        // If only air was seen, drop straight down from the farthest block until solid
        if (targetBlock == null) {
            Location probe = lastSeen.getLocation().clone();
            while (probe.getY() > -60) {
                probe.subtract(0, 1, 0);
                if (probe.getBlock().getType().isSolid()) {
                    targetBlock = probe.getBlock();
                    break;
                }
            }
        }
        if (targetBlock == null) {
            user.sendMessage(ChatColor.RED + "Could not find ground block.");
            return false;
        }

        Location endLoc = targetBlock.getLocation().add(0.5, 0.5, 0.5);
        // Higher start (25 blocks above eyes)
        Location startLoc = user.getEyeLocation().clone().add(0, 25, 0);

        List<Location> path = getLocationsBetween(startLoc, endLoc);
        if (path.isEmpty()) {
            user.sendMessage(ChatColor.RED + "Could not calculate path.");
            return false;
        }

        World world = user.getWorld();
        AtomicInteger idx = new AtomicInteger(0);
        AtomicInteger taskId = new AtomicInteger();
        final BlockDisplay[] cube = new BlockDisplay[1];

        // Repeating task
        taskId.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            int i = idx.getAndIncrement();
            if (i >= path.size()) {
                if (cube[0] != null) cube[0].remove();
                Bukkit.getScheduler().cancelTask(taskId.get());
                return;
            }
            Location center = path.get(i).clone().add(0, 1, 0);
            if (cube[0] == null) {
                cube[0] = world.spawn(center, BlockDisplay.class, e -> {
                    e.setBlock(Material.MAGMA_BLOCK.createBlockData());
                    e.setPersistent(false);
                });
            } else cube[0].teleport(center);

            // Slightly smaller cube: scale 6
            float angle = i * 0.03f;
            cube[0].setTransformationMatrix(new Matrix4f()
                    .rotateXYZ(angle * 0.04f, angle * 0.02f, 0f)
                    .scale(6.0f));

            // Resize particle cloud to new cube size
            world.spawnParticle(Particle.FLAME, center, 30, 1.5, 1.5, 1.5, 0.03);
            world.spawnParticle(Particle.LAVA, center, 10, 0.9, 0.9, 0.9, 0);
            world.spawnParticle(Particle.SMOKE, center, 15, 1.5, 1.5, 1.5, 0.01);

            // Impact phase
            if (i == path.size() - 1) {
                triggerExplosions(center);
                cube[0].remove();
                Bukkit.getScheduler().cancelTask(taskId.get());
            }
        }, 0L, 1L));
        return true;
    }

    private List<Location> getLocationsBetween(Location start, Location end) {
        List<Location> locs = new ArrayList<>();
        Vector dir = end.toVector().subtract(start.toVector());
        double len = dir.length();
        if (len < 0.1) return locs;
        dir.normalize();
        for (double d = 0; d <= len; d += 0.5) {
            Vector cur = start.toVector().add(dir.clone().multiply(d));
            Location loc = cur.toLocation(start.getWorld());
            loc.setX(loc.getBlockX() + 0.5);
            loc.setY(loc.getBlockY() + 0.5);
            loc.setZ(loc.getBlockZ() + 0.5);
            locs.add(loc);
        }
        return locs;
    }

    private void triggerExplosions(Location center) {
        World w = center.getWorld();
        // Core blast (power 7) – breaks blocks
        w.createExplosion(center, 7, false, true);
        // Ring blasts (power 5)
        Vector[] offsets = {
                new Vector(3, 0, 0), new Vector(-3, 0, 0),
                new Vector(0, 0, 3), new Vector(0, 0, -3),
                new Vector(0, 3, 0)
        };
        for (Vector off : offsets) {
            w.createExplosion(center.clone().add(off), 5, false, true);
        }

        safe.add(center);
        new BukkitRunnable() {
            public void run() {
                safe.remove(center);
            }
        }.runTaskLater(plugin, 5 * 20);

        for (Player p : Bukkit.getOnlinePlayers()) {
            double dist = p.getLocation().distance(center);

            double maxHp = p.getAttribute(Attribute.MAX_HEALTH).getValue();
            double base  = 0.75 * maxHp;
            double dmg   = Math.max(0, base - (base/15) * dist);

            if (dmg > 0) {
                p.setHealth(Math.max(0, p.getHealth() - dmg));
                degradeArmor(p, dist);
            }
        }
    }

    private void degradeArmor(Player p, double dist) {
        for (ItemStack item : p.getInventory().getArmorContents()) {
            if (item == null || item.getType().getMaxDurability() <= 0) continue;
            ItemMeta meta = item.getItemMeta();
            if (!(meta instanceof Damageable dmg)) continue;
            int extra = (int) (35 - dist * 2.5);
            dmg.setDamage(Math.min(item.getType().getMaxDurability(), dmg.getDamage() + Math.max(0, extra)));
            item.setItemMeta(dmg);
        }
        Bukkit.getScheduler().runTask(plugin, p::updateInventory);
    }

    Set<Location> safe = new HashSet<>();

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION && cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return;

        Location hitLoc = event.getEntity().getLocation();

        for (Location centre : safe) {
            if (!centre.getWorld().equals(hitLoc.getWorld())) continue;
            if (centre.distanceSquared(hitLoc) <= 30) {
                event.setCancelled(true);
                return;
            }
        }
    }
}