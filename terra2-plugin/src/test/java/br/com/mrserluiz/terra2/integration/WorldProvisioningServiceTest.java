package br.com.mrserluiz.terra2.integration;

import br.com.mrserluiz.terra2.core.GenerationSafetySettings;
import br.com.mrserluiz.terra2.core.WorldDefinition;
import br.com.mrserluiz.terra2.core.WorldSafetyGuard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldProvisioningServiceTest {
    @TempDir Path temporary;

    @Test
    void createsOnlyExplicitIsolatedWorldAndWritesManifest() throws Exception {
        Path packs = Files.createDirectories(temporary.resolve("packs"));
        Path pack = Files.createDirectories(packs.resolve("overworld"));
        Files.writeString(pack.resolve("pack.yml"), "id: OVERWORLD\nversion: 1.0.0\naddons:\n  language-yaml: '1.+'\n");
        Path worlds = Files.createDirectories(temporary.resolve("worlds"));
        FakeRuntime runtime = new FakeRuntime(worlds);
        WorldManifestStore store = new WorldManifestStore(worlds);
        WorldProvisioningService service = new WorldProvisioningService(
                new CommunityPackCatalog(packs), new PackValidator(), store, runtime,
                new WorldSafetyGuard(), Clock.fixed(Instant.ofEpochMilli(1234), ZoneOffset.UTC));

        var result = service.provision(new GenerationSafetySettings(true, false, false, true, true),
                new WorldDefinition("ether_expansion", true, true, "OVERWORLD", "NORMAL", 42L),
                Set.of("world", "world_nether", "world_the_end"));

        assertEquals(ProvisioningResult.Status.CREATED, result.status());
        assertTrue(store.read("ether_expansion").isPresent());
        assertEquals("OVERWORLD", store.read("ether_expansion").orElseThrow().packId());
    }

    @Test
    void refusesMainWorldBeforeCallingRuntime() throws Exception {
        Path packs = Files.createDirectories(temporary.resolve("packs"));
        Path pack = Files.createDirectories(packs.resolve("overworld"));
        Files.writeString(pack.resolve("pack.yml"), "id: OVERWORLD\nversion: 1.0.0\naddons:\n  language-yaml: '1.+'\n");
        Path worlds = Files.createDirectories(temporary.resolve("worlds"));
        FakeRuntime runtime = new FakeRuntime(worlds);
        var service = new WorldProvisioningService(new CommunityPackCatalog(packs), new PackValidator(),
                new WorldManifestStore(worlds), runtime, new WorldSafetyGuard(), Clock.systemUTC());

        var result = service.provision(new GenerationSafetySettings(true, false, false, true, true),
                new WorldDefinition("world", true, true, "OVERWORLD", "NORMAL", null), Set.of("world"));

        assertEquals(ProvisioningResult.Status.BLOCKED, result.status());
        assertEquals(0, runtime.createCalls);
    }

    private static final class FakeRuntime implements TerraRuntimeAdapter {
        private final Path worlds;
        private int createCalls;
        private FakeRuntime(Path worlds) { this.worlds = worlds; }
        public boolean terraAvailable() { return true; }
        public Set<String> installedAddonIds() { return Set.of("language-yaml"); }
        public boolean packLoaded(String packId) { return packId.equals("OVERWORLD"); }
        public boolean createWorld(WorldDefinition definition, PackDescriptor pack) throws Exception {
            createCalls++;
            Files.createDirectories(worlds.resolve(definition.worldName()));
            return true;
        }
    }
}
