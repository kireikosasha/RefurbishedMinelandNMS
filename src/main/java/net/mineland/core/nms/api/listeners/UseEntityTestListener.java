package net.mineland.core.nms.api.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.mineland.core.nms.MinelandNMSPlugin;
import net.mineland.core.nms.api.data.NMSFakePlayer;
import net.mineland.core.nms.api.data.NMSFakePlayerFinder;
import net.mineland.core.nms.services.NPCService;
import org.bukkit.entity.Player;

public class UseEntityTestListener extends PacketAdapter {
    public UseEntityTestListener() {
        super(MinelandNMSPlugin.getInstance(), ListenerPriority.HIGHEST,
                        PacketType.Play.Client.USE_ENTITY);
    }
    @Override
    public void onPacketReceiving(final PacketEvent event) {
        final Player player = event.getPlayer();
        final NMSFakePlayerFinder finder = new NMSFakePlayerFinder(player);
        final PacketContainer packet = event.getPacket();
        final boolean attack = !packet.getEntityUseActions().getValues().isEmpty() ?
        packet.getEntityUseActions().read(0).toString().equals("ATTACK")
                        : packet.getEnumEntityUseActions().read(0).getAction().equals(
                        EnumWrappers.EntityUseAction.ATTACK);
        if (packet.getIntegers().getValues().isEmpty()) {
            return;
        }
        final int entityId = packet.getIntegers().read(0);
        final NMSFakePlayer nmsFakePlayer = finder.findBy(entityId);
        if (nmsFakePlayer == null) {
            NPCService.destroyEntityByID(player, entityId);
            return;
        }
        final String name = nmsFakePlayer.getName();
        player.sendMessage("NMS FakePlayer action: " + entityId + " isAttack: " + attack);
        if (attack) {
            player.sendMessage("NPC " + name + " removed!");
            NPCService.delete(nmsFakePlayer);
        } else {
            final String newName = "Changed name";
            player.sendMessage("NPC " + name + " changed to " + newName);
            nmsFakePlayer.changeName(newName);
        }
        /*
        Entity entity = ProtocolLibrary.getProtocolManager().
                        getEntityFromID(event.getPlayer().getWorld(), entityId);
         */
    }
}
