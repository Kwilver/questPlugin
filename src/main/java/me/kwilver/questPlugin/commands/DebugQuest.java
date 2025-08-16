package me.kwilver.questPlugin.commands;

import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.quests.Instant;
import me.kwilver.questPlugin.quests.easy.*;
import me.kwilver.questPlugin.quests.medium.*;
import me.kwilver.questPlugin.quests.hard.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DebugQuest implements CommandExecutor {
    QuestPlugin main;
    public DebugQuest (QuestPlugin main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        main.newQuest(Miner.class, (Player) commandSender); //TODO this is terrible remove asap
        return true;
    }
}
