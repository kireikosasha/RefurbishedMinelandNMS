package net.mineland.core.nms.services;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.mineland.core.nms.api.data.NMSFakePlayer;
import net.mineland.core.nms.api.java.AsyncNMSScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

@UtilityClass
public class NPCService {

    @Getter
    private final List<NMSFakePlayer> npcList
                    = Collections.synchronizedList(new ArrayList<>());
    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    /*
    Вместо обращение к кривому NMS, мы
    будем создавать NPC вручную через пакет.
    Стандартный вариант, дальнейшее управление
    можно выполнять через findBy() [NMSFakePlayerFinder]
     */
    public NMSFakePlayer spawnNPC(final Player toPlayer, final Location loc, final String npcName,
                                  final String skinValue, final String skinSignature) {
        final NMSFakePlayer nmsFakePlayer = spawnNPCSilent(
                        toPlayer, loc, npcName, skinValue, skinSignature,
                        UUID.randomUUID(), new Random().nextInt(Integer.MAX_VALUE));
        npcList.add(nmsFakePlayer);
        return nmsFakePlayer;
    }

    /*
    Если вы хотите спавнить NPC без добавления в npcList,
    однако вы не сможете им управлять, если не управляете
    существующим экземпляром...
     */
    public NMSFakePlayer spawnNPCSilent(final Player toPlayer, final Location loc, final String npcName,
                         final String skinValue, final String skinSignature, final UUID uuid, final int id) {
        final WrappedGameProfile profile = new WrappedGameProfile(uuid, npcName);
        if (skinValue != null && skinSignature != null) {
            profile.getProperties().put("textures", new WrappedSignedProperty("textures", skinValue, skinSignature));
        }
        final PacketContainer playerInfoPacket = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
        playerInfoPacket.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        final List<PlayerInfoData> infoDataList = new ArrayList<>();
        infoDataList.add(new PlayerInfoData(profile, 1, EnumWrappers.NativeGameMode.SURVIVAL,
                        WrappedChatComponent.fromText(npcName)));
        playerInfoPacket.getPlayerInfoDataLists().write(0, infoDataList);
        protocolManager.sendServerPacket(toPlayer, playerInfoPacket);

        final PacketContainer spawnPacket = protocolManager.createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        spawnPacket.getIntegers().write(0, id);
        spawnPacket.getUUIDs().write(0, profile.getUUID());
        spawnPacket.getDoubles().write(0, loc.getX());
        spawnPacket.getDoubles().write(1, loc.getY());
        spawnPacket.getDoubles().write(2, loc.getZ());
        spawnPacket.getBytes().write(0, (byte) (loc.getYaw() * 256 / 360));
        // Спавним NPC
        protocolManager.sendServerPacket(toPlayer, spawnPacket);
        // Задаем позицию головы
        final PacketContainer headRotation = protocolManager.createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        headRotation.getIntegers().write(0, id);
        headRotation.getBytes().write(0, (byte) (loc.getYaw() * 256 / 360));
        protocolManager.sendServerPacket(toPlayer, headRotation);
        return new NMSFakePlayer(toPlayer, uuid, id,
                        loc, npcName, skinValue, skinSignature);
    }

    public void delete(final NMSFakePlayer nmsFakePlayer) {
        nmsFakePlayer.hide();
        npcList.remove(nmsFakePlayer);
    }
    /*
    При выходе игрока очистка данных NPC в ОЗУ,
    потому-что теперь эти данные - мусор.
    */
    public void clearMemoryFor(final Player player) {
        npcList.removeIf(nmsFakePlayer -> nmsFakePlayer.getUuid().equals(player.getUniqueId()));
    }
    public void destroyEntityByID(final Player toPlayer, final int id) {
        PacketContainer entityDestroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        entityDestroyPacket.getIntegerArrays().write(0, new int[]{id});
        protocolManager.sendServerPacket(toPlayer, entityDestroyPacket);
    }

    /*
    Асинхронность - не проблема...
     */
    public void spawnNPCAsync(final Player toPlayer, final Location loc, final String npcName,
                              final String skinValue, final String skinSignature) {
        AsyncNMSScheduler.run(() -> spawnNPC(toPlayer, loc, npcName, skinValue, skinSignature));
    }
    public void spawnNPCSilentAsync(final Player toPlayer, final Location loc, final String npcName,
                              final String skinValue, final String skinSignature, final UUID uuid, final int id) {
        AsyncNMSScheduler.run(() -> spawnNPCSilent(toPlayer, loc, npcName, skinValue, skinSignature, uuid, id));
    }
    public void deleteAsync(final NMSFakePlayer nmsFakePlayer) {
        AsyncNMSScheduler.run(() -> delete(nmsFakePlayer));
    }
    public void destroyEntityByIDAsync(final Player toPlayer, final int id) {
        AsyncNMSScheduler.run(() -> destroyEntityByIDAsync(toPlayer, id));
    }
}
