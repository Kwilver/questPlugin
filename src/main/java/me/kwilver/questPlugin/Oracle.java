package me.kwilver.questPlugin;

import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.*;
import kr.toxicity.model.api.data.renderer.ModelRenderer;
import kr.toxicity.model.api.tracker.EntityTracker;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Oracle implements Listener, CommandExecutor {
    private final QuestPlugin main;
    private final Random random = new Random();
    private final Set<UUID> canAccept = new HashSet<>();
    private final Set<UUID> talkingToNpc = new HashSet<>();

    public ArmorStand oracle;
    private BukkitRunnable ambientTask;

    public Oracle(QuestPlugin main, Location oracleSpawn, UUID id) {
        this.main = main;

        if (id == null || !(Bukkit.getEntity(id) instanceof ArmorStand)) {
            oracle = oracleSpawn.getWorld().spawn(oracleSpawn, ArmorStand.class);
            connectModel();
            Bukkit.getLogger().warning("No ID was passed to Oracle, creating a new entity...");
        } else {
            oracle = (ArmorStand) Bukkit.getEntity(id);
        }

        startAmbientSoundLoop();
        Bukkit.getPluginManager().registerEvents(this, main);
        main.getCommand("acceptChallenge").setExecutor(this);
        main.getCommand("declineChallenge").setExecutor(this);
    }

    private void connectModel() {
        BetterModelPlugin api = BetterModel.plugin();
        ModelRenderer renderer = api.modelManager().renderer("eye_hooded_entity_posed");
        if (renderer != null) {
            EntityTracker tracker = renderer.create(oracle);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Only players can accept quests from the Oracle!");
            return true;
        }

        if (!canAccept.remove(p.getUniqueId())) {
            p.sendMessage(ChatColor.RED + "The Oracle's offer has expired...");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("acceptChallenge")) {
            oracleText(p, getStrings().get(random.nextInt(getStrings().size())));
            new BukkitRunnable() {
                @Override
                public void run() {
                    main.newQuest(main.questManager.getRandomQuest(), p);
                }
            }.runTaskLater(main, 20);
        } else {
            oracleText(p, "Visit me again someday, will you?");
        }

        talkingToNpc.remove(p.getUniqueId());
        return true;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked().equals(oracle)) {
            Player p = e.getPlayer();
            talkToPlayer(p);
            talkingToNpc.add(p.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        talkingToNpc.remove(e.getPlayer().getUniqueId());
        //DEBUG REMOVE
        main.lastCompletions.remove(e.getPlayer().getUniqueId());
    }

    private void talkToPlayer(Player p) {
        if (main.activeQuests.containsKey(p)) {
            oracleText(p, "Complete your current quest, then come back...");
            return;
        }

        UUID uid = p.getUniqueId();
        if (main.lastCompletions.containsKey(uid) &&
                System.currentTimeMillis() - main.lastCompletions.get(uid) < 30 * 60 * 1000L) {

            long remaining = 30 * 60 * 1000L - (System.currentTimeMillis() - main.lastCompletions.get(uid));
            new BukkitRunnable() {
                @Override
                public void run() {
                    long sec = remaining / 1000 % 60;
                    long min = remaining / (60 * 1000) % 60;
                    long hr  = remaining / (60 * 60 * 1000);
                    StringBuilder sb = new StringBuilder("§cYou must wait ");
                    if (hr > 0) sb.append(hr).append("h ");
                    if (min > 0) sb.append(min).append("m ");
                    if (sec > 0 || (hr == 0 && min == 0)) sb.append(sec).append("s ");
                    sb.append("before taking another challenge...");
                    p.sendMessage(sb.toString().trim());
                }
            }.runTaskLater(main, 60);
            oracleText(p, "Rest for a while and come back later...");
            talkingToNpc.remove(p.getUniqueId());
            return;
        }

        if(QuestPlugin.getTodayCount(uid) > 5) {
            oracleText(p, "You've fought hard today, my friend...");
            new BukkitRunnable() {
                @Override
                public void run() {
                    oracleText(p, "Rest up and come back tomorrow...");
                    talkingToNpc.remove(p.getUniqueId());
                }
            }.runTaskLater(main, 60);

            return;
        }

        if (!talkingToNpc.contains(uid)) {
            oracleText(p, "Greetings, traveler.");
            new BukkitRunnable() {
                @Override public void run() { oracleText(p, "I offer you a trial. Succeed, and your power will grow."); }
            }.runTaskLater(main, 7 * 20);
            new BukkitRunnable() {
                @Override public void run() {
                    oracleText(p, "This challenge is not for the faint of heart. Do you accept?");
                    new BukkitRunnable() {
                        @Override public void run() { askUser(p); }
                    }.runTaskLater(main, 5 * 20);
                }
            }.runTaskLater(main, 14 * 20);
        }
    }

    private void askUser(Player p) {
        canAccept.add(p.getUniqueId());
        new BukkitRunnable() {
            @Override public void run() { canAccept.remove(p.getUniqueId()); }
        }.runTaskLater(main, 60 * 20);

        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        TextComponent msg = new TextComponent("§6Do you accept the challenge? ");
        TextComponent yes = new TextComponent("[Accept]");
        yes.setColor(ChatColor.GREEN); yes.setBold(true);
        yes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptchallenge"));
        TextComponent no = new TextComponent(" [Decline]");
        no.setColor(ChatColor.RED); no.setBold(true);
        no.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/declinechallenge"));
        msg.addExtra(yes);
        msg.addExtra(no);
        p.sendMessage(msg);
    }

    private void oracleText(Player p, String s) {
        p.playSound(p.getLocation(), Sound.AMBIENT_CAVE, SoundCategory.MASTER, 1.5f, 0.6f);
        p.playSound(p.getLocation(), Sound.AMBIENT_SOUL_SAND_VALLEY_MOOD, SoundCategory.MASTER, 1.0f, 0.8f);
        p.playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_AMBIENT, SoundCategory.MASTER, 0.7f, 0.5f);
        p.playSound(p.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, SoundCategory.MASTER, 0.5f, 2.0f);
        p.sendTitle("", ChatColor.BOLD + "" + ChatColor.RED + s);
    }

    private static @NotNull List<String> getStrings() {
        return List.of(
                "Well then...", "I wish you the best of luck...", "I wish to see you again one day...",
                "Prove yourself worthy, my friend...", "Good luck, traveler...", "I wonder if I'll ever get a day off from assigning quests...",
                "Return triumphant, my friend.", "I'll be awaiting your return.", "Don't get yourself hurt, now...",
                "I'll be watching your journey from above...", "Do try to keep all your limbs this time...",
                "Farewell and good luck...", "I have a good feeling about you, young one...", "If you fail, fail with style...",
                "I've seen many come and go. Make me remember your name.", "The trial knows no mercy. Neither should you.",
                "The time has come. Do not falter."
        );
    }

    public void startAmbientSoundLoop() {
        final Sound ambientLoop   = Sound.AMBIENT_SOUL_SAND_VALLEY_LOOP;
        final Sound ambientAccent = Sound.AMBIENT_CAVE;

        final List<Sound> dayMusic = List.of(
                Sound.MUSIC_UNDER_WATER,
                Sound.MUSIC_CREATIVE,
                Sound.MUSIC_MENU
        );
        final List<Sound> nightMusic = List.of(
                Sound.MUSIC_DISC_5,
                Sound.MUSIC_DISC_11
        );

        final int maxRadius            = 24;
        final float baseVolume         = 1.0f;
        final float basePitch          = 1.0f;
        final long  musicCooldownMs    = 3 * 60 * 1000L;

        Map<UUID, Long> lastMusicPlay = new HashMap<>();

        ambientTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (oracle == null || !oracle.isValid()) {
                    cancel();
                    return;
                }

                Location source = oracle.getLocation();
                World    world  = source.getWorld();
                long     t      = world.getTime();
                List<Sound> pool = (t < 12300) ? dayMusic : nightMusic;

                for (Player p : world.getNearbyPlayers(source, maxRadius)) {
                    double dist = p.getLocation().distance(source);
                    float  vol  = baseVolume * (1 - (float) dist / maxRadius);

                    if (vol > 0.01f) {
                        p.playSound(source, ambientLoop,   SoundCategory.AMBIENT, vol,         basePitch);
                        p.playSound(source, ambientAccent, SoundCategory.AMBIENT, vol * 0.6f, basePitch - 0.2f);
                    }

                    long now    = System.currentTimeMillis();
                    long played = lastMusicPlay.getOrDefault(p.getUniqueId(), 0L);

                    if (now - played >= musicCooldownMs) {
                        Sound track = pool.get(random.nextInt(pool.size()));
                        lastMusicPlay.put(p.getUniqueId(), now);
                        p.stopSound(track, SoundCategory.MUSIC);
                        p.playSound(source, track, SoundCategory.MUSIC, 1.0f, 1.0f);
                    }
                }
            }
        };

        ambientTask.runTaskTimer(main, 0L, 100L);
    }

    public void stopAmbientSoundLoop() {
        if (ambientTask != null && !ambientTask.isCancelled()) {
            ambientTask.cancel();
        }
    }
}
