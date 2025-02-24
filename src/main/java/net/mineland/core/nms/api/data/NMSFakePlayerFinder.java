package net.mineland.core.nms.api.data;

import lombok.AllArgsConstructor;
import net.mineland.core.nms.services.NPCService;
import org.bukkit.entity.Player;

import java.util.UUID;

@AllArgsConstructor
public class NMSFakePlayerFinder {
    private final Player player;
    public NMSFakePlayer findBy(final UUID uuid) {
        for (final NMSFakePlayer n : NPCService.getNpcList())
            if (n.getUuid().equals(uuid) && compare(n.getToPlayer(), player))
                return n;
        return null;
    }
    public NMSFakePlayer findBy(final int id) {
        for (final NMSFakePlayer n : NPCService.getNpcList())
            if (n.getId() == id && compare(n.getToPlayer(), player))
                return n;
        return null;
    }
    public NMSFakePlayer findBy(final String name) {
        for (final NMSFakePlayer n : NPCService.getNpcList())
            if (n.getName().equals(name) && compare(n.getToPlayer(), player))
                return n;
        return null;
    }
    private static boolean compare(Player p, Player p2) {
        return p.getUniqueId().equals(p2.getUniqueId());
    }
}
