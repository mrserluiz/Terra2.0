package br.com.mrserluiz.terra2.paper;

import java.nio.file.Path;
import org.bukkit.Bukkit;
import org.bukkit.World;

/** Resolves loaded dimensions through Paper, never by legacy _nether/_the_end suffixes. */
final class PaperWorldStorage {
    Path resolve(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) return world.getWorldFolder().toPath();
        // Creation of unloaded dimensions needs an explicit platform registration first.
        // Do not mistake an absent legacy folder for a new dimension and overwrite its save.
        throw new IllegalStateException("Dimension '" + worldName
                + "' is not loaded. Paper must resolve its storage before provisioning.");
    }
}
