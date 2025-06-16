package me.kwilver.questPlugin.commands;

import me.kwilver.questPlugin.QuestPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QuestInfo implements CommandExecutor {
    QuestPlugin main;
    public QuestInfo(QuestPlugin main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(commandSender instanceof Player player) {
            if(main.activeQuests.containsKey(player)) {
                for(String string : main.activeQuests.get(player).info()) {
                    player.sendMessage(string);
                    player.playSound(player.getLocation(), Sound.BLOCK_COPPER_BULB_TURN_OFF, 1, 1);
                }
            } else {
                player.sendMessage(ChatColor.RED + "You aren't doing a quest at the moment--visit the oracle!");
                player.playSound(player.getLocation(), Sound.BLOCK_COPPER_BULB_TURN_ON, 1, 1);
            }
        }
        return true;
    } //TODO fill all cases with messages
}
