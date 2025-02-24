package net.mineland.core.nms.api.listeners;

import net.mineland.core.nms.services.NPCService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class MemoryCleaner implements Listener {
    @EventHandler
    public void npc(final PlayerQuitEvent event) {
        NPCService.clearMemoryFor(event.getPlayer());
    }
}
