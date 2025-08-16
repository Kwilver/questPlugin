package me.kwilver.questPlugin.glyphs;

import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.utils.FakePlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity.RemovalReason;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Phantom extends Glyph implements Listener {
    private FakePlayer fakePlayer;
    private Listener attackListener;

    public Phantom(Player player) {
        super(player);
        QuestPlugin.getInstance().getServer().getPluginManager().registerEvents(this, QuestPlugin.getInstance());
    }

    @Override
    protected boolean useGlyph() {
        Location loc = user.getLocation();
        Location spawnLoc = loc.clone().add(new Vector(0, 0, 3));

        if (!isClear(spawnLoc)) {
            return false;
        }

        // Poof particles on spawn
        spawnLoc.getWorld().spawnParticle(Particle.CLOUD, spawnLoc.add(0, 1, 0), 60, 0.8, 0.8, 0.8, 0.02);

        fakePlayer = new FakePlayer(
                user,
                spawnLoc,
                ((CraftPlayer) user).getHandle().getGameProfile()
        );

        // Schedule despawn after 10 seconds (200 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (fakePlayer != null && !fakePlayer.isRemoved()) {
                    Location despawnLoc = fakePlayer.getBukkitEntity().getLocation();
                    despawnLoc.getWorld().spawnParticle(Particle.CLOUD, despawnLoc.add(0, 1, 0), 60, 0.8, 0.8, 0.8, 0.02);
                    fakePlayer.deletePlayer();
                }
            }
        }.runTaskLater(plugin, 20 * 10); // 10 seconds

        Map<UUID, ServerPlayer> fakePlayers = new HashMap<>();
        fakePlayers.put(user.getUniqueId(), fakePlayer);

        attackListener = new Listener() {
            @EventHandler
            public void onPlayerSwing(PlayerInteractEvent event) {
                if (!event.getPlayer().equals(user)) return;
                if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

                Location eye = fakePlayer.getBukkitEntity().getEyeLocation();
                Vector dir = eye.getDirection();
                RayTraceResult r = eye.getWorld().rayTraceEntities(eye, dir, 5,
                        e -> e instanceof LivingEntity && !e.equals(fakePlayer.getBukkitEntity()));

                if (r != null && r.getHitEntity() instanceof LivingEntity target) {
                    double dmg = user.getAttribute(Attribute.ATTACK_DAMAGE).getValue();
                    target.damage(dmg, fakePlayer.getBukkitEntity());
                    fakePlayer.getBukkitEntity().swingHand(EquipmentSlot.HAND);
                }
            }
        };
        QuestPlugin.getInstance().getServer().getPluginManager().registerEvents(attackListener, QuestPlugin.getInstance());

        // Sync movement task
        new BukkitRunnable() {
            Location last;
            @Override
            public void run() {
                if (!user.isOnline() || fakePlayer == null || fakePlayer.isRemoved()) {
                    if (fakePlayer != null) fakePlayer.remove(RemovalReason.KILLED);
                    HandlerList.unregisterAll(attackListener);
                    cancel();
                    return;
                }
                if (last != null) {
                    Vector movement = user.getLocation().toVector().subtract(last.toVector());
                    Vector currentVel = fakePlayer.getBukkitEntity().getVelocity();
                    Vector newVel = new Vector(movement.getX(), currentVel.getY(), movement.getZ());
                    fakePlayer.getBukkitEntity().setVelocity(newVel);
                }
                last = user.getLocation().clone();
                if (user.getVelocity().getY() > 0) fakePlayer.jump();
            }
        }.runTaskTimer(QuestPlugin.getInstance(), 0L, 1L);

        return true;
    }

    private boolean isClear(Location loc) {
        return !loc.getBlock().getType().isSolid() && !loc.clone().add(0, 1, 0).getBlock().getType().isSolid();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (fakePlayer != null && e.getPlayer() == fakePlayer.getBukkitEntity()) {
            e.getDrops().clear();
        }
    }
}