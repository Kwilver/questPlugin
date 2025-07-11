package me.kwilver.questPlugin.glyphs;

import me.kwilver.questPlugin.QuestPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Mimic extends Glyph implements Listener {
    boolean active = false;
    List<UUID> aud = new ArrayList<>();
    LivingEntity target;
    Random random = new Random();

    public Mimic(Player player) {
        super(player);

        Bukkit.getServer().getPluginManager().registerEvents(this, QuestPlugin.getInstance());
    }

    @Override
    protected boolean useGlyph() {
        active = true;
        user.sendMessage("Right click a mob to \"Morph\" into it!");

        new BukkitRunnable() {
            @Override
            public void run() {
                reset(user);
                HandlerList.unregisterAll(Mimic.this);
            }
        }.runTaskLater(plugin, 30 * 20);

        return true;
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!event.getPlayer().equals(user) || !active) return;
        Entity clicked = event.getRightClicked();

        if(clicked instanceof LivingEntity e && clicked != QuestPlugin.oracle.oracle) {
            morph(e);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().equals(user)) return;
        reset(user);
    }

    private void morph(LivingEntity t) {
        this.target = t;
        target.setCollidable(false);

        for(Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(plugin, user);
            aud.add(p.getUniqueId());
        }

        user.setMaxHealth(target.getMaxHealth());
        user.sendMessage(user.getAttribute(Attribute.SCALE).getBaseValue() + " " + target.getEyeHeight());

        user.getAttribute(Attribute.SCALE).setBaseValue(target.getBoundingBox().getHeight() / 2);
        user.hideEntity(plugin, target);

        user.setHealth(Math.min(target.getHealth(), user.getAttribute(Attribute.MAX_HEALTH).getValue()));

        new BukkitRunnable() {
            @Override
            public void run() {
                if(!active) {
                    target.setCollidable(true);
                    cancel();
                }
                if (!active || !target.isValid() || target.isDead()) {
                    if(active) user.setHealth(0.0);
                    cancel();
                    return;
                }

                target.teleport(user);
                if(random.nextBoolean()) target.getWorld().spawnParticle(Particle.ASH, target.getLocation(), 1);
                user.setHealth(target.getHealth());
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void reset(Player p) {
        active = false;
        if(target.isValid()) p.showEntity(plugin, target);
        for (UUID id : aud) {
            Player viewer = Bukkit.getPlayer(id);
            if (viewer != null && viewer.isOnline()) {
                viewer.showPlayer(plugin, p);
            } else {
                QuestPlugin.hiddenPlayers.put(id, p.getUniqueId());
            }
        }
        aud.clear();

        p.getAttribute(Attribute.SCALE).setBaseValue(1.0);
        p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
        p.setHealth(p.getMaxHealth());
    }
}
