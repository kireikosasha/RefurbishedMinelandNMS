package net.mineland.core.nms.api.listeners;

import net.mineland.core.nms.api.MinelandNMS;
import net.mineland.core.nms.services.BookGuiService;
import net.mineland.core.nms.services.NPCService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InteractTestListener implements Listener {
    @EventHandler
    public void npc(final PlayerInteractEvent event) {
        event.getPlayer().sendMessage(event.getAction().name());
        if (event.getItem() == null || event.getItem().getData() == null) return;
        final Player player = event.getPlayer();
        // Проверяем спавн NPC, можно спавнить палкой.
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            final Location testLocation = event.getClickedBlock().getLocation().clone().add(0, 1, 0);
            if (event.getItem().getData().getItemType().name().contains("STICK")) {
                player.sendMessage("NPC Spawned!");
                testLocation.setYaw((float) (Math.random() * 360));
                NPCService.spawnNPCAsync(
                                player, testLocation,
                                "Test " + new Random().nextInt(Integer.MAX_VALUE),
                                "/", "/"
                );
            } else if (event.getItem().getData().getItemType().name().contains("FIRE")) {
                player.sendMessage("Fake expo test!");
                MinelandNMS.sendFakeExplosion(List.of(player), testLocation.getX(),
                                testLocation.getY(), testLocation.getZ(), 1f, new ArrayList<>());
            }
        } else if (event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            if (event.getItem().getData().getItemType().name().contains("BOOK")) {
                player.sendMessage("BookGUI test!");
                BookGuiService.openBookGui(player,
                                BookGuiService.createInteractiveBook(
                                                "Test book!",
                                                "Notch",
                                                List.of("§lВыбери квест!\n§e[Нажми для старта]", "§lПолучить награду!\n§e[Нажми для квеста]"),
                                                List.of("/startQuest", "/claimReward")
                                ));
            }
        }
    }
}
