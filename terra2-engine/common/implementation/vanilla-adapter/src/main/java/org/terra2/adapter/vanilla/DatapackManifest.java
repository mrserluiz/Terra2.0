package org.terra2.adapter.vanilla;

import com.google.gson.*;
import java.io.IOException;
import java.nio.file.*;

/** Saves/validates a source pin at the world path supplied by the platform. No inferred save layout. */
public record DatapackManifest(String world, String dimension, long seed, String plan, String selection,
                              String sourceDimension, int minY, int maxY) {
    public void verifyOrCreate(Path manifest) throws IOException {
        if(Files.isSymbolicLink(manifest)) throw new IOException("Manifest cannot be a symbolic link");
        JsonObject expected = new JsonObject();
        expected.addProperty("schema", 1); expected.addProperty("world", world);
        expected.addProperty("dimension", dimension); expected.addProperty("seed", seed);
        expected.addProperty("plan", plan); expected.addProperty("selection", selection);
        expected.addProperty("sourceDimension", sourceDimension); expected.addProperty("minecraft", "26.2");
        expected.addProperty("minY", minY); expected.addProperty("maxY", maxY);
        if(Files.exists(manifest)) {
            if(Files.size(manifest) > 16 * 1024) throw new IOException("World manifest exceeds 16 KiB");
            JsonElement actual = JsonParser.parseString(Files.readString(manifest));
            if(!expected.equals(actual)) throw new IllegalArgumentException("Existing world manifest differs; refusing generator change: " + world);
        } else {
            Files.writeString(manifest, expected.toString() + "\n", StandardOpenOption.CREATE_NEW);
        }
    }
}
