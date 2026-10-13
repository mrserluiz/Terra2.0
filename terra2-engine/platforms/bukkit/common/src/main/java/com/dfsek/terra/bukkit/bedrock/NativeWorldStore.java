package com.dfsek.terra.bukkit.bedrock;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;

/** Explicit, owned authorization for Bedrock rendering; separate from generation settings. */
public final class NativeWorldStore {
    public record Binding(UUID uuid,long seed,String name,String fingerprint) {}
    private final Path path;private final Map<UUID,Binding> bindings=new LinkedHashMap<>();
    public NativeWorldStore(Path path) throws java.io.IOException {
        this.path=path;
        if(Files.isSymbolicLink(path))throw new java.io.IOException("Refusing native-world store symlink");
        if(!Files.exists(path))return;
        if(Files.size(path)>65536)throw new java.io.IOException("Native-world store too large");
        try {
            var root=JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            if(root.get("terra2_native_worlds_schema").getAsInt()!=1)throw new IllegalArgumentException("Unknown schema");
            for(var item:root.getAsJsonArray("worlds")) {
                var obj=item.getAsJsonObject();var b=new Binding(UUID.fromString(obj.get("uuid").getAsString()),obj.get("seed").getAsLong(),obj.get("name").getAsString(),obj.get("fingerprint").getAsString());
                if(bindings.putIfAbsent(b.uuid(),b)!=null || bindings.size()>128)throw new IllegalArgumentException("Duplicate/oversized world catalog");
            }
        }catch(RuntimeException invalid){throw new java.io.IOException("Invalid native-world store; preserve the original",invalid);}
    }
    public boolean matches(UUID uuid,long seed,String fingerprint) {var b=bindings.get(uuid);return b!=null&&b.seed()==seed&&b.fingerprint().equals(fingerprint);}
    public int size(){return bindings.size();}
    public void enable(Binding binding) throws java.io.IOException {
        var updated=new LinkedHashMap<>(bindings);updated.put(binding.uuid(),binding);if(updated.size()>128)throw new java.io.IOException("Native world limit: 128");write(updated);
    }
    public void forget(UUID uuid) throws java.io.IOException {var updated=new LinkedHashMap<>(bindings);updated.remove(uuid);write(updated);}
    public void clear() throws java.io.IOException {write(Map.of());}
    private void write(Map<UUID,Binding> updated) throws java.io.IOException {
        if(Files.isSymbolicLink(path))throw new java.io.IOException("Refusing native-world store symlink");
        var root=new JsonObject();root.addProperty("terra2_native_worlds_schema",1);var worlds=new JsonArray();
        updated.values().stream().sorted(Comparator.comparing(b->b.uuid().toString())).forEach(b->{
            var obj=new JsonObject();obj.addProperty("uuid",b.uuid().toString());obj.addProperty("seed",b.seed());obj.addProperty("name",b.name());obj.addProperty("fingerprint",b.fingerprint());worlds.add(obj);
        });root.add("worlds",worlds);
        Files.createDirectories(path.getParent());var temp=Files.createTempFile(path.getParent(),"native-worlds-",".tmp");
        try{Files.writeString(temp,new GsonBuilder().setPrettyPrinting().create().toJson(root));Files.move(temp,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
        finally{Files.deleteIfExists(temp);}bindings.clear();bindings.putAll(updated);
    }
}
