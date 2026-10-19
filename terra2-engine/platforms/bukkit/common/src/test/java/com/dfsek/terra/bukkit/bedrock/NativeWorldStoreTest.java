package com.dfsek.terra.bukkit.bedrock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class NativeWorldStoreTest {
    @TempDir Path folder;
    @Test void persistsIdentityWithoutAuthorizingRecreatedWorlds() throws Exception {
        var file=folder.resolve("native-worlds.json");UUID uuid=UUID.randomUUID();
        var store=new NativeWorldStore(file);assertEquals(0,store.size());
        store.enable(new NativeWorldStore.Binding(uuid,42,"test","pack-fingerprint"));
        var restart=new NativeWorldStore(file);assertTrue(restart.matches(uuid,42,"pack-fingerprint"));
        assertFalse(restart.matches(UUID.randomUUID(),42,"pack-fingerprint"));assertFalse(restart.matches(uuid,43,"pack-fingerprint"));
        assertFalse(restart.matches(uuid,42,"another-pack"));restart.forget(uuid);assertEquals(0,new NativeWorldStore(file).size());
    }
    @Test void explicitForgetPersistsAsAutomaticActivationExclusion() throws Exception {
        var file=folder.resolve("native-worlds.json");var id=UUID.randomUUID();var store=new NativeWorldStore(file);
        store.forget(id);assertTrue(new NativeWorldStore(file).excluded(id));
        store.enable(new NativeWorldStore.Binding(id,42,"test","fp"));
        assertFalse(new NativeWorldStore(file).excluded(id));assertTrue(new NativeWorldStore(file).matches(id,42,"fp"));
    }
    @Test void refusesUnownedStore() throws Exception {
        var file=folder.resolve("native-worlds.json");Files.writeString(file,"{\"operator\":true}");
        assertThrows(java.io.IOException.class,()->new NativeWorldStore(file));assertEquals("{\"operator\":true}",Files.readString(file));
    }
}
