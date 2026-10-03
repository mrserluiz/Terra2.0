package org.terra2.core;
/** A compiled source supplies generation behavior to the manager. Implementations must be thread safe. */
public interface GenerationProgram<B, M> {
    void generate(GenerationContext context, int chunkX, int chunkZ, BlockVolume<B> output);
    B blockAt(GenerationContext context, int x, int y, int z);
    M biomeAt(GenerationContext context, int x, int y, int z);
}
