package me.kwilver.questPlugin.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.mojang.authlib.GameProfile;
import me.kwilver.questPlugin.QuestPlugin;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class FakePlayer extends ServerPlayer {
    private final ServerLevel level;
    private final Location spawnLocation;
    private final ProtocolManager manager;
    private final GameProfile profile;

    public FakePlayer(Player original, Location location, GameProfile fakeProfile) {
        super(
                ((CraftServer) Bukkit.getServer()).getServer(),
                ((CraftWorld) original.getWorld()).getHandle(),
                fakeProfile,
                ClientInformation.createDefault()
        );
        this.level = ((CraftWorld) original.getWorld()).getHandle();
        this.spawnLocation = location;
        this.profile = fakeProfile;
        this.manager = ProtocolLibrary.getProtocolManager();

        this.connection = new TutNetHandler(server, new TutNetworkManager(net.minecraft.network.protocol.PacketFlow.CLIENTBOUND), this, profile);
        this.uuid = UUID.randomUUID();

        setPos(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ());

        // Copy armor, main hand, and offhand items
        setItemSlot(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(original.getInventory().getHelmet()));
        setItemSlot(EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(original.getInventory().getChestplate()));
        setItemSlot(EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(original.getInventory().getLeggings()));
        setItemSlot(EquipmentSlot.FEET, CraftItemStack.asNMSCopy(original.getInventory().getBoots()));
        setItemSlot(EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(original.getInventory().getItemInMainHand()));
        setItemSlot(EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(original.getInventory().getItemInOffHand()));

        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                manager.sendServerPacket(player, addPlayerPacket());
                manager.sendServerPacket(player, playerInfoPacket());
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        this.setNoGravity(false);
        this.setGameMode(GameType.SURVIVAL);
        this.getAbilities().flying = false;
        this.getBukkitEntity().setNoDamageTicks(0);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!original.isOnline()) {
                    deletePlayer();
                    cancel();
                    return;
                }
                Location targetLoc = original.getLocation();
                setRot(targetLoc.getYaw(), targetLoc.getPitch());
                setYHeadRot(targetLoc.getYaw());
            }
        }.runTaskTimer(QuestPlugin.getInstance(), 0L, 2L);

        level.addFreshEntity(this);
    }

    Long last;
    public void jump() {
        if(onGround() && last != null && System.currentTimeMillis() - last > 250) {
            setDeltaMovement(getDeltaMovement().add(0, 0.5, 0));
        }
        last = System.currentTimeMillis();
    }

    @Override
    public void tick() {
        super.tick();
        doTick();
    }

    public void deletePlayer() {
        Location l = getBukkitEntity().getLocation();
        for (int i = 0; i < 10; i++) {
            getBukkitEntity().getWorld().spawnParticle(Particle.ASH, l.getX(), l.getY(), l.getZ(), 1);
        }
        getBukkitEntity().remove();
    }

    private PacketContainer playerInfoPacket() {
        Collection<ServerPlayer> col = Collections.singletonList(this);
        return PacketContainer.fromPacket(new net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER),
                col
        ));
    }

    private PacketContainer addPlayerPacket() {
        PacketContainer p = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        p.getIntegers().write(0, this.getBukkitEntity().getEntityId());
        p.getUUIDs().write(0, uuid);
        p.getEntityTypeModifier().write(0, org.bukkit.entity.EntityType.PLAYER);
        p.getDoubles()
                .write(0, spawnLocation.getX())
                .write(1, spawnLocation.getY())
                .write(2, spawnLocation.getZ());
        p.getBytes()
                .write(0, (byte) (spawnLocation.getPitch() * 256.0F / 360.0F))
                .write(1, (byte) (spawnLocation.getYaw() * 256.0F / 360.0F));
        return p;
    }
}

class TutNetHandler extends ServerGamePacketListenerImpl {
    public TutNetHandler(MinecraftServer server, net.minecraft.network.Connection conn, ServerPlayer player, GameProfile profile) {
        super(server, conn, player, new CommonListenerCookie(profile, 0, ClientInformation.createDefault(), false));
    }

    @Override
    public void send(net.minecraft.network.protocol.Packet<?> packet) {}
}

class TutNetworkManager extends net.minecraft.network.Connection {
    public TutNetworkManager(net.minecraft.network.protocol.PacketFlow direction) {
        super(direction);
    }
}