package com.dfsek.terra.bukkit.bedrock;

import com.google.gson.*;
import org.terra2.api.visual.VisualProfile;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Namespaced client settings only: never replaces a minecraft biome or a liquid texture. */
public final class BedrockClientBiomes {
    private BedrockClientBiomes() {}
    public static String name(String source) {
        return "terra2:visual_"+UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8)).toString().replace("-", "");
    }
    public static JsonObject document(VisualProfile profile) {
        var root=new JsonObject();root.addProperty("format_version","1.21.40");
        var biome=new JsonObject();var description=new JsonObject();description.addProperty("identifier",name(profile.id()));
        biome.add("description",description);var components=new JsonObject();
        for(String role:java.util.List.of("water","grass","foliage")) {
            Integer rgb=profile.colors().get(role);if(rgb==null)continue;
            var color=new JsonObject();color.addProperty(role.equals("water")?"surface_color":"color","#%06X".formatted(rgb));
            components.add("minecraft:"+role+"_appearance",color);
        }
        biome.add("components",components);root.add("minecraft:client_biome",biome);return root;
    }
}
