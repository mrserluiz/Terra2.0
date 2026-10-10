package com.dfsek.terra.bukkit.util;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Bounded, per-save placement index. Generation threads only update memory. */
public final class StructureIndex {
    public static final int LIMIT = 100_000;
    public record Placement(String id, int x, int y, int z, String kind) {}
    private record Snapshot(int schema, UUID world, long seed, List<Placement> placements) {}
    private static final Gson JSON = new Gson();
    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();
    private static Path directory;
    private static final class State {
        final long seed;
        final Map<String, Placement> entries = new LinkedHashMap<>();
        long version, saved = -1;
        boolean loaded, full;
        State(long seed) { this.seed = seed; }
    }
    private StructureIndex() {}
    public static synchronized void configure(Path path) throws IOException {
        Files.createDirectories(path); directory = path; STATES.clear();
    }
    private static State state(UUID world, long seed) {
        return STATES.computeIfAbsent(world, key -> new State(seed));
    }
    public static void record(UUID world, long seed, String id, int x, int y, int z, String kind) {
        if(directory == null) return;
        var state = state(world, seed);
        synchronized(state) {
            if(state.seed != seed) return;
            var value = new Placement(id, x, y, z, kind);
            var key = id + ":" + x + ":" + y + ":" + z;
            if(state.entries.containsKey(key)) return;
            if(state.entries.size() >= LIMIT) { state.full = true; return; }
            state.entries.put(key, value); state.version++;
        }
    }
    /** Must run off the server and generation threads. Concurrent placements are preserved. */
    public static void load(UUID world, long seed) throws IOException {
        var state = state(world, seed);
        var file = directory.resolve(world + ".json");
        List<Placement> previous = List.of();
        if(Files.exists(file)) {
            if(Files.size(file) > 32_000_000) throw new IOException("Structure index exceeds file limit");
            try(var reader = Files.newBufferedReader(file)) {
                var snapshot = JSON.fromJson(reader, Snapshot.class);
                if(snapshot == null || snapshot.schema != 1 || !world.equals(snapshot.world) || seed != snapshot.seed
                    || snapshot.placements == null || snapshot.placements.size() > LIMIT)
                    throw new IOException("Invalid structure index identity/schema");
                previous = snapshot.placements;
            } catch(RuntimeException error) { throw new IOException("Invalid structure index", error); }
        }
        for(var placement : previous) {
            if(placement == null || placement.id == null || placement.kind == null) throw new IOException("Invalid placement");
            record(world, seed, placement.id, placement.x, placement.y, placement.z, placement.kind);
        }
        synchronized(state) { state.loaded = true; }
    }
    public static boolean ready(UUID world) {
        var state = STATES.get(world); if(state == null) return false;
        synchronized(state) { return state.loaded; }
    }
    public static boolean full(UUID world) {
        var state = STATES.get(world); if(state == null) return false;
        synchronized(state) { return state.full; }
    }
    public static List<Placement> entries(UUID world) {
        var state = STATES.get(world); if(state == null) return List.of();
        synchronized(state) { return List.copyOf(state.entries.values()); }
    }
    public static synchronized void flush() throws IOException {
        for(var entry : STATES.entrySet()) {
            var state = entry.getValue(); long version; List<Placement> placements;
            synchronized(state) {
                if(!state.loaded || state.saved == state.version) continue;
                version = state.version; placements = List.copyOf(state.entries.values());
            }
            var target = directory.resolve(entry.getKey() + ".json");
            var temporary = Files.createTempFile(directory, "index-", ".tmp");
            try {
                try(var writer = Files.newBufferedWriter(temporary)) {
                    JSON.toJson(new Snapshot(1, entry.getKey(), state.seed, placements), writer);
                }
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                synchronized(state) { state.saved = version; }
            } finally { Files.deleteIfExists(temporary); }
        }
    }
}
