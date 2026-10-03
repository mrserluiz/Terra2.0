package org.terra2.core;
/** Chunk-local x/z, absolute y. State representation is chosen by the platform adapter. */
public interface BlockVolume<B> {
    void set(int x, int y, int z, B state);
    B get(int x, int y, int z);
    int minY();
    int maxY();
}
