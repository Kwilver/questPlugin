package me.kwilver.questPlugin;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.*;
import kr.toxicity.model.api.data.renderer.ModelRenderer;
import kr.toxicity.model.api.tracker.EntityTracker;
import me.kwilver.questPlugin.quests.easy.*;
import me.kwilver.questPlugin.quests.medium.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Oracle implements Listener, CommandExecutor {
    QuestPlugin main;
    Set<UUID> canAccept = new HashSet<>();
    Random random = new Random();
    List<Class<? extends Quest>> easyQuests = List.of(Farmer.class, Hunter.class, Miner.class, TasteTester.class, Traveler.class, TrialEnthusiast.class);
    List<Class<? extends Quest>> mediumQuests = List.of(Aquaman.class, Archaeologist.class, Brewery.class, CatPerson.class, me.kwilver.questPlugin.quests.medium.Condiut.class, DogPerson.class, Fisherman.class, FlowerCrown.class, TrialVeteran.class, TurtleMaster.class, Warrior.class);

    public ArmorStand oracle;

    Set<UUID> talkingToNpc = new HashSet<>();

    public Oracle(QuestPlugin main, Location oracleSpawn) {
        this.main = main;

        oracle = spawnOracle(oracleSpawn);
        startAmbientSoundLoop(oracle, Sound.AMBIENT_SOUL_SAND_VALLEY_MOOD, 1.0f, 1.0f, 100);
        QuestPlugin.getInstance().getServer().getPluginManager().registerEvents(this, QuestPlugin.getInstance());

        main.getCommand("acceptChallenge").setExecutor(this);
        main.getCommand("declineChallenge").setExecutor(this);
    }

    public ArmorStand spawnOracle(Location spawnLocation) {
        BetterModelPlugin api = BetterModel.plugin();

        ModelRenderer renderer = api.modelManager().renderer("eye_hooded_entity_posed");

        if(renderer != null) {
            ArmorStand oracle = spawnLocation.getWorld().spawn(spawnLocation, ArmorStand.class);

            EntityTracker tracker = renderer.create(oracle);

            tracker.spawnNearby();
            return oracle;
        }
        return null;
    }

    private void talkToPlayer(Player p) {
        if(main.activeQuests.containsKey(p)) {
            oracleText(p, "Complete your current quest, then come back...");
            return;
        }
        if (main.lastCompletions.containsKey(p.getUniqueId()) &&
                System.currentTimeMillis() - main.lastCompletions.get(p.getUniqueId()) < 30 * 60 * 1000) {

            long remaining = (30 * 60 * 1000) - (System.currentTimeMillis() - main.lastCompletions.get(p.getUniqueId()));

            new BukkitRunnable() {
                public void run() {
                    long seconds = remaining / 1000 % 60;
                    long minutes = remaining / (1000 * 60) % 60;
                    long hours = remaining / (1000 * 60 * 60);

                    StringBuilder sb = new StringBuilder("§cYou must wait ");

                    if (hours > 0) sb.append(hours).append("h ");
                    if (minutes > 0) sb.append(minutes).append("m ");
                    if (seconds > 0 || (hours == 0 && minutes == 0)) sb.append(seconds).append("s ");

                    sb.append("before taking another challenge.");

                    p.sendMessage(sb.toString().trim());
                }
            }.runTaskLater(QuestPlugin.getInstance(), 60);

            oracleText(p, "Rest for a while and come back later...");
            return;
        }
        if(!talkingToNpc.contains(p.getUniqueId())) {
            oracleText(p, "Greetings, traveler.");

            new BukkitRunnable() {
                @Override
                public void run() {
                    oracleText(p, "I offer you a trial. Succeed, and your power will grow.");
                }
            }.runTaskLater(QuestPlugin.getInstance(), 7 * 20);

            new BukkitRunnable() {
                @Override
                public void run() {
                    oracleText(p, "This challenge is not for the faint of heart. Do you accept?");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            askUser(p);
                        }
                    }.runTaskLater(QuestPlugin.getInstance(), 5 * 20);
                }
            }.runTaskLater(QuestPlugin.getInstance(), 14 * 20);
        }
    }

    private void askUser(Player p) {
        canAccept.add(p.getUniqueId());

        new BukkitRunnable() {
            @Override
            public void run() {
                canAccept.remove(p.getUniqueId());
            }
        }.runTaskLater(QuestPlugin.getInstance(), 60 * 20);

        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        TextComponent message = new TextComponent("§6Do you accept the challenge? ");

        TextComponent accept = new TextComponent("[Accept]");
        accept.setColor(ChatColor.GREEN);
        accept.setBold(true);
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptchallenge"));

        TextComponent deny = new TextComponent(" [Decline]");
        deny.setColor(ChatColor.RED);
        deny.setBold(true);
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/declinechallenge"));

        message.addExtra(accept);
        message.addExtra(deny);

        p.sendMessage(message);
    }

    private void oracleText(Player p, String s) {
        p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, 2.0f, 0.5f);
        p.playSound(p.getLocation(), Sound.AMBIENT_SOUL_SAND_VALLEY_MOOD, 2.0f, 0.8f);
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.4f, 0.6f);
        p.sendTitle("", ChatColor.BOLD + "" + ChatColor.RED + s);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage("Only players can accept quests from the Oracle!");
            return true;
        }

        Player player = (Player) commandSender;

        if(!canAccept.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "The Oracle's offer has expired...");
            return true;
        }

        canAccept.remove(player.getUniqueId());

        if(command.getName().equalsIgnoreCase("acceptChallenge")) {
            List<String> lines = getStrings();
            oracleText(player, lines.get(random.nextInt(lines.size())));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(random.nextInt(10) < 3) {
                        main.newQuest(mediumQuests.get(random.nextInt(mediumQuests.size())), player);
                    } else {
                        main.newQuest(easyQuests.get(random.nextInt(easyQuests.size())), player);
                    }
                }
            }.runTaskLater(QuestPlugin.getInstance(), 20);

        }
        if(command.getName().equalsIgnoreCase("declineChallenge")) {
            oracleText(player, "Visit me again someday, will you?");
        }

        return true;
    }

    private static @NotNull List<String> getStrings() {
        List<String> lines = new ArrayList<>();

        lines.add("Well then...");
        lines.add("I wish you the best of luck...");
        lines.add("I wish to see you again one day...");
        lines.add("Prove yourself worthy, my friend...");
        lines.add("Good luck, traveler...");
        lines.add("I wonder if I'll ever get a day off from assigning quests...");
        lines.add("Return triumphant, my friend.");
        lines.add("I'll be awaiting your return.");
        lines.add("Don't get yourself hurt, now...");
        lines.add("I'll be watching your journey from above...");
        lines.add("Do try to keep all your limbs this time...");
        lines.add("Farewell and good luck...");
        lines.add("I have a good feeling about you, young one...");
        lines.add("If you fail, fail with style...");
        lines.add("I've seen many come and go. Make me remember your name.");
        lines.add("The trial knows no mercy. Neither should you.");
        lines.add("The time has come. Do not falter.");

        return lines;
    }

    public void startAmbientSoundLoop(LivingEntity entity, Sound sound, float volume, float pitch, int intervalTicks) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!entity.isValid()) {
                    cancel();
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().equals(entity.getWorld()) &&
                            player.getLocation().distanceSquared(entity.getLocation()) <= 64) {
                        player.playSound(entity.getLocation(), sound, volume, pitch);
                    }
                }
            }
        }.runTaskTimer(QuestPlugin.getInstance(), 0L, intervalTicks);
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked().equals(oracle)) {
            Player player = event.getPlayer();
            talkToPlayer(player);
            talkingToNpc.add(player.getUniqueId());
        }
    }
}
