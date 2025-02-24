package net.mineland.core.nms;

import lombok.Getter;
import net.mineland.core.nms.services.BootService;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class MinelandNMSPlugin extends JavaPlugin {

    @Getter
    private static MinelandNMSPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        BootService.enable(this);
    }
}
