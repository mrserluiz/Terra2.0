package registry;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.dfsek.terra.api.Platform;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.config.PackSourceAdapter;
import com.dfsek.terra.api.registry.key.RegistryKey;
import com.dfsek.terra.registry.master.ConfigRegistry;
import static org.junit.jupiter.api.Assertions.*;

class PackSourceAdapterTest {
    @TempDir Path directory;

    private Platform platform() {
        return (Platform) Proxy.newProxyInstance(Platform.class.getClassLoader(), new Class<?>[]{Platform.class}, (proxy, method, args) -> {
            if(method.getName().equals("getDataFolder")) return directory.toFile();
            throw new AssertionError("Pack loading must not access worlds: " + method.getName());
        });
    }

    private PackSourceAdapter adapter(String id, AtomicInteger calls, ConfigPack pack) {
        return new PackSourceAdapter() {
            public String id() { return id; }
            public boolean supports(Path source) { return source.getFileName().toString().equals("fixture"); }
            public ConfigPack load(Path source, Platform platform) { calls.incrementAndGet(); return pack; }
        };
    }

    @Test void externalPackEntersTheSameRegistryWithoutWorldAccess() throws Exception {
        Files.createDirectories(directory.resolve("packs/fixture"));
        RegistryKey key = RegistryKey.parse("terra2:fixture");
        ConfigPack pack = (ConfigPack) Proxy.newProxyInstance(ConfigPack.class.getClassLoader(), new Class<?>[]{ConfigPack.class},
            (proxy, method, args) -> {
                if(method.getName().equals("getRegistryKey")) return key;
                if(method.getName().equals("getID")) return "fixture";
                throw new AssertionError("Unexpected pack call: " + method.getName());
            });
        AtomicInteger calls = new AtomicInteger();
        ConfigRegistry registry = new ConfigRegistry();
        registry.registerSourceAdapter(adapter("terra2:fixture", calls, pack));
        registry.loadAll(platform());
        assertEquals(1, calls.get());
        assertSame(pack, registry.get(key).orElseThrow());
    }

    @Test void reloadDiscoversNewPackWithoutReplacingLiveRegistry() throws Exception {
        Files.createDirectories(directory.resolve("packs/fixture"));
        RegistryKey key = RegistryKey.parse("terra2:fixture");
        ConfigPack pack = (ConfigPack) Proxy.newProxyInstance(ConfigPack.class.getClassLoader(), new Class<?>[]{ConfigPack.class},
            (proxy, method, args) -> {
                if(method.getName().equals("getRegistryKey")) return key;
                if(method.getName().equals("getID")) return "fixture";
                throw new AssertionError("Unexpected pack call: " + method.getName());
            });
        AtomicInteger calls = new AtomicInteger();
        ConfigRegistry registry = new ConfigRegistry(); registry.registerSourceAdapter(adapter("terra2:fixture", calls, pack));
        registry.loadAll(platform());
        Path added = Files.createDirectories(directory.resolve("packs/renamed"));
        Files.writeString(added.resolve("pack.yml"), "id: NEW_COMMUNITY\nversion: 1.0.0\n");
        registry.discoverForReload(platform());
        assertEquals(1, calls.get()); assertSame(pack, registry.get(key).orElseThrow());
        assertTrue(registry.discoveredSources().stream().anyMatch(source -> source.matches("fixture") && source.status().equals("REGISTERED")));
        var failure = assertThrows(IllegalArgumentException.class, () -> registry.assertSourceAvailable("NEW_COMMUNITY"));
        assertTrue(failure.getMessage().contains("server restart required"));
        assertTrue(registry.getByID("NEW_COMMUNITY").isEmpty());
    }

    @Test void adapterFileFormatsRemainDiscoverable() throws Exception {
        Files.createDirectories(directory.resolve("packs"));
        Files.writeString(directory.resolve("packs/fixture"), "custom source format");
        RegistryKey key = RegistryKey.parse("terra2:fixture");
        ConfigPack pack = (ConfigPack) Proxy.newProxyInstance(ConfigPack.class.getClassLoader(), new Class<?>[]{ConfigPack.class},
            (proxy, method, args) -> {
                if(method.getName().equals("getRegistryKey")) return key;
                throw new AssertionError("Unexpected pack call: " + method.getName());
            });
        AtomicInteger calls = new AtomicInteger();
        ConfigRegistry registry = new ConfigRegistry(); registry.registerSourceAdapter(adapter("terra2:fixture", calls, pack));
        registry.loadAll(platform()); registry.discoverForReload(platform());
        assertEquals(1, calls.get()); assertSame(pack, registry.get(key).orElseThrow());
        assertEquals("REGISTERED", registry.discoveredSources().getFirst().status());
    }

    @Test void ambiguousAdaptersFailBeforeEitherLoaderRuns() throws Exception {
        Files.createDirectories(directory.resolve("packs/fixture"));
        AtomicInteger calls = new AtomicInteger();
        ConfigRegistry registry = new ConfigRegistry();
        registry.registerSourceAdapter(adapter("terra2:first", calls, null));
        registry.registerSourceAdapter(adapter("terra2:second", calls, null));
        assertThrows(ConfigRegistry.PackLoadFailuresException.class, () -> registry.loadAll(platform()));
        assertEquals(0, calls.get());
    }
}
