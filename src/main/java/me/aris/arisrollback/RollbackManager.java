package me.aris.arisrollback;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class RollbackManager {
    private final ArisRollback plugin;
    private final File dataFolder;

    public RollbackManager(ArisRollback plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!this.dataFolder.exists()) this.dataFolder.mkdirs();
    }

    public void saveAtomic(Player player) {
        String uuid = player.getUniqueId().toString();
        String inv = InventoryBase64.toBase64(player.getInventory().getContents());
        String ec = InventoryBase64.toBase64(player.getEnderChest().getContents());
        
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try {
                File tmp = new File(dataFolder, uuid + ".tmp");
                File real = new File(dataFolder, uuid + ".dat");
                Properties props = new Properties();
                props.setProperty("inv", inv);
                props.setProperty("ec", ec);
                
                try (FileOutputStream out = new FileOutputStream(tmp)) {
                    props.store(out, null);
                }
                Files.move(tmp.toPath(), real.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ignored) {}
        });
    }

    public void restore(Player player) {
        String uuid = player.getUniqueId().toString();
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            File real = new File(dataFolder, uuid + ".dat");
            if (!real.exists()) return;
            try {
                Properties props = new Properties();
                try (FileInputStream in = new FileInputStream(real)) {
                    props.load(in);
                }
                ItemStack[] inv = InventoryBase64.fromBase64(props.getProperty("inv"));
                ItemStack[] ec = InventoryBase64.fromBase64(props.getProperty("ec"));
                
                player.getScheduler().run(plugin, t -> {
                    player.getInventory().setContents(inv);
                    player.getEnderChest().setContents(ec);
                }, null);
            } catch (Exception ignored) {}
        });
    }
                  }
