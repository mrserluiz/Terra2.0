package com.dfsek.terra.bukkit.bedrock;

import com.google.gson.JsonParser;
import org.terra2.api.visual.VisualProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;

class BedrockVisualPackTest {
    @TempDir Path folder;
    @Test void independentGeneratorsShareExactColorsAndStableAssets() throws Exception {
        var a = new VisualProfile("generator_a:sakura", Map.of("grass", 0xFFD1EF, "foliage", 0xFF99CC));
        var b = new VisualProfile("generator_b:frost", Map.of("grass", 0xFFD1EF, "water", 0x80F9FF));
        byte[] first = BedrockVisualPack.compile(List.of(a,b));
        assertArrayEquals(first, BedrockVisualPack.compile(List.of(b,a,a)));
        Path target = folder.resolve("palette.mcpack"); Files.write(target, first);
        try(var zip = new ZipFile(target.toFile())) {
            var index = JsonParser.parseString(new String(zip.getInputStream(zip.getEntry("terra2-palette.json")).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals(2,index.getAsJsonObject("profiles").size());
            var manifest=JsonParser.parseString(new String(zip.getInputStream(zip.getEntry("manifest.json")).readAllBytes(),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("[7,0,30]",manifest.getAsJsonObject("header").getAsJsonArray("version").toString());
            assertEquals(manifest.getAsJsonObject("header").get("version"),manifest.getAsJsonArray("modules").get(0).getAsJsonObject().get("version"));
            assertEquals("#FFD1EF",index.getAsJsonObject("profiles").getAsJsonObject("generator_b:frost").get("grass").getAsString());
            assertEquals(259,index.get("exact_color_count").getAsInt());
            var png = ImageIO.read(zip.getInputStream(zip.getEntry("textures/terra2/palette/terra2_rgb_ffd1ef.png")));
            assertEquals(0xFFFFD1EF,png.getRGB(8,8));
            var client=JsonParser.parseString(new String(zip.getInputStream(zip.getEntry("biomes/"+BedrockClientBiomes.name(b.id()).substring(7)+".client_biome.json")).readAllBytes(),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("minecraft:client_biome");
            assertTrue(client.getAsJsonObject("description").get("identifier").getAsString().startsWith("terra2:"));
            var water=client.getAsJsonObject("components").getAsJsonObject("minecraft:water_appearance");
            assertEquals("#80F9FF",water.get("surface_color").getAsString());
            assertFalse(water.has("surface_opacity")); // Leave vanilla transparency unchanged.
            assertNull(zip.getEntry("textures/blocks/water_still.png"));
            assertNull(zip.getEntry("blocks.json")); assertNull(zip.getEntry("biomes/plains.json"));
            assertEquals("OPT_IN_GRASS_AND_NATIVE_BIOME_PREVIEW",index.get("runtime_projection").getAsString());
        }
        BedrockVisualPack.write(target,List.of(a,b)); assertArrayEquals(first,Files.readAllBytes(target));
    }
    @Test void rejectsAmbiguousProfilesAndInvalidRequests() {
        var a = new VisualProfile("generic:a", Map.of("grass",1));
        assertThrows(IllegalArgumentException.class,()->BedrockVisualPack.compile(List.of(a,new VisualProfile("generic:a",Map.of("grass",2)))));
        assertThrows(IllegalArgumentException.class,()->new VisualProfile("../invalid",Map.of("grass",1)));
        assertThrows(IllegalArgumentException.class,()->new VisualProfile("generic:a",Map.of("grass",0x1000000)));
        assertThrows(IllegalArgumentException.class,()->new VisualProfile("generic:a",Map.of("unknown",1)));
        var colors = new HashMap<String,Integer>(); colors.put("grass",1);
        var profile = new VisualProfile("generic:a",colors); colors.put("grass",2);
        assertEquals(1,profile.colors().get("grass"));
    }
    @Test void preservesUnownedPacksAndRefusesOversizedCatalogBeforeWriting() throws Exception {
        Path target=folder.resolve("operator.mcpack"); byte[] original={1,2,3}; Files.write(target,original);
        assertThrows(java.io.IOException.class,()->BedrockVisualPack.write(target,List.of()));
        assertArrayEquals(original,Files.readAllBytes(target));
        var profiles=new ArrayList<VisualProfile>();
        for(int i=0;i<4097;i++) profiles.add(new VisualProfile("generic:c"+i,Map.of("grass",i)));
        assertThrows(IllegalArgumentException.class,()->BedrockVisualPack.write(target,profiles));
        assertArrayEquals(original,Files.readAllBytes(target));
    }
}
