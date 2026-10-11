package org.geysermc.geyser.pack;

import com.google.gson.*;
import org.geysermc.geyser.GeyserBootstrap;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.api.biome.custom.CustomBiomeDefinition;
import org.geysermc.geyser.api.util.Identifier;
import org.geysermc.geyser.registry.Registries;
import org.geysermc.geyser.registry.mappings.*;
import org.geysermc.geyser.scoreboard.network.util.GeyserMockContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.zip.ZipFile;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Executed inside the unmodified PR #6658 source tree against an actual Terra2 registry export. */
public class Terra2BiomeMappingsTest {
    @TempDir Path folder;
    @Test void terra2RegistryColorsReachBedrockAssetsWithoutVanillaOverrides() throws Exception {
        Path mapping = Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("configuration/terra2-biomes.json")).toURI());
        var expected = JsonParser.parseString(Files.readString(mapping)).getAsJsonObject().getAsJsonObject("biomes");
        Map<Identifier, CustomBiomeDefinition> loaded = new HashMap<>();
        GeyserMockContext.mockContext(context -> {
            var bootstrap = context.mock(GeyserBootstrap.class);
            when(GeyserImpl.getInstance().getBootstrap()).thenReturn(bootstrap);
            when(bootstrap.getConfigFolder()).thenReturn(folder);
            when(bootstrap.getResourceOrThrow(any())).thenAnswer(call -> {
                var stream = getClass().getClassLoader().getResourceAsStream(call.<String>getArgument(0));
                assertNotNull(stream); return stream;
            });
            MappingsConfigReader.readCustomMappings(MappingsType.BIOMES, mapping, loaded::put);
            assertEquals(expected.size(), loaded.size(), "Every exported biome is accepted by the real reader");
            assertTrue(loaded.keySet().stream().noneMatch(id -> id.namespace().equals("minecraft")));
            assertEquals(loaded.size(), loaded.values().stream().map(CustomBiomeDefinition::bedrockIdentifier).distinct().count());
            Registries.CUSTOM_BIOMES.set(Map.copyOf(loaded));
            try {
                Path pack = CustomBiomeResourcePackManager.createResourcePack(); assertNotNull(pack);
                Map<String, JsonObject> clientBiomes = new HashMap<>();
                try(var zip = new ZipFile(pack.toFile())) {
                    for(var entry : zip.stream().toList()) if(entry.getName().endsWith(".json")) {
                        try(var reader = new InputStreamReader(zip.getInputStream(entry), java.nio.charset.StandardCharsets.UTF_8)) {
                            var root = JsonParser.parseReader(reader).getAsJsonObject();
                            if(root.has("minecraft:client_biome")) {
                                var biome = root.getAsJsonObject("minecraft:client_biome");
                                clientBiomes.put(biome.getAsJsonObject("description").get("identifier").getAsString(), biome.getAsJsonObject("components"));
                            }
                        }
                    }
                }
                assertEquals(expected.size(), clientBiomes.size());
                for(var entry : expected.entrySet()) {
                    var definition = loaded.get(Identifier.of(entry.getKey()));
                    var components = clientBiomes.get(definition.bedrockIdentifier().toString());
                    var effects = entry.getValue().getAsJsonObject().getAsJsonObject("effects");
                    assertEquals(effects.get("water_color").getAsString(), components.getAsJsonObject("minecraft:water_appearance").get("surface_color").getAsString());
                    if(effects.has("grass_color")) assertEquals(effects.get("grass_color").getAsString(), components.getAsJsonObject("minecraft:grass_appearance").get("color").getAsString());
                    if(effects.has("foliage_color")) assertEquals(effects.get("foliage_color").getAsString(), components.getAsJsonObject("minecraft:foliage_appearance").get("color").getAsString());
                }
                // This archive is a diagnostic only: Geyser generates/serves its own runtime pack.
                Path output = Path.of("build/terra2-bedrock"); Files.createDirectories(output);
                Files.copy(pack, output.resolve("custom_biomes.mcpack"), StandardCopyOption.REPLACE_EXISTING);
                Files.writeString(output.resolve("result.json"), "{\"status\":\"PASSED\",\"biomes\":" + loaded.size() + ",\"clientVisualTest\":false}");
                System.out.println("TERRA2_GEYSER_MAPPING_ASSETS_OK biomes=" + loaded.size());
            } catch(IOException error) { throw new AssertionError(error); }
            finally { Registries.CUSTOM_BIOMES.set(Map.of()); }
        });
    }
}
