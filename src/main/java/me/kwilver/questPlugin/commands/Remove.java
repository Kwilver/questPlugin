package me.kwilver.questPlugin.commands;

import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.glyphs.GlyphTracker;
import me.kwilver.questPlugin.quests.medium.Archaeologist;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Remove implements CommandExecutor {
    QuestPlugin main;
    public Remove(QuestPlugin main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if(args.length != 2) {
            sender.sendMessage(ChatColor.RED + "2 Arguments Required! Usage: /remove [number] [PlayerName]");
            return true;
        }

        int num;
        try {
            num = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid Integer \"" + args[0] + "\"!");
            return true;
        }

        if(Bukkit.getPlayer(args[1]) == null) {
            sender.sendMessage(ChatColor.RED + "Player \"" + args[1] + "\" doesn't exist!");
            return true;
        }

        UUID uuid = Objects.requireNonNull(Bukkit.getPlayer(args[1])).getUniqueId();

        if(!main.equippedGlyphs.containsKey(uuid)) {
            sender.sendMessage(ChatColor.RED + "Player doesn't have any glyphs equipped!");
            return true;
        }

        if(main.equippedGlyphs.get(uuid).get(num - 1) == null) {
            sender.sendMessage(ChatColor.RED + "Player doesn't have a glyph equipped in that slot!");
            return true;
        }

        List<GlyphTracker> list = main.equippedGlyphs.get(uuid);
        list.remove(num - 1);

        main.equippedGlyphs.put(uuid, list);
        sender.sendMessage(ChatColor.GREEN + "Success!");
        return true;
    }
}
