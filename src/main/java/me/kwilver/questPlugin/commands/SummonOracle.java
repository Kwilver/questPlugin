package me.kwilver.questPlugin.commands;

import me.kwilver.questPlugin.Oracle;
import me.kwilver.questPlugin.QuestPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SummonOracle implements CommandExecutor {
    QuestPlugin main;

    public SummonOracle(QuestPlugin main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(!(commandSender instanceof Player sender)) {
            commandSender.sendMessage("You must be a player to use this command.");
            return true;
        }

        if(QuestPlugin.getOracle() == null) {
            QuestPlugin.setOracle(new Oracle(main, sender.getLocation(), null));
        } else {
             QuestPlugin.getOracle().oracle.teleport(sender.getLocation());
        }

        sender.sendMessage("Summoned the Oracle!");

        return true;
    }
}
