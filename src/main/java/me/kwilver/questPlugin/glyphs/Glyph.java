package me.kwilver.questPlugin.glyphs;

import me.kwilver.questPlugin.QuestPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class Glyph {
    protected Player user;
    protected Plugin plugin = QuestPlugin.getInstance();

    public Glyph(Player player) {
        this.user = player;
    }

    protected abstract boolean useGlyph();
}
