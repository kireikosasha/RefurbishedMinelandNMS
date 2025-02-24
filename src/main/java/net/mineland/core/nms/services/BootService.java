package net.mineland.core.nms.services;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.experimental.UtilityClass;
import net.mineland.core.nms.MinelandNMSPlugin;
import net.mineland.core.nms.api.listeners.InteractTestListener;
import net.mineland.core.nms.api.listeners.MemoryCleaner;
import net.mineland.core.nms.api.listeners.UseEntityTestListener;
import org.bukkit.Bukkit;

@UtilityClass
public class BootService {
    public void enable(final MinelandNMSPlugin instance) {
        // Для тестов... Выключи на проде.
        if (true) {
            final ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            Bukkit.getPluginManager().registerEvents(new InteractTestListener(), instance);
            manager.addPacketListener(new UseEntityTestListener());
        }
        /*
        Для очистки мусора после выхода игрока.
        Не выключать! Не выключать!
         */
        Bukkit.getPluginManager().registerEvents(new MemoryCleaner(), instance);
    }
}
