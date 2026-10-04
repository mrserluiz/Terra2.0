import java.nio.file.*;
import java.util.*;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import com.dfsek.terra.bukkit.TerraBukkitPlugin;

/** Runs only in disposable CI servers. Never included in the released plugin. */
public final class NativeIntegrationProbe extends JavaPlugin {
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 1) return false;
        try {
            var engine = (TerraBukkitPlugin) getServer().getPluginManager().getPlugin("Terra2");
            if(engine == null || !engine.isEnabled()) throw new IllegalStateException("Engine did not enable");
            var world = WorldCreator.name("terra2_native_smoke").seed(42).environment(World.Environment.NORMAL)
                .generator("Terra2:PACKS").createWorld();
            if(world == null) throw new IllegalStateException("World creation failed");
            world.getChunkAt(0, 0); world.getChunkAt(1, 0); world.getChunkAt(0, 1);
            var checkpoint = getDataFolder().toPath().resolve("checkpoint.properties");
            Files.createDirectories(checkpoint.getParent());
            Properties data = new Properties();
            if(args[0].equals("create")) {
                boolean found = false;
                for(int x = -12; x < 32 && !found; x++) for(int z = -12; z < 32 && !found; z++) for(int y = 98; y < 106 && !found; y++) {
                    if(world.getBlockAt(x, y, z).getState() instanceof Chest chest && chest.getLootTable() != null
                        && chest.getLootTable().getKey().toString().startsWith("terra2:")) {
                        var items = chest.getBlockInventory().getContents();
                        var item = Arrays.stream(items).filter(Objects::nonNull).findFirst().orElseThrow();
                        if(!engine.lootManager().authentic(item)) throw new IllegalStateException("Loot origin not signed");
                        if(!"Smoke relic".equals(net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName())))
                            throw new IllegalStateException("Per-world loot name not applied");
                        if(!new NamespacedKey("smoke", "relic").equals(item.getItemMeta().getItemModel())) throw new IllegalStateException("Per-world item model not applied");
                        data.setProperty("chest", x + "," + y + "," + z); data.setProperty("item", item.getType().name()); found = true;
                    }
                }
                if(!found) throw new IllegalStateException("Generated native chest not found");
                long gold = 0;
                for(int x = 0; x < 32; x++) for(int z = 0; z < 32; z++) for(int y = 98; y < 106; y++) if(world.getBlockAt(x, y, z).getType() == Material.GOLD_BLOCK) gold++;
                if(gold < 18) throw new IllegalStateException("Jigsaw child/processor output missing: " + gold);
                data.setProperty("gold", Long.toString(gold)); data.setProperty("world", world.getUID().toString()); data.setProperty("dimension", world.getKey().toString());
                try(var output = Files.newOutputStream(checkpoint)) { data.store(output, "Native save/restart checkpoint"); }
                world.save(); getServer().savePlayers();
                sender.sendMessage("TERRA2_NATIVE_SMOKE_CREATED " + gold);
            } else if(args[0].equals("reload")) {
                try(var input = Files.newInputStream(checkpoint)) { data.load(input); }
                if(!data.getProperty("world").equals(world.getUID().toString()) || !data.getProperty("dimension").equals(world.getKey().toString()))
                    throw new IllegalStateException("World identity changed after restart");
                var xyz = Arrays.stream(data.getProperty("chest").split(",")).mapToInt(Integer::parseInt).toArray();
                var chest = (Chest) world.getBlockAt(xyz[0], xyz[1], xyz[2]).getState();
                var item = Arrays.stream(chest.getBlockInventory().getContents()).filter(Objects::nonNull).findFirst().orElseThrow();
                if(!engine.lootManager().authentic(item)) throw new IllegalStateException("Saved loot provenance failed after restart");
                // Force additional generation after restored registry/template aliases are installed.
                world.getChunkAt(2, 0); world.getChunkAt(2, 1); world.save();
                sender.sendMessage("TERRA2_NATIVE_SMOKE_RESTART_OK");
            } else return false;
        } catch(Throwable error) {
            error.printStackTrace(); sender.sendMessage("TERRA2_NATIVE_SMOKE_FAILED " + error); return true;
        }
        return true;
    }
}
