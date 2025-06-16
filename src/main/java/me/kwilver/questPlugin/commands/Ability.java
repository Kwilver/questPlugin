package me.kwilver.questPlugin.commands;

import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.glyphs.Glyph;
import me.kwilver.questPlugin.glyphs.GlyphTracker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Ability implements CommandExecutor {
    QuestPlugin main;
    public Ability(QuestPlugin main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(!(commandSender instanceof Player)) return true;

        Player player = (Player) commandSender;

        GlyphTracker selected;
        List<GlyphTracker> glyphTrackerList = main.equippedGlyphs.getOrDefault(player.getUniqueId(), new ArrayList<>());
        if(strings[0].equals("1")) {
            if(!glyphTrackerList.isEmpty()) {
                glyphTrackerList.getFirst().activate(player);
            }
        }
        if(strings[0].equals("2")) {
            if(glyphTrackerList.size() > 1) {
                glyphTrackerList.get(1).activate(player);
            }
        }

        return true;
    }
}
