package me.kwilver.questPlugin;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.*;
import kr.toxicity.model.api.data.blueprint.ModelBlueprint;
import kr.toxicity.model.api.data.renderer.ModelRenderer;
import kr.toxicity.model.api.manager.ModelManager;
import kr.toxicity.model.api.tracker.EntityTracker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class Oracle implements Listener {
    QuestPlugin main;
    public Oracle(QuestPlugin main, Location oracleSpawn) {
        this.main = main;
        spawnOracle(oracleSpawn);
    }

    public void spawnOracle(Location spawnLocation) {
        BetterModelPlugin api = BetterModel.plugin();

        ModelRenderer renderer = api.modelManager().renderer("eye_hooded_entity_posed");

        if(renderer != null) {
            Zombie oracle = spawnLocation.getWorld().spawn(spawnLocation, Zombie.class);

            EntityTracker tracker = renderer.create(oracle);

            tracker.spawnNearby();
        }
    }
}
