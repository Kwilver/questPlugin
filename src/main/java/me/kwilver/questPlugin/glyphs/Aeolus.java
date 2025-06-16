package me.kwilver.questPlugin.glyphs;

import me.kwilver.questPlugin.QuestPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Aeolus extends Glyph {
    public Aeolus(Player player) {
        super(player);
    }

    public void drawParticleCircle(Location center, double radius) {
        World world = center.getWorld();
        double points = radius * 5;

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            Location particleLocation = center.clone().add(x, 0, z);
            world.spawnParticle(Particle.WHITE_SMOKE, particleLocation, 1, 0, 0, 0, 0);
        }
    }

    @Override
    protected boolean useGlyph() {
        Location userLoc = user.getLocation().clone();
        new BukkitRunnable() {
            double radius = 0;
            public void run () {
                for(Player p : Bukkit.getOnlinePlayers()) {
                    Location pLoc = p.getLocation();

                    if(pLoc.getY() == userLoc.getY() || pLoc.getY() + 1 == userLoc.getY()) {
                        if(pLoc.distance(userLoc) <= radius) {
                            Location loc = pLoc.clone();
                            loc.setY(user.getLocation().getY());

                            Vector pVec = loc.toVector();
                            Vector uVec = user.getLocation().toVector();

                            Vector direction = pVec.subtract(uVec).normalize();

                            p.setVelocity(p.getVelocity().add(direction.multiply(5).add(new Vector(0, 5, 0))));
                        }
                    }
                }

                drawParticleCircle(userLoc, radius);

                radius += 0.5;

                if(radius >= 15) cancel();
            }
        }.runTaskTimer(QuestPlugin.getInstance(), 0, 1);
        return false;
    }
}
