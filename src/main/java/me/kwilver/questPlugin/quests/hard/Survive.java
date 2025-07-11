package me.kwilver.questPlugin.quests.hard;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.TickingQuest;
import me.kwilver.questPlugin.TimedQuest;
import me.kwilver.questPlugin.lootTables.Hard;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class Survive extends Quest implements Listener, TickingQuest, TimedQuest {
    QuestPlugin main;
    WorldBorder border;
    BukkitRunnable runnable;

    public Survive(Player player, QuestPlugin main) {
        super(30 * 60, player, main, Hard.class);

        border = Bukkit.createWorldBorder();
        border.setCenter(QuestPlugin.oracle.oracle.getLocation());
        border.setSize(10);

        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if(border.getSize() >= 200) {
                    cancel();
                }
                border.setSize(border.getSize() + 0.1);

                Location center = border.getCenter();
                double size = border.getSize() / 2;
                double y = player.getLocation().getY() + 1.5;

                double spacing = 0.5;

                Particle.DustOptions dust = new Particle.DustOptions(Color.RED, 1.0f);

                for (double x = -size; x <= size; x += spacing) {
                    player.getWorld().spawnParticle(Particle.DUST, center.clone().add(x, y, -size), 1, 0, 0, 0, 0, dust);
                    player.getWorld().spawnParticle(Particle.DUST, center.clone().add(x, y, size), 1, 4, 0, 0, 0, dust);
                }
                for (double z = -size; z <= size; z += spacing) {
                    player.getWorld().spawnParticle(Particle.DUST, center.clone().add(-size, y, z), 1, 0, 0, 0, 0, dust);
                    player.getWorld().spawnParticle(Particle.DUST, center.clone().add(size, y, z), 1, 0, 0, 0, 0, dust);
                }

                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 1));
            }
        };

        runnable.runTaskTimer(main, 0, 1);

        this.main = main;
    }

    public void tick(Player onlinePlayer) {
        if(!border.isInside(onlinePlayer.getLocation())) {
            onlinePlayer.setWorldBorder(null);
            main.endQuest(player, false);
            runnable.cancel();
        }
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        info.add("Survive without leaving the border.");
        info.add("TIME: 30 Minutes");
        info.add("PROGRESS: Going well, for now...");
        return info;
    }

    public String displayName() {
        return ChatColor.DARK_RED + "Survive";
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if(e.getPlayer() == player) {
            e.getPlayer().setWorldBorder(null);
            main.endQuest(player, false);
            runnable.cancel();
        }
    }
}
