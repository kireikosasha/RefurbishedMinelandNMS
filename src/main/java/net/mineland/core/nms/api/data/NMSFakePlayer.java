package net.mineland.core.nms.api.data;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.mineland.core.nms.api.java.AsyncNMSScheduler;
import net.mineland.core.nms.services.NPCService;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class NMSFakePlayer {

    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    private final Player toPlayer;
    private final UUID uuid;
    private final int id;
    private Location location;
    private volatile String name, skinValue, skinSignature;

    public void show() {
        NPCService.spawnNPCSilent(toPlayer, location, name,
                        skinValue, skinSignature, uuid, id);
    }
    public void hide() {
        PacketContainer playerInfoPacket = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
        playerInfoPacket.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        final WrappedGameProfile profile = new WrappedGameProfile(uuid, name);
        List<PlayerInfoData> infoDataList = new ArrayList<>();
        infoDataList.add(new PlayerInfoData(profile, 1,
                        EnumWrappers.NativeGameMode.SURVIVAL,
                        WrappedChatComponent.fromText(profile.getName())));

        playerInfoPacket.getPlayerInfoDataLists().write(0, infoDataList);
        protocolManager.sendServerPacket(toPlayer, playerInfoPacket);

        PacketContainer entityDestroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        entityDestroyPacket.getIntegerArrays().write(0, new int[]{id});

        protocolManager.sendServerPacket(toPlayer, entityDestroyPacket);
    }
    public void changeLocation(final Location newLocation) {
        if (location.equals(newLocation)) return;
        //NPCService.destroyEntityByID(toPlayer, id);
        NPCService.spawnNPCSilent(toPlayer, newLocation, name,
                        skinValue, skinSignature, uuid, id);
        this.location = newLocation;
    }
    public void changeName(final String newName) {
        if (newName.equals(name)) return;
        //NPCService.destroyEntityByID(toPlayer, id);
        NPCService.spawnNPCSilent(toPlayer, location, newName,
                        skinValue, skinSignature, uuid, id);
        this.name = newName;
    }
    public void showAsync() {
        AsyncNMSScheduler.run(this::show);
    }
    public void hideAsync() {
        AsyncNMSScheduler.run(this::hide);
    }
    public void changeLocationAsync(final Location newLocation) {
        AsyncNMSScheduler.run(() -> changeLocation(newLocation));
    }
    public void changeNameAsync(final String newName) {
        AsyncNMSScheduler.run(() -> changeName(newName));
    }
}
