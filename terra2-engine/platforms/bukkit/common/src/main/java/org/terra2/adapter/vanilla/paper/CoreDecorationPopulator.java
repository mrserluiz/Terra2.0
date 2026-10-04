package org.terra2.adapter.vanilla.paper;
import java.util.Random;
import org.bukkit.generator.*;
import org.terra2.core.*;
import java.util.function.Function;

/** Runs compiled additive features after the world's existing population stages. */
public final class CoreDecorationPopulator<B, M> extends BlockPopulator {
    private final GenerationManager<B, M> manager;
    private final String world;
    private final Function<org.bukkit.block.data.BlockData, B> fromPaper;
    private final Function<B, org.bukkit.block.data.BlockData> toPaper;
    public CoreDecorationPopulator(GenerationManager<B, M> manager, String world,
        Function<org.bukkit.block.data.BlockData, B> fromPaper, Function<B, org.bukkit.block.data.BlockData> toPaper) {
        this.manager = manager; this.world = world; this.fromPaper = fromPaper; this.toPaper = toPaper;
    }
    public void populate(WorldInfo info, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        var context = new GenerationContext(manager.binding(world).target(), info.getSeed(), info.getMinHeight(), info.getMaxHeight(), null);
        try {
            manager.decorate(context, chunkX, chunkZ, new BlockVolume<>() {
                public int minY() { return info.getMinHeight(); }
                public int maxY() { return info.getMaxHeight(); }
                public B get(int x, int y, int z) { return fromPaper.apply(region.getBlockData(chunkX * 16 + x, y, chunkZ * 16 + z)); }
                public void set(int x, int y, int z, B state) { region.setBlockData(chunkX * 16 + x, y, chunkZ * 16 + z, toPaper.apply(state)); }
            });
        } catch(RuntimeException | LinkageError error) {
            com.dfsek.terra.bukkit.util.GenerationReport.failure("terrapack-decoration " + chunkX + "," + chunkZ,
                world, manager.binding(world).plan().identity(), error);
            throw error;
        }
    }
}
