import java.nio.file.*;
import java.util.*;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import com.dfsek.terra.bukkit.TerraBukkitPlugin;

/** Runs only in disposable CI servers. Never included in the released plugin. */
public final class NativeIntegrationProbe extends JavaPlugin {
    private long nativeStarts(World world, int x, int z) {
        var level = ((org.bukkit.craftbukkit.CraftWorld) world).getHandle();
        var registry = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
        return level.getChunk(x, z).getAllStarts().entrySet().stream().filter(entry -> entry.getValue().isValid())
            .filter(entry -> registry.getKey(entry.getKey()).getNamespace().equals("terra2")).count();
    }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length != 1) return false;
        try {
            var engine = (TerraBukkitPlugin) getServer().getPluginManager().getPlugin("Terra2");
            if(engine == null || !engine.isEnabled()) throw new IllegalStateException("Engine did not enable");
            // Community IDs must come from manifests, including an archive renamed in CI.
            for(String id : List.of("OVERWORLD", "TARTARUS", "HYDRAXIA")) {
                engine.platform().getRawConfigRegistry().assertSourceAvailable(id);
                if(engine.platform().getConfigRegistry().getByID(id).isEmpty()) throw new IllegalStateException("Community regression: " + id);
            }
            if(args[0].equals("create")) {
                var future = engine.getDataFolder().toPath().resolve("packs/new-after-start");
                Files.createDirectories(future); Files.writeString(future.resolve("pack.yml"), "id: FUTURE_COMMUNITY\nversion: 1.0.0\n");
                engine.reloadGenerationSettings();
                boolean restartRequired = false;
                try { engine.platform().getRawConfigRegistry().assertSourceAvailable("FUTURE_COMMUNITY"); }
                catch(IllegalArgumentException expected) { restartRequired = expected.getMessage().contains("server restart required"); }
                if(!restartRequired) throw new IllegalStateException("Reload falsely activated new Community Pack");
                // Remove metadata-only fixture before the restart phase's real pack loader runs.
                Files.delete(future.resolve("pack.yml")); Files.delete(future);
            }
            if(args[0].equals("create")) {
                var settingsPath = engine.getDataFolder().toPath().resolve("terra2-settings.yml");
                String originalSettings = Files.readString(settingsPath);
                for(String forbidden : List.of("world", "world_nether", "world_the_end", "nether", "end", "WORLD")) {
                    boolean rejected = false;
                    try { engine.unlockGenerationWorld(forbidden, "OVERWORLD"); }
                    catch(Exception expected) { rejected = true; }
                    if(!rejected) throw new IllegalStateException("unlock allowed base world: " + forbidden);
                }
                boolean unknownRejected = false;
                try { engine.unlockGenerationWorld("terra2_command_invalid", "MISSING_TERRA2_PROBE_ID"); }
                catch(Exception expected) { unknownRejected = true; }
                if(!unknownRejected || !originalSettings.equals(Files.readString(settingsPath)))
                    throw new IllegalStateException("Refused unlock modified settings");
                if(!"OVERWORLD".equals(engine.generationSettings().getString("worlds.terra2_command_smoke.pack")))
                    throw new IllegalStateException("unlock command failed to persist authorization");
                var suggestions = engine.onTabComplete(Bukkit.getConsoleSender(), engine.getCommand("terra2"), "terra2",
                    new String[]{"unlock", "terra2_command_smoke", "OVER"});
                if(!suggestions.contains("OVERWORLD")) throw new IllegalStateException("Pack completion missing");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "terra2 lis Cpack");
                getLogger().info("TERRA2_COMMANDS_UNLOCK_AND_COMPLETION_OK");
            } else if(!"OVERWORLD".equals(engine.generationSettings().getString("worlds.terra2_command_smoke.pack"))) {
                throw new IllegalStateException("Command authorization missing after restart");
            }
            boolean protectedWorld = false;
            try { engine.assertGenerationAuthorized("world", "PACKS"); }
            catch(RuntimeException expected) { protectedWorld = true; }
            if(!protectedWorld) throw new IllegalStateException("Primary world authorization was not blocked");
            var world = WorldCreator.name("terra2_native_smoke").seed(42).environment(World.Environment.NORMAL)
                .generator("Terra2:PACKS").createWorld();
            if(world == null) throw new IllegalStateException("World creation failed");
            world.getChunkAt(0, 0); world.getChunkAt(1, 0); world.getChunkAt(0, 1);
            if(args[0].equals("create")) {
                String before = Files.readString(engine.getDataFolder().toPath().resolve("terra2-settings.yml"));
                boolean rejected = false;
                try { engine.unlockGenerationWorld(world.getName(), "TARTARUS"); }
                catch(Exception expected) { rejected = true; }
                if(!rejected || !before.equals(Files.readString(engine.getDataFolder().toPath().resolve("terra2-settings.yml"))))
                    throw new IllegalStateException("Live generator replacement was not refused transactionally");
            }

            var hydraxia = WorldCreator.name("terra2_hydraxia_smoke").seed(42).environment(World.Environment.NORMAL)
                .generator("Terra2:HYDRAXIA").createWorld();
            if(hydraxia == null) throw new IllegalStateException("HYDRAXIA world creation failed");
            hydraxia.getChunkAt(args[0].equals("create") ? 0 : 2, 0);
            hydraxia.save();
            getLogger().info("TERRA2_HYDRAXIA_GENERATION_OK " + args[0]);
            var checkpoint = getDataFolder().toPath().resolve("checkpoint.properties");
            Files.createDirectories(checkpoint.getParent());
            Properties data = new Properties();
            if(args[0].equals("create")) {
                boolean found = false;
                for(int x = -12; x < 32 && !found; x++) for(int z = -12; z < 32 && !found; z++) for(int y = 98; y < 106 && !found; y++) {
                    if(world.getBlockAt(x, y, z).getState() instanceof Chest chest && chest.getLootTable() != null
                        && chest.getLootTable().getKey().toString().endsWith("/smoke/chest")) {
                        // Reading the backing item list does not open a Minecraft loot container.
                        var level = ((org.bukkit.craftbukkit.CraftWorld) world).getHandle();
                        ((net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity) level.getBlockEntity(
                            new net.minecraft.core.BlockPos(x, y, z))).unpackLootTable(null);
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
                long gold = 0, childMarker = 0;
                for(int x = 0; x < 32; x++) for(int z = 0; z < 32; z++) for(int y = 98; y < 106; y++) {
                    var type = world.getBlockAt(x, y, z).getType();
                    if(type == Material.GOLD_BLOCK) gold++;
                    if(type == Material.EMERALD_BLOCK) childMarker++;
                }
                if(gold < 18) throw new IllegalStateException("Jigsaw child/processor output missing: " + gold);
                if(childMarker < 1) throw new IllegalStateException("Jigsaw child template not assembled");
                long starts = nativeStarts(world, 0, 0);
                if(starts < 1) throw new IllegalStateException("Native structure-start record missing");
                data.setProperty("starts", Long.toString(starts));
                if(nativeStarts(getServer().getWorld("world"), 8, 8) != 0) throw new IllegalStateException("Native structures activated in primary world");
                String table = engine.platform().nativeLootTables(world.getName()).keySet().stream()
                    .filter(key -> key.endsWith("/smoke/mob")).findFirst().orElseThrow();
                var mob = world.spawn(new Location(world, 8, 110, 8), org.bukkit.entity.Zombie.class);
                mob.setCanPickupItems(false); mob.setLootTable(Bukkit.getLootTable(NamespacedKey.fromString(table))); mob.setHealth(0);
                var drop = world.getEntitiesByClass(org.bukkit.entity.Item.class).stream()
                    .filter(entity -> engine.lootManager().authentic(entity.getItemStack()))
                    .filter(entity -> "Smoke drop".equals(net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                        .serialize(entity.getItemStack().getItemMeta().displayName()))).findFirst().orElseThrow();
                var stack = drop.getItemStack().clone();
                var transfer = getServer().getWorld("world").dropItem(new Location(getServer().getWorld("world"), 8, 100, 8), stack.clone());
                if(!transfer.getItemStack().isSimilar(stack)) throw new IllegalStateException("Cross-world item drop was rewritten");
                transfer.remove(); drop.remove();
                var savedChest = Arrays.stream(data.getProperty("chest").split(",")).mapToInt(Integer::parseInt).toArray();
                ((Chest) world.getBlockAt(savedChest[0], savedChest[1], savedChest[2]).getState()).getBlockInventory().addItem(stack);
                data.setProperty("gold", Long.toString(gold)); data.setProperty("world", world.getUID().toString()); data.setProperty("dimension", world.getKey().toString());
                try(var output = Files.newOutputStream(checkpoint)) { data.store(output, "Native save/restart checkpoint"); }
                world.save(); getServer().savePlayers();
                sender.sendMessage("TERRA2_NATIVE_SMOKE_CREATED " + gold);
            } else if(args[0].equals("reload")) {
                try(var input = Files.newInputStream(checkpoint)) { data.load(input); }
                if(!data.getProperty("world").equals(world.getUID().toString()) || !data.getProperty("dimension").equals(world.getKey().toString()))
                    throw new IllegalStateException("World identity changed after restart");
                if(nativeStarts(world, 0, 0) != Long.parseLong(data.getProperty("starts")))
                    throw new IllegalStateException("Native structure starts did not survive restart");
                var xyz = Arrays.stream(data.getProperty("chest").split(",")).mapToInt(Integer::parseInt).toArray();
                var chest = (Chest) world.getBlockAt(xyz[0], xyz[1], xyz[2]).getState();
                var item = Arrays.stream(chest.getBlockInventory().getContents()).filter(Objects::nonNull).findFirst().orElseThrow();
                if(!engine.lootManager().authentic(item)) throw new IllegalStateException("Saved loot provenance failed after restart");
                boolean mobLoot = Arrays.stream(chest.getBlockInventory().getContents()).filter(Objects::nonNull)
                    .filter(engine.lootManager()::authentic).anyMatch(stack -> "Smoke drop".equals(
                        net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(stack.getItemMeta().displayName())));
                if(!mobLoot) throw new IllegalStateException("Saved entity-table loot provenance missing after restart");
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
