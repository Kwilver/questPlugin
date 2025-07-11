package me.kwilver.questPlugin.utils;

import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;

import static com.comphenix.protocol.PacketType.Play.Client.*;

public class FakePlayerMovementListener extends PacketAdapter {

    private final ProtocolManager protocolManager;
    private final Plugin plugin;

    private final Map<UUID, ServerPlayer> fakePlayers;

    public FakePlayerMovementListener(Plugin plugin, Map<UUID, ServerPlayer> fakePlayers) {
        super(plugin, ListenerPriority.NORMAL,
                FLYING,
                POSITION,
                PacketType.Play.Client.LOOK,
                PacketType.Play.Client.POSITION_LOOK,
                PacketType.Play.Client.CLIENT_COMMAND,
                PacketType.Play.Client.ENTITY_ACTION
        );
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.fakePlayers = fakePlayers;
    }

    public void register() {
        protocolManager.addPacketListener(this);
    }

    public void unregister() {
        protocolManager.removePacketListener(this);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player realPlayer = event.getPlayer();
        ServerPlayer fakePlayer = fakePlayers.get(realPlayer.getUniqueId());

        if (fakePlayer == null) return; // no fake player to sync

        PacketContainer packet = event.getPacket();

        PacketType type = packet.getType();

        if (type == PacketType.Play.Client.POSITION
                || type == PacketType.Play.Client.POSITION_LOOK
                || type == PacketType.Play.Client.LOOK) {
            handleMovementPacket(fakePlayer, realPlayer, packet);
        } else if (type == PacketType.Play.Client.CLIENT_COMMAND) {
            handleClientCommand(fakePlayer, packet);
        } else if (type == PacketType.Play.Client.ENTITY_ACTION) {
            handleEntityAction(fakePlayer, packet);
        }
    }

    private void handleMovementPacket(ServerPlayer fakePlayer, Player realPlayer, PacketContainer packet) {
        double x = fakePlayer.getX();
        double y = fakePlayer.getY();
        double z = fakePlayer.getZ();
        float yaw = fakePlayer.getYRot();
        float pitch = fakePlayer.getXRot();

        // POSITION, POSITION_LOOK, FLYING packets have position data
        if (packet.getType() == POSITION
                || packet.getType() == PacketType.Play.Client.POSITION_LOOK
                || packet.getType() == FLYING) {
            x = packet.getDoubles().read(0);
            y = packet.getDoubles().read(1);
            z = packet.getDoubles().read(2);
        }

        // LOOK, POSITION_LOOK packets have look data
        if (packet.getType() == PacketType.Play.Client.LOOK
                || packet.getType() == PacketType.Play.Client.POSITION_LOOK) {
            yaw = packet.getFloat().read(0);
            pitch = packet.getFloat().read(1);
        }

        // Update fake player's position & rotation
        fakePlayer.setPos(x, y, z);
        fakePlayer.setYRot(yaw);
        fakePlayer.setXRot(pitch);
    }

    private void handleClientCommand(ServerPlayer fakePlayer, PacketContainer packet) {
        int command = packet.getIntegers().read(0);

        switch (command) {
            case 0:
                fakePlayer.setPose(Pose.CROUCHING);
                break;
            case 1:
                fakePlayer.setPose(Pose.STANDING);
                break;
            case 3:
                fakePlayer.setSprinting(true);
                break;
            case 4:
                fakePlayer.setSprinting(false);
                break;
        }
    }

    private void handleEntityAction(ServerPlayer fakePlayer, PacketContainer packet) {
        int action = packet.getIntegers().read(0);
        switch (action) {
            case 1:
                fakePlayer.setPose(Pose.CROUCHING);
                break;
            case 2:
                fakePlayer.setPose(Pose.STANDING);
                break;
            case 6:
                fakePlayer.setSprinting(true);
                break;
            case 7:
                fakePlayer.setSprinting(false);
                break;
            case 8:
                fakePlayer.jumpFromGround();
                break;
        }
    }
}
