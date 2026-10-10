package com.dfsek.terra.bukkit.util;

import java.util.function.BiFunction;

/** Bounded sampling: no chunk loads, and no claim of exact nearest-block distance. */
public final class BiomeSearch {
    public record Result(int x, int z, int samples, boolean limited) {}
    public static Result find(int x, int z, int radius, String id, BiFunction<Integer, Integer, String> query) {
        if(radius < 32 || radius > 8192) throw new IllegalArgumentException("radius: 32..8192");
        long deadline = System.nanoTime() + 2_000_000_000L;
        int samples = 0;
        for(int ring = 0; ring <= radius; ring += 32) {
            for(int dx = -ring; dx <= ring; dx += 32) {
                for(int dz = -ring; dz <= ring; dz += 32) {
                    if(Math.abs(dx) != ring && Math.abs(dz) != ring) continue;
                    if(++samples > 16_384 || System.nanoTime() > deadline || Thread.currentThread().isInterrupted())
                        return new Result(x, z, samples - 1, true);
                    if(id.equalsIgnoreCase(query.apply(x + dx, z + dz))) return new Result(x + dx, z + dz, samples, false);
                }
            }
        }
        return new Result(x, z, -samples, false);
    }
    private BiomeSearch() {}
}
