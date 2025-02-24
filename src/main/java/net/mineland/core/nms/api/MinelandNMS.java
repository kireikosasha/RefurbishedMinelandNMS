package net.mineland.core.nms.api;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.mineland.core.nms.api.java.AsyncNMSScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class MinelandNMS {

    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private static final UUID bossFirstUUID = UUID.randomUUID();

    public static void sendPacketAsync(final Player player, final Object packet) {
        if (!(packet instanceof PacketContainer)) {
            throw new IllegalArgumentException("Packet must be a PacketContainer");
        }
        final PacketContainer packetContainer = (PacketContainer) packet;
        AsyncNMSScheduler.run(() -> protocolManager.sendServerPacket(player, packetContainer));
    }

    public static void sendPacketAsync(Collection<? extends Player> players, final Object packet) {
        if (!(packet instanceof PacketContainer)) {
            throw new IllegalArgumentException("Packet must be a PacketContainer");
        }
        final PacketContainer packetContainer = (PacketContainer) packet;
        AsyncNMSScheduler.run(() -> {
            for (final Player player : players)
                protocolManager.sendServerPacket(player, packetContainer);
        });
    }

    public static void sendPacketSync(final Player player, final Object packet) {
        if (!(packet instanceof PacketContainer)) {
            throw new IllegalArgumentException("Packet must be a PacketContainer");
        }
        protocolManager.sendServerPacket(player, (PacketContainer) packet);
    }

    public static void sendAddBossBar(final Collection<Player> players, final String text,
                               final BarColor barColor, final BarStyle barStyle, final float value) {
        final PacketContainer bossPacket = protocolManager.createPacket(PacketType.Play.Server.BOSS);
        bossPacket.getIntegers().write(0, 0);
        bossPacket.getSpecificModifier(UUID.class).write(0, bossFirstUUID);
        bossPacket.getChatComponents().write(0, WrappedChatComponent.fromText(text));
        bossPacket.getIntegers().write(1, barColor.ordinal());
        bossPacket.getIntegers().write(2, barStyle.ordinal());
        bossPacket.getFloat().write(0, value);
        sendPacketAsync(players, bossPacket);
    }
    @SneakyThrows
    public static void sendCustomPayload(final Player player, final String channel, final ByteBuf byteBuf) {
        /*
        Как же я заебался...
        Как же тут много ебанины.
         */
        final PacketContainer customPayload = protocolManager.createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
        customPayload.getStrings().write(0, channel);
        final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        final String serializerClassName = "net.minecraft.server." + version + ".PacketDataSerializer";
        final Class<?> serializerClass = Class.forName(serializerClassName);
        final Constructor<?> constructor = serializerClass.getDeclaredConstructor(ByteBuf.class);
        constructor.setAccessible(true);
        byteBuf.writeByte(0);
        final Object serializer = constructor.newInstance(byteBuf);

        @SuppressWarnings("unchecked") // молчать
        final StructureModifier<Object> modifier = (StructureModifier<Object>)
                        customPayload.getSpecificModifier(serializerClass);
        modifier.write(0, serializer);
        sendPacketAsync(player, customPayload);
    }
    public static void sendCustomPayload(final Player player, final String channel, final byte[] message) {
        sendCustomPayload(player, channel, Unpooled.wrappedBuffer(message));
    }

    public static PacketContainer getFakeEntityEquipmentPacket(final int entityId, final EnumWrappers.ItemSlot itemSlot,
                                                        final org.bukkit.inventory.ItemStack itemStack){
        final PacketContainer packetContainer = protocolManager
                        .createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packetContainer.getIntegers().write(0, entityId);
        packetContainer.getItemSlots().write(0, itemSlot);
        packetContainer.getItemModifier().write(0, itemStack);
        return packetContainer;
    }
    public static void sendMount(final Player player, final Entity entity, final int... passengerIds) {
        final PacketContainer mountPacket = protocolManager.createPacket(PacketType.Play.Server.MOUNT);
        mountPacket.getIntegers().write(0, entity.getEntityId());
        mountPacket.getIntegerArrays().write(0, passengerIds);
        sendPacketAsync(player, mountPacket);
    }
    public static void sendMount(final Collection<Player> view, final Entity entity, final int... passengerIds) {
        final PacketContainer mountPacket = protocolManager.createPacket(PacketType.Play.Server.MOUNT);
        mountPacket.getIntegers().write(0, entity.getEntityId());
        mountPacket.getIntegerArrays().write(0, passengerIds);
        sendPacketAsync(view, mountPacket);
    }
    public static void sendActionBar(final Player player, final String message) {
        final PacketContainer actionBarPacket = protocolManager.createPacket(PacketType.Play.Server.CHAT);
        actionBarPacket.getChatComponents().write(0, WrappedChatComponent.fromText(message));
        actionBarPacket.getIntegers().write(0, 2);
        sendPacketAsync(player, actionBarPacket);
    }
    public static void sendBlockBreakEffect(final Player from, final Collection<? extends Player> view, final Location location) {
        final PacketContainer effectPacket = protocolManager.createPacket(PacketType.Play.Server.WORLD_EVENT);
        effectPacket.getIntegers().write(0, 2001);
        effectPacket.getIntegers().write(1, getBlockCombinedId(location));
        effectPacket.getIntegers().write(2, location.getBlockX());
        effectPacket.getIntegers().write(3, location.getBlockY());
        effectPacket.getIntegers().write(4, location.getBlockZ());
        sendPacketAsync(view, effectPacket);
    }
    public static void sendPlayerUseBed(final Collection<Player> players,
                                        final Entity playerEntity, final Location bedLocation) {
        final PacketContainer bedPacket = protocolManager.createPacket(PacketType.Play.Server.BED);
        bedPacket.getIntegers().write(0, playerEntity.getEntityId());
        bedPacket.getDoubles().write(0, bedLocation.getX());
        bedPacket.getDoubles().write(1, bedLocation.getY());
        bedPacket.getDoubles().write(2, bedLocation.getZ());
        sendPacketAsync(players, bedPacket);
    }
    public static void sendBlockChange(final Player player, final Location location,
                                       final Material blockType) {
        final PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.BLOCK_CHANGE);
        packet.getBlockPositionModifier().write(0, new com.comphenix.protocol.wrappers.BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        packet.getBlockData().write(0, WrappedBlockData.createData(blockType));
        sendPacketAsync(player, packet);
    }

    public void sendMetadataEntity(final Collection<Player> players, final Entity entity, final List<WrappedDataValue> metadata) {
        final PacketContainer metadataPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        metadataPacket.getIntegers().write(0, entity.getEntityId());
        metadataPacket.getDataValueCollectionModifier().write(0, metadata);
        sendPacketAsync(players, metadataPacket);
    }
    public static void sendFakeExplosion(final Collection<Player> players,
                                         final double x, final double y, final double z, final float power,
                                         final List<BlockPosition> particleTable) {
        final PacketContainer packet = new PacketContainer(PacketType.Play.Server.EXPLOSION);
        packet.getDoubles().write(0, x).write(1, y).write(2, z);
        packet.getFloat().write(0, power); // Float.MAX_VALUE :3
        packet.getBlockPositionCollectionModifier().write(0, particleTable);
        sendPacketAsync(players, packet);
    }
    public static void resetTeam(final Collection<Player> players, final String name) {
        final PacketContainer teamReset = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        teamReset.getStrings().write(0, name);
        teamReset.getIntegers().write(0, 1);
        sendPacketAsync(players, teamReset);
    }
    public static int getBlockCombinedId(final Location location) {
        return location.getWorld().getBlockAt(location).getType().getId();
    }
}
