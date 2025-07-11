package me.kwilver.questPlugin.glyphs;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class Spikes extends Glyph {
    List<BlockDisplay> spawnedSpikes = new ArrayList<>();

    public Spikes(Player selectedPlayer) {
        super(selectedPlayer);
    }

    public List<Location> getLineOnGround(Location start, Location end) {
        List<Location> locations = new ArrayList<>();

        World world = start.getWorld();
        if (world == null || !world.equals(end.getWorld())) return locations;

        int x1 = start.getBlockX();
        int z1 = start.getBlockZ();
        int x2 = end.getBlockX();
        int z2 = end.getBlockZ();

        int dx = Math.abs(x2 - x1);
        int dz = Math.abs(z2 - z1);

        int sx = x1 < x2 ? 1 : -1;
        int sz = z1 < z2 ? 1 : -1;

        int err = dx - dz;

        while (true) {
            int y = world.getHighestBlockYAt(x1, z1) + 1;
            locations.add(new Location(world, x1 + 0.5, y, z1 + 0.5));

            if (x1 == x2 && z1 == z2) break;

            int e2 = 2 * err;
            if (e2 > -dz) {
                err -= dz;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                z1 += sz;
            }
        }

        return locations;
    }

    public void moveSpike(Location loc, int index, int maxIndex, Player target, Location finalLoc) {
        World world = loc.getWorld();
        if (world == null) return;

        float maxHeight = 2.0f;
        int steps = 10;

        boolean isFinal = (index >= maxIndex - 1);


        BlockData SpikeData = Bukkit.createBlockData(Material.POINTED_DRIPSTONE);
        BlockDisplay display = world.spawn(loc.clone(), BlockDisplay.class, e -> {
            e.setBlock(SpikeData);
        });

        spawnedSpikes.add(display);

        float scaleY = 3.0f;
        Vector scale = new Vector(1, scaleY, 1);

        float fraction = Math.max(0f, Math.min(1f, (float) index / maxIndex));
        float targetY = maxHeight * fraction;

        Vector startPos = new Vector(0, -3.0f, 0);
        Vector endPos = new Vector(0, targetY - 1.5f, 0);

        if (isFinal) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, steps + 20, 10, false, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, steps + 20, 128, false, false, false));
        }

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                float t = (float) tick / steps;
                Vector interp = startPos.clone().multiply(1 - t)
                        .add(endPos.clone().multiply(t));
                Transformation transformation = new Transformation(
                        interp.toVector3f(),
                        new Quaternionf(),
                        scale.toVector3f(),
                        new Quaternionf()
                );
                display.setTransformation(transformation);

                float dy = transformation.getTranslation().y;

                double preciseY = loc.getY()
                        + dy
                        + 1.7;

                if (isFinal) {
                    Location ride = new Location(
                            loc.getWorld(),
                            loc.getX() + 0.5,      // center X
                            preciseY,            // smoothly interpolated Y
                            loc.getZ() + 0.5,      // center Z
                            target.getLocation().getYaw(),
                            target.getLocation().getPitch()
                    );

                    target.teleport(ride);
                }

                tick++;
                if (tick > steps) {
                    if (isFinal) {
                        // final snap + stun
                        Location finish = new Location(
                                loc.getWorld(),
                                loc.getX() + 0.5,
                                preciseY,
                                loc.getZ() + 0.5,
                                target.getLocation().getYaw(),
                                target.getLocation().getPitch()
                        );
                        target.teleport(finish);
                        stunPlayer(target, finish);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }



    void stunPlayer(Player target, Location loc) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run () {
                if(ticks >= 7 * 20) {
                    retractSpikes();
                    this.cancel();
                }

                ticks++;

                target.teleport(loc);
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    void retractSpikes() {
        int steps = 10;

        for (BlockDisplay display : spawnedSpikes) {
            Vector3f translation = display.getTransformation().getTranslation();

            Vector startPos = new Vector(
                    translation.x(),
                    translation.y(),
                    translation.z()
            );

            Vector endPos = new Vector(translation.x(), -3.0f, translation.z());

            new BukkitRunnable() {
                int tick = 0;

                @Override
                public void run() {
                    float t = (float) tick / steps;
                    Vector interp = startPos.clone().multiply(1 - t)
                            .add(endPos.clone().multiply(t));

                    Transformation transformation = new Transformation(
                            interp.toVector3f(),
                            new Quaternionf(),
                            display.getTransformation().getScale(),
                            new Quaternionf()
                    );

                    display.setTransformation(transformation);

                    tick++;
                    if (tick > steps) {
                        display.remove();
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }

        spawnedSpikes.clear(); // clean up
    }


    @Override
    protected boolean useGlyph() {
        List<Player> targets = new ArrayList<>();

        for(Player target : Bukkit.getOnlinePlayers()) {
            if(target.getLocation().distance(user.getLocation()) < 15 && target != user) {
                targets.add(target);
                List<Location> path = getLineOnGround(user.getLocation(), target.getLocation());
                for (Location loc : path) {
                    new BukkitRunnable() {
                        @Override
                        public void run () {
                            moveSpike(loc, path.indexOf(loc), path.size(), target, path.getLast());
                        }
                    }.runTaskLater(plugin, path.indexOf(loc));
                }
            }
        }

        user.sendMessage(ChatColor.RED + "There are no other players nearby to stun!");
        return !targets.isEmpty();
    }
}
