package me.aris.arisrollback;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.concurrent.TimeUnit;

public class ArisRollback extends JavaPlugin {
    private RollbackManager manager;

    @Override
    public void onEnable() {
        manager = new RollbackManager(this);
        getServer().getPluginManager().registerEvents(new RollbackListener(manager), this);
        getServer().getAsyncScheduler().runAtFixedRate(this, task -> {
            for (Player p : getServer().getOnlinePlayers()) {
                p.getScheduler().run(this, t -> manager.saveAtomic(p), null);
            }
        }, 15, 15, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        for (Player p : getServer().getOnlinePlayers()) {
            manager.saveAtomic(p);
        }
    }
}
