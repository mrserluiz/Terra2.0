package com.dfsek.terra.bukkit.bedrock;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import java.util.TreeMap;

/** Geyser PR #6658 interchange. Never overrides a vanilla biome or an operator-owned file. */
public final class BedrockMappings {
    private BedrockMappings() {}
    public static JsonObject document(Map<String, JsonObject> entries) {
        if(entries.isEmpty()) throw new IllegalArgumentException("Nenhum bioma Community Pack registrado");
        JsonObject root = new JsonObject(), biomes = new JsonObject();
        root.addProperty("format_version", 1);
        root.addProperty("terra2_export_schema", 1);
        new TreeMap<>(entries).forEach((id, appearance) -> {
            if(!id.matches("terra:[a-z0-9_./-]+")) throw new IllegalArgumentException("Somente IDs Terra: " + id);
            biomes.add(id, appearance.deepCopy());
        });
        root.add("biomes", biomes);
        return root;
    }
    public static void write(Path target, JsonObject root) throws IOException {
        Files.createDirectories(target.getParent());
        if(Files.isSymbolicLink(target)) throw new IOException("Destino não pode ser um link simbólico");
        if(Files.exists(target)) {
            try {
                var previous = JsonParser.parseString(Files.readString(target)).getAsJsonObject();
                if(!previous.has("terra2_export_schema") || previous.get("terra2_export_schema").getAsInt() != 1)
                    throw new IOException("Arquivo existente não pertence ao exportador Terra2: " + target);
            } catch(RuntimeException error) { throw new IOException("Arquivo existente inválido; preserve-o antes de substituir", error); }
        }
        Path temporary = Files.createTempFile(target.getParent(), ".terra2-biomes-", ".tmp");
        try {
            Files.writeString(temporary, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root) + "\n", StandardCharsets.UTF_8);
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temporary); }
    }
}
