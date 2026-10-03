package com.dfsek.terra.bukkit.nms;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper;
import com.dfsek.terra.bukkit.TerraBukkitPlugin;


public class NMSInjectListener implements Listener {
    private static final Logger LOGGER = LoggerFactory.getLogger(NMSInjectListener.class);
    private static final Set<World> INJECTED = new HashSet<>();
    private static final ReentrantLock INJECT_LOCK = new ReentrantLock();
    private final TerraBukkitPlugin plugin;

    public NMSInjectListener(TerraBukkitPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        if(!INJECTED.contains(event.getWorld()) &&
           event.getWorld().getGenerator() instanceof BukkitChunkGeneratorWrapper bukkitChunkGeneratorWrapper) {
            INJECT_LOCK.lock();
            try {
            if(INJECTED.contains(event.getWorld())) return;
            plugin.assertGenerationAuthorized(event.getWorld().getName(), bukkitChunkGeneratorWrapper.getPack().getID());
            LOGGER.info("Preparing to take over the world: {}", event.getWorld().getName());
            CraftWorld craftWorld = (CraftWorld) event.getWorld();
            ServerLevel serverWorld = craftWorld.getHandle();

            ConfigPack pack = bukkitChunkGeneratorWrapper.getPack();

            ChunkGenerator vanilla = serverWorld.getChunkSource().getGenerator();
            NMSBiomeProvider provider = new NMSBiomeProvider(pack.getBiomeProvider(), craftWorld.getSeed(),
                bukkitChunkGeneratorWrapper.getHandle() instanceof org.terra2.adapter.terra.CoreTerraGenerator core ? core : null,
                new com.dfsek.terra.bukkit.world.BukkitWorldProperties(craftWorld));
            ChunkMap chunkMap = serverWorld.getChunkSource().chunkMap;
            WorldGenContext worldGenContext = Reflection.CHUNKMAP.getWorldGenContext(chunkMap);
            Reflection.CHUNKMAP.setWorldGenContext(chunkMap, new WorldGenContext(
                worldGenContext.level(),
                new NMSChunkGeneratorDelegate(vanilla, pack, provider, craftWorld.getSeed(), bukkitChunkGeneratorWrapper.getHandle()),
                worldGenContext.structureManager(),
                worldGenContext.lightEngine(),
                worldGenContext.mainThreadExecutor(),
                worldGenContext.unsavedListener()
            ));

            LOGGER.info("Successfully injected into world.");
            INJECTED.add(event.getWorld());
            } catch(RuntimeException | LinkageError e) {
                com.dfsek.terra.bukkit.util.GenerationReport.failure("nms-world-init", event.getWorld().getName(),
                    bukkitChunkGeneratorWrapper.getPack().getID(), e);
                throw e;
            } finally {
            INJECT_LOCK.unlock();
            }
        }
    }
}
