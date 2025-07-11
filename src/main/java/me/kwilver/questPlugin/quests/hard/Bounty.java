package me.kwilver.questPlugin.quests.hard;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.TickingQuest;
import me.kwilver.questPlugin.lootTables.Hard;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class Bounty extends Quest implements Listener, TickingQuest {
    UUID targetId;
    private long targetLogoutTime = -1;

    public Bounty(Player player, QuestPlugin main) {
        super(2 * 60 * 60, player, main, Hard.class);

        if(Bukkit.getOnlinePlayers().size() > 1) {
            List<Player> choices = new ArrayList<>();
            for(Player p : Bukkit.getOnlinePlayers()) {
                if(p != player) {
                    choices.add(p);
                }
            }
            targetId = choices.get(new Random().nextInt(choices.size())).getUniqueId();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Player target = Bukkit.getPlayer(targetId);

                if (target != null) {
                    targetLogoutTime = -1;

                    double distance = target.getLocation().distance(player.getLocation());

                    if (distance > 301) {
                        drawParticleLine(player, Color.RED);
                    } else if (distance > 201) {
                        drawParticleLine(player, Color.ORANGE);
                    } else if (distance > 101) {
                        drawParticleLine(player, Color.YELLOW);
                    } else if (distance > 51) {
                        drawParticleLine(player, Color.LIME);
                    } else if (distance > 0) {
                        drawParticleCircle();
                    }

                    target.setGlowing(distance <= 10);
                } else {
                    if (targetLogoutTime == -1) {
                        targetLogoutTime = System.currentTimeMillis();
                        player.sendMessage("Your target seems to have logged out--after 10 minutes of inactivity, the quest will be cancelled and won't count against you!");
                    } else {
                        long elapsed = System.currentTimeMillis() - targetLogoutTime;
                        if (elapsed >= 10 * 60 * 1000) {
                            main.cancelQuest(player, "Your target has been offline for 10 minutes...");
                            cancel();
                            return;
                        }
                    }
                }

                if(!questActive()) cancel();
            }
        }.runTaskTimer(main, 0, 1); //60 * 10 * 20
    }

    public void drawParticleLine(Player onlinePlayer, Color color) {
        if (player.getPlayer() == null || Bukkit.getPlayer(targetId) == null) return;
        Player target = Bukkit.getPlayer(targetId);

        Location from = player.getPlayer().getEyeLocation();
        Location to = target.getEyeLocation();

        Vector direction = to.toVector().subtract(from.toVector()).normalize();

        World world = onlinePlayer.getWorld();
        double length = 5.0;
        double spacing = 0.25;

        new BukkitRunnable() {
            double d = 0;

            @Override
            public void run() {
                if (d >= length) cancel();

                Vector offset = direction.clone().multiply(d);
                Location point = from.clone().add(offset);
                world.spawnParticle(Particle.DUST, point, 0, 0, 0, 0, 0, new Particle.DustOptions(color, 1));

                d += spacing;
            }
        }.runTaskTimer(main, 0, 1);
    }

    public void drawParticleCircle() {
        if (Bukkit.getPlayer(targetId) == null) return;

        Player target = Bukkit.getPlayer(targetId);
        Location center = target.getLocation();
        World world = center.getWorld();

        int points = 50;

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = Math.cos(angle) * 5;
            double z = Math.sin(angle) * 5;

            Location point = center.clone().add(x, 0, z);
            world.spawnParticle(Particle.DUST, point, 0, 0, 0, 0, 0, new Particle.DustOptions(Color.LIME, 1));
        }
    }

    @Override
    public void tick(Player onlinePlayer) {
    }

    @Override
    public ArrayList<String> questInfo() {
        return null;
    }

    @Override
    public String displayName() {
        return ChatColor.RED + "Bounty";
    }

    @EventHandler
    public void onTargetLogin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();
        if (targetId != null && joined.getUniqueId().equals(targetId)) {
            joined.setGlowing(false);
            targetLogoutTime = -1;
        }
    }

    @EventHandler
    public void onTargetKilled(PlayerDeathEvent event) {
        Player dead = event.getPlayer();
        Player killer = event.getEntity().getPlayer();

        if (killer == null) return;

        if (targetId != null && dead.getUniqueId().equals(targetId) && killer.getUniqueId().equals(player.getUniqueId())) {
            main.cancelQuest(player, "You have killed your bounty!");
            (dead).setGlowing(false);
        }
    }
}