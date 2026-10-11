package com.dfsek.terra.bukkit.compatibility;
import java.nio.file.*;
import java.util.*;
/** A mode transition must never silently reinterpret an existing world's chunks. */
final class BiomeWorldManifest {
    private BiomeWorldManifest() { }
    static void bind(Path folder, UUID uuid, long seed, boolean vanilla, String fingerprint, String name) throws java.io.IOException {
        Path manifest=folder.resolve("terra2-biomes-"+uuid+".properties");
        var expected=new Properties();expected.setProperty("schema","1");expected.setProperty("mode",vanilla?"VANILLA_COMPATIBLE":"CUSTOM");
        expected.setProperty("world",uuid.toString());expected.setProperty("seed",Long.toString(seed));expected.setProperty("fingerprint",fingerprint);
        if(Files.exists(manifest)) {
            var saved=new Properties();try(var in=Files.newInputStream(manifest)){saved.load(in);}
            if(!saved.equals(expected))throw new java.io.IOException("World biome mode/pack/reference manifest mismatch; refusing to mix generations: "+name);
            return;
        }
        if(!vanilla)return;
        Path region=folder.resolve("region");
        if(Files.isDirectory(region))try(var files=Files.list(region)) {
            if(files.anyMatch(p->p.getFileName().toString().endsWith(".mca")))
                throw new java.io.IOException("VANILLA_COMPATIBLE is for new worlds only: "+name);
        }
        Files.createDirectories(folder);
        Path temp=Files.createTempFile(folder,"terra2-biomes-", ".tmp");
        try {
            try(var out=Files.newOutputStream(temp)){expected.store(out,"Terra2 biome projection - do not edit");}
            Files.move(temp,manifest,StandardCopyOption.ATOMIC_MOVE);
        }finally{Files.deleteIfExists(temp);}
    }
}
