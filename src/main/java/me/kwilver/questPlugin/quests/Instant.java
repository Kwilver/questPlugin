package me.kwilver.questPlugin.quests;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.TickingQuest;
import me.kwilver.questPlugin.lootTables.Debug;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Instant extends Quest implements TickingQuest {

    public Instant(Player player, QuestPlugin main) {
        super(3 * 60 * 60, player, main, Debug.class);
    }

    public void tick(Player onlinePlayer) {
        main.endQuest(player, true);
    }

    public ArrayList<String> questInfo() {
        ArrayList<String> info = new ArrayList<>();
        return info;
    }

    @Override
    public String displayName() {
        return ChatColor.GREEN + "Farmer";
    }
}
