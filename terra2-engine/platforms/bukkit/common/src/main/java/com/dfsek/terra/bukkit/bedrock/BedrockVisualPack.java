package com.dfsek.terra.bukkit.bedrock;

import com.google.gson.*;
import org.terra2.api.visual.VisualProfile;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Portable exact-color texture atlas. Registration/position projection is deliberately separate. */
public final class BedrockVisualPack {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_COLORS = 4096;
    private BedrockVisualPack() {}

    public static List<VisualProfile> fromBiomes(Map<String, JsonObject> biomes) {
        var result = new ArrayList<VisualProfile>();
        biomes.forEach((id, value) -> {
            var colors = new TreeMap<String, Integer>();
            JsonObject effects = value.getAsJsonObject("effects"), attributes = value.getAsJsonObject("attributes");
            read(colors, effects, "grass_color", "grass"); read(colors, effects, "foliage_color", "foliage");
            read(colors, effects, "dry_foliage_color", "dry_foliage"); read(colors, effects, "water_color", "water");
            read(colors, attributes, "minecraft:visual/sky_color", "sky");
            read(colors, attributes, "minecraft:visual/fog_color", "fog");
            read(colors, attributes, "minecraft:visual/water_fog_color", "water_fog");
            if(!colors.isEmpty()) result.add(new VisualProfile(id, colors));
        });
        return result;
    }
    private static void read(Map<String, Integer> colors, JsonObject object, String field, String role) {
        if(object == null || !object.has(field)) return;
        String hex = object.get(field).getAsString();
        if(!hex.matches("#[0-9a-fA-F]{6}")) throw new IllegalArgumentException("Invalid color: " + field);
        colors.put(role, Integer.parseInt(hex.substring(1), 16));
    }
    public static JsonObject index(Collection<VisualProfile> input) {
        var profiles = new TreeMap<String, VisualProfile>();
        var palette = new TreeSet<Integer>();
        // RGB332 starter palette. Exact requested colors below are added, never rounded to it.
        for(int r=0;r<8;r++) for(int g=0;g<8;g++) for(int b=0;b<4;b++)
            palette.add(((r*255/7)<<16) | ((g*255/7)<<8) | (b*255/3));
        for(var profile : input) {
            var previous = profiles.putIfAbsent(profile.id(), profile);
            if(previous != null && !previous.equals(profile)) throw new IllegalArgumentException("Conflicting visual profile: " + profile.id());
            palette.addAll(profile.colors().values());
        }
        if(palette.size() > MAX_COLORS) throw new IllegalArgumentException("Palette exceeds " + MAX_COLORS + " exact colors; split the catalogs");
        var root = new JsonObject(); root.addProperty("terra2_visual_schema", 1);
        root.addProperty("runtime_projection", "OPT_IN_GRASS_AND_NATIVE_BIOME_PREVIEW");
        root.addProperty("base_palette_colors", 256); root.addProperty("exact_color_count", palette.size());
        var textures = new JsonObject();
        for(int rgb : palette) textures.addProperty(hex(rgb), texture(rgb));
        root.add("textures", textures);
        var entries = new JsonObject();
        profiles.forEach((id, profile) -> {
            var roles = new JsonObject(); profile.colors().forEach((role, rgb) -> roles.addProperty(role, hex(rgb)));
            entries.add(id, roles);
        });
        root.add("profiles", entries); return root;
    }
    private static String hex(int rgb) { return "#%06X".formatted(rgb); }
    private static String texture(int rgb) { return "terra2_rgb_%06x".formatted(rgb); }
    public static JsonArray version() throws IOException {
        var properties=new Properties();
        try(var stream=BedrockVisualPack.class.getResourceAsStream("/terra2/bedrock-build.properties")) {
            if(stream==null)throw new IOException("Pack build version resource missing");properties.load(stream);
        }
        String[] parts=properties.getProperty("version","").split("[-+]",2)[0].split("\\.");
        if(parts.length!=3)throw new IOException("Invalid visual pack build version");
        var array=new JsonArray();try{for(String part:parts){int number=Integer.parseInt(part);if(number<0)throw new NumberFormatException();array.add(number);}}catch(NumberFormatException invalid){throw new IOException("Invalid visual pack build version",invalid);}return array;
    }
    public static byte[] compile(Collection<VisualProfile> profiles) throws IOException {
        var index = index(profiles);var version=version();index.add("pack_version",version);String identity = JSON.toJson(index);
        var manifest = new JsonObject(); manifest.addProperty("format_version", 2);
        var header = new JsonObject(); header.addProperty("name", "Terra2 Generic RGB Palette");
        header.addProperty("description", "RGB atlas for optional Terra2 position preview. No vanilla overrides.");
        header.addProperty("uuid", UUID.nameUUIDFromBytes(("header:"+identity).getBytes(StandardCharsets.UTF_8)).toString());
        header.add("version", version.deepCopy()); header.add("min_engine_version", JsonParser.parseString("[1,21,40]"));
        manifest.add("header", header);
        var module = new JsonObject(); module.addProperty("type", "resources");
        module.addProperty("uuid", UUID.nameUUIDFromBytes(("module:"+identity).getBytes(StandardCharsets.UTF_8)).toString());
        module.add("version", version.deepCopy()); var modules = new JsonArray(); modules.add(module); manifest.add("modules", modules);
        var atlas = new JsonObject(); atlas.addProperty("resource_pack_name", "Terra2 Generic RGB Palette");
        atlas.addProperty("texture_name", "atlas.terrain"); atlas.addProperty("padding", 8); atlas.addProperty("num_mip_levels", 4);
        var textureData = new JsonObject();
        var bytes = new ByteArrayOutputStream();
        try(var zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            entry(zip, "manifest.json", JSON.toJson(manifest).getBytes(StandardCharsets.UTF_8));
            entry(zip, "terra2-palette.json", identity.getBytes(StandardCharsets.UTF_8));
            for(var profile : index.getAsJsonObject("profiles").entrySet()) {
                var roles=new TreeMap<String,Integer>();profile.getValue().getAsJsonObject().entrySet().forEach(e -> roles.put(e.getKey(),Integer.parseInt(e.getValue().getAsString().substring(1),16)));
                var settings=BedrockClientBiomes.document(new VisualProfile(profile.getKey(),roles));
                entry(zip,"biomes/"+BedrockClientBiomes.name(profile.getKey()).substring(7)+".client_biome.json",JSON.toJson(settings).getBytes(StandardCharsets.UTF_8));
            }
            for(var color : index.getAsJsonObject("textures").entrySet()) {
                int rgb = Integer.parseInt(color.getKey().substring(1), 16); String name = color.getValue().getAsString();
                var definition = new JsonObject(); definition.addProperty("textures", "textures/terra2/palette/"+name); textureData.add(name, definition);
                // Neutral swatches are generated assets, not copied/recolored Mojang textures.
                var image = new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
                for(int x=0;x<16;x++) for(int y=0;y<16;y++) image.setRGB(x,y,0xFF000000 | rgb);
                var png = new ByteArrayOutputStream(); if(!ImageIO.write(image,"png",png)) throw new IOException("PNG encoder unavailable");
                entry(zip, "textures/terra2/palette/"+name+".png", png.toByteArray());
            }
            atlas.add("texture_data", textureData); entry(zip,"textures/terrain_texture.json",JSON.toJson(atlas).getBytes(StandardCharsets.UTF_8));
        }
        return bytes.toByteArray();
    }
    private static void entry(ZipOutputStream zip, String path, byte[] bytes) throws IOException {
        var entry = new ZipEntry(path); entry.setTime(0); zip.putNextEntry(entry); zip.write(bytes); zip.closeEntry();
    }
    public static void write(Path path, Collection<VisualProfile> profiles) throws IOException {
        byte[] bytes = compile(profiles); // Validate everything before touching output.
        Files.createDirectories(path.toAbsolutePath().getParent());
        if(Files.isSymbolicLink(path)) throw new IOException("Refusing symbolic-link output");
        if(Files.exists(path)) {
            try(var zip = new ZipFile(path.toFile())) {
                var marker = zip.getEntry("terra2-palette.json");
                if(marker == null) throw new IOException("Existing resource pack is not a Terra2 palette");
                try(var reader = new InputStreamReader(zip.getInputStream(marker), StandardCharsets.UTF_8)) {
                    var document = JsonParser.parseReader(reader).getAsJsonObject();
                    if(!document.has("terra2_visual_schema") || document.get("terra2_visual_schema").getAsInt()!=1)
                        throw new IOException("Unknown palette schema");
                }
            } catch(RuntimeException error) { throw new IOException("Invalid existing palette", error); }
        }
        var temp = Files.createTempFile(path.toAbsolutePath().getParent(), "terra2-palette-", ".tmp");
        try { Files.write(temp,bytes); Files.move(temp,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
        finally { Files.deleteIfExists(temp); }
    }
}
