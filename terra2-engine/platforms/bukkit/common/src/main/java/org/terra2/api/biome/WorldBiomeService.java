package org.terra2.api.biome;
import java.util.Optional;
import org.bukkit.World;
/** Read persisted Terra2 identities. Never loads or regenerates chunks. Call on the owning world thread. */
public interface WorldBiomeService {
    int apiVersion();
    Optional<BiomeIdentity> at(World world, int x, int y, int z);
}
