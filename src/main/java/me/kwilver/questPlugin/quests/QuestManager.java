package me.kwilver.questPlugin.quests;

import me.kwilver.questPlugin.Quest;
import me.kwilver.questPlugin.QuestPlugin;
import me.kwilver.questPlugin.lootTables.*;
import me.kwilver.questPlugin.lootTables.LootTable;
import me.kwilver.questPlugin.quests.easy.*;
import me.kwilver.questPlugin.quests.medium.*;
import me.kwilver.questPlugin.quests.hard.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestManager {
    File enabledQuestsFile = new File(QuestPlugin.getInstance().getDataFolder(), "enabledQuests.yml");
    YamlConfiguration enabledQuestsConfig = YamlConfiguration.loadConfiguration(enabledQuestsFile);
    
    public static Random random = new Random();

    public QuestManager() {
        if (!enabledQuestsConfig.contains("enabledQuests")) {
            enabledQuests.addAll(allQuests);
        } else {
            List<String> stored = enabledQuestsConfig.getStringList("enabledQuests");
            for (QuestEntry e : allQuests) {
                if (stored.contains(e.questClass.getSimpleName())) {
                    enabledQuests.add(e);
                }
            }
        }
    }

    public record QuestEntry(Class<? extends Quest> questClass, Class<? extends LootTable> difficulty) {}

    public static final List<QuestEntry> allQuests = List.of(
        new QuestEntry(Farmer.class, Easy.class),
        new QuestEntry(Hunter.class, Easy.class),
        new QuestEntry(Miner.class, Easy.class),
        new QuestEntry(TasteTester.class, Easy.class),
        new QuestEntry(Traveler.class, Easy.class),
        new QuestEntry(TrialEnthusiast.class, Easy.class),

        new QuestEntry(Aquaman.class, Medium.class),
        new QuestEntry(Archaeologist.class, Medium.class),
        new QuestEntry(Brewery.class, Medium.class),
        new QuestEntry(CatPerson.class, Medium.class),
        new QuestEntry(Condiut.class, Medium.class),
        new QuestEntry(DogPerson.class, Medium.class),
        new QuestEntry(Fisherman.class, Medium.class),
        new QuestEntry(FlowerCrown.class, Medium.class),
        new QuestEntry(TrialVeteran.class, Medium.class),
        new QuestEntry(TurtleMaster.class, Medium.class),
        new QuestEntry(Warrior.class, Medium.class),

        new QuestEntry(MasterFarmer.class, Hard.class),
        new QuestEntry(TrueHunter.class, Hard.class),
        new QuestEntry(LavaMiner.class, Hard.class),
        new QuestEntry(TrialMaster.class, Hard.class),
        new QuestEntry(Survive.class, Hard.class)
    );

    public static List<QuestEntry> enabledQuests = new ArrayList<>();

    public Class<? extends Quest> getRandomQuest() {
        int roll = random.nextInt(10);
        Class<? extends LootTable> selectedDifficulty;

        if (roll < 7) {
            selectedDifficulty = Easy.class;
        } else if (roll < 9) {
            selectedDifficulty = Medium.class;
        } else {
            selectedDifficulty = Hard.class;
        }

        List<Class<? extends Quest>> eligible = new ArrayList<>();
        for (QuestEntry entry : allQuests) {
            if (entry.difficulty().equals(selectedDifficulty) && enabledQuests.contains(entry)) {
                if(entry.questClass == Bounty.class && Bukkit.getOnlinePlayers().size() <= 1) {
                    continue;
                }
                eligible.add(entry.questClass());
            }
        }

        if (eligible.isEmpty()) return null;

        return eligible.get(random.nextInt(eligible.size()));
    }

    public void enableAll() {
        enabledQuests = allQuests;
    }

    public void disableAll() {
        enabledQuests.clear();
    }

    public boolean enable(String name) {
        for(QuestEntry e : allQuests) {
            if(e.questClass.getSimpleName().equalsIgnoreCase(name)) {
                if(!enabledQuests.contains(e)) {
                    enabledQuests.add(e);
                }
                return true;
            }
        }
        return false;
    }

    public boolean disable(String name) {
        for(QuestEntry e : allQuests) {
            if (e.questClass.getSimpleName().equalsIgnoreCase(name)) {
                enabledQuests.remove(e);
                return true;
            }
        }
        return false;
    }

    public List<String> disabledQuests() {
        List<String> disabledQuests = new ArrayList<>();
        for(QuestEntry e : allQuests) {
            if(!enabledQuests.contains(e)) {
                disabledQuests.add(e.questClass.getSimpleName());
            }
        }
        return disabledQuests;
    }

    public List<String> enabledQuests() {
        List<String> enabled = new ArrayList<>();
        for(QuestEntry e : enabledQuests) {
            enabled.add(e.questClass.getSimpleName());
        }
        return enabled;
    }

    public void onDisable() {
        List<String> enabled = new ArrayList<>();
        for(QuestEntry e : enabledQuests) {
            enabled.add(e.questClass.getSimpleName());
        }
        enabledQuestsConfig.set("enabledQuests", enabled);

        try {
            enabledQuestsConfig.save(enabledQuestsFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Issue with saving enabled quests, please double check them when the server starts up again!");
        }
    }
}
