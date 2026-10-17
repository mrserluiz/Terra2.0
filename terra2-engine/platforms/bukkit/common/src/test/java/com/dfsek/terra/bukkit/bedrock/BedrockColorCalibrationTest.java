package com.dfsek.terra.bukkit.bedrock;
import org.junit.jupiter.api.Test;
import org.terra2.api.visual.VisualProfile;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
class BedrockColorCalibrationTest {
    @Test void neutralPreservesEveryChannel() {
        var adjustment=new BedrockColorCalibration.Adjustment(1,1,0);
        for(int color:new int[]{0,0xFFFFFF,0x9B42FF,0xAD6C35})assertEquals(color,adjustment.apply(color));
    }
    @Test void biomeOverrideDoesNotChangeJavaOrOtherRoles() {
        var source=new VisualProfile("terra:test",Map.of("foliage",0xAA8866,"water",0x9B42FF));
        var calibration=new BedrockColorCalibration(Map.of("foliage",new BedrockColorCalibration.Adjustment(1.15,1,0)),Map.of("terra:test",Map.of("foliage",new BedrockColorCalibration.Adjustment(1.25,1,0))));
        var target=calibration.apply(source);
        assertEquals(0xAA8866,source.colors().get("foliage"));assertEquals(source.colors().get("water"),target.colors().get("water"));
        assertEquals(new BedrockColorCalibration.Adjustment(1.25,1,0).apply(0xAA8866),target.colors().get("foliage"));assertNotEquals(source.colors().get("foliage"),target.colors().get("foliage"));
    }
    @Test void refusesInvalidAdjustments() {
        assertThrows(IllegalArgumentException.class,()->new BedrockColorCalibration.Adjustment(Double.NaN,1,0));
        assertThrows(IllegalArgumentException.class,()->new BedrockColorCalibration.Adjustment(1,3,0));
    }

    @org.junit.jupiter.api.io.TempDir java.nio.file.Path temp;
    @Test void yamlExactColorWinsAndReportMatchesPack() throws Exception {
        var path=temp.resolve("color-calibration.yml");
        java.nio.file.Files.writeString(path,"""
            roles:
              foliage: {saturation: 2.0, brightness: 0.4, hue-degrees: 20}
            biomes:
              'terra:my.pack/autumn':
                foliage: {color: '#FF6A19', saturation: 2.0, brightness: 0.2}
            """);
        var calibration=BedrockColorCalibration.load(path);
        var original=new VisualProfile("terra:my.pack/autumn",Map.of("foliage",0xABCCDD,"water",0x9B42FF));
        calibration.validate(java.util.List.of(original));var applied=calibration.apply(original);
        assertEquals(0xFF6A19,applied.colors().get("foliage"));assertEquals(0x9B42FF,applied.colors().get("water"));assertEquals(0xABCCDD,original.colors().get("foliage"));
        assertEquals("BIOME_COLOR",calibration.origin(original.id(),"foliage"));
        byte[] pack=BedrockVisualPack.compile(java.util.List.of(applied));
        var report=new BedrockColorReport(java.util.List.of(original),java.util.List.of(applied),calibration,pack);report.write(temp.resolve("color-report.yml"));
        assertTrue(report.details(original.id()).stream().anyMatch(line->line.contains("original=#ABCCDD; final=#FF6A19; BIOME_COLOR")));
        assertTrue(report.metadata().contains(java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(pack))));
        try(var zip=new java.util.zip.ZipInputStream(new java.io.ByteArrayInputStream(pack))) {
            boolean checked=false;
            for(java.util.zip.ZipEntry entry;(entry=zip.getNextEntry())!=null;)if(entry.getName().endsWith(".client_biome.json")) {
                var doc=com.google.gson.JsonParser.parseString(new String(zip.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
                assertEquals("#FF6A19",doc.getAsJsonObject("minecraft:client_biome").getAsJsonObject("components").getAsJsonObject("minecraft:foliage_appearance").get("color").getAsString());checked=true;
            }
            assertTrue(checked);
        }
        var saved=new org.bukkit.configuration.file.YamlConfiguration();saved.load(temp.resolve("color-report.yml").toFile());assertEquals(1,saved.getInt("schema"));assertEquals(1,saved.getMapList("profiles").size());
    }
    @Test void refusesTyposAndUnknownIDsInsteadOfIgnoringThem() throws Exception {
        var path=temp.resolve("bad.yml");java.nio.file.Files.writeString(path,"roles: {}\nfoliage: {color: '#FF0000'}\n");
        assertThrows(IllegalArgumentException.class,()->BedrockColorCalibration.load(path));
        java.nio.file.Files.writeString(path,"roles: {}\nbiomes:\n  'terra:typo':\n    foliage: {color: '#FF0000'}\n");
        var calibration=BedrockColorCalibration.load(path);
        assertThrows(IllegalArgumentException.class,()->calibration.validate(java.util.List.of(new VisualProfile("terra:valid",Map.of("foliage",0)))));
        for(String invalid:java.util.List.of("'#XYZ123'","12345")) {
            java.nio.file.Files.writeString(path,"roles:\n  foliage: {color: "+invalid+"}\nbiomes: {}\n");
            assertThrows(IllegalArgumentException.class,()->BedrockColorCalibration.load(path));
        }
    }
}
