package me.kwilver.questPlugin.commands;

import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.quests.QuestManager;
import me.kwilver.questPlugin.quests.QuestManager.QuestEntry;
import me.kwilver.questPlugin.lootTables.*;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestToggleCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean isEnable = label.equalsIgnoreCase("enablequest");

        if (args.length != 2) {
            sender.sendMessage("Usage: /" + label + " <easy|medium|hard> <questName>");
            return true;
        }

        String difficulty = args[0].toLowerCase();
        String questName = args[1];
        boolean result = isEnable ?
                QuestPlugin.questManager.enable(questName) :
                QuestPlugin.questManager.disable(questName);

        if (result) {
            sender.sendMessage((isEnable ? "Enabled " : "Disabled ") + questName + " successfully.");
        } else {
            sender.sendMessage("Quest not found: " + questName);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        boolean isEnable = alias.equalsIgnoreCase("enablequest");

        if (args.length == 1) {
            return List.of("easy", "medium", "hard");
        }

        if (args.length == 2) {
            Class<?> difficulty = switch (args[0].toLowerCase()) {
                case "easy" -> Easy.class;
                case "medium" -> Medium.class;
                case "hard" -> Hard.class;
                default -> null;
            };

            if (difficulty == null) return Collections.emptyList();

            List<String> suggestions = new ArrayList<>();
            for (QuestEntry entry : QuestManager.allQuests) {
                boolean isCorrectState = isEnable != QuestManager.enabledQuests.contains(entry);

                if (entry.difficulty().equals(difficulty) && isCorrectState) {
                    suggestions.add(entry.questClass().getSimpleName());
                }
            }
            return suggestions;
        }

        return Collections.emptyList();
    }
}
