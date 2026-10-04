package org.terra2.pack;

import com.google.gson.*;
import java.util.*;

/** Conversion-time inventory of structure dependencies, separate from executable READY validation. */
public final class StructureCatalog {
    public record Piece(String id, int dataVersion, StructureNbt.Position size, int blocks, int palettes,
                        int blockEntities, int entities, int jigsaws, Set<String> pools, Set<String> lootTables) {}
    public record Reference(String from, String kind, String target, String resolution) {}
    public record Report(List<Piece> pieces, List<Reference> references, List<String> errors,
                         Map<String, Integer> types, List<String> pendingBackends) {}
    private StructureCatalog() {}
    public static Report audit(ResourceBundle source) {
        List<Piece> pieces = new ArrayList<>(); List<Reference> references = new ArrayList<>(); List<String> errors = new ArrayList<>();
        Map<String, Integer> types = new TreeMap<>();
        for(String path : source.paths()) {
            try {
                if(path.matches("data/[^/]+/(structure|structures)/.+\\.nbt")) {
                    String folder = path.split("/")[2]; String id = id(path, folder, 4);
                    var template = StructureNbt.read(source.bytes(path));
                    int blockEntities = 0, jigsaws = 0; Set<String> pools = new TreeSet<>(), loot = new TreeSet<>();
                    for(var block : template.blocks()) {
                        if(!block.nbt().isEmpty()) blockEntities++;
                        boolean jigsaw = template.palettes().stream().anyMatch(palette -> palette.get(block.state()).name().equals("minecraft:jigsaw"));
                        if(jigsaw) {
                            jigsaws++;
                            Object pool = block.nbt().get("pool");
                            if(!(pool instanceof String text)) throw new IllegalArgumentException("Jigsaw block missing pool");
                            pools.add(ResourceBundle.identifier(text));
                        }
                        Object table = block.nbt().get("LootTable");
                        if(table instanceof String text) loot.add(ResourceBundle.identifier(text));
                    }
                    for(String pool : pools) reference(source, references, id, "template_pool", pool);
                    for(String table : loot) reference(source, references, id, "loot_table", table);
                    pieces.add(new Piece(id, template.dataVersion(), template.size(), template.blocks().size(), template.palettes().size(),
                        blockEntities, template.entities().size(), jigsaws, Collections.unmodifiableSet(pools), Collections.unmodifiableSet(loot)));
                } else if(path.matches("data/[^/]+/worldgen/(structure|structure_set|template_pool|processor_list)/.+\\.json")) {
                    String kind = path.split("/")[3], id = id(path, "worldgen/" + kind, 5);
                    var json = PackCompiler.object(source.text(path));
                    if(json.has("type") && json.get("type").isJsonPrimitive()) types.merge(kind + ":" + json.get("type").getAsString(), 1, Integer::sum);
                    else types.merge(kind, 1, Integer::sum);
                    switch(kind) {
                        case "structure" -> {
                            if(json.has("start_pool")) reference(source, references, id, "template_pool", PackCompiler.string(json, "start_pool"));
                            if(json.has("biomes") && json.get("biomes").isJsonPrimitive()) {
                                String biome = PackCompiler.string(json, "biomes");
                                reference(source, references, id, biome.startsWith("#") ? "tag/worldgen/biome" : "biome", biome.replaceFirst("^#", ""));
                            }
                        }
                        case "structure_set" -> {
                            for(var element : PackCompiler.array(json, "structures")) reference(source, references, id, "structure", PackCompiler.string(element.getAsJsonObject(), "structure"));
                            var placement = PackCompiler.child(json, "placement");
                            types.merge("placement:" + PackCompiler.string(placement, "type"), 1, Integer::sum);
                            if(placement.has("exclusion_zone")) reference(source, references, id, "structure_set", PackCompiler.string(PackCompiler.child(placement, "exclusion_zone"), "other_set"));
                        }
                        case "template_pool" -> {
                            reference(source, references, id, "template_pool", PackCompiler.string(json, "fallback"));
                            for(var element : PackCompiler.array(json, "elements")) poolElement(source, references, types, id, PackCompiler.child(element.getAsJsonObject(), "element"));
                        }
                        case "processor_list" -> {
                            for(var processor : PackCompiler.array(json, "processors")) types.merge("processor:" + PackCompiler.string(processor.getAsJsonObject(), "processor_type"), 1, Integer::sum);
                        }
                        default -> throw new IllegalStateException(kind);
                    }
                }
            } catch(Exception error) { errors.add(path + ": " + error.getMessage()); }
        }
        // A missing custom dependency is an error. Vanilla references still require runtime registry validation.
        for(var ref : references) if(ref.resolution.equals("MISSING_CUSTOM")) errors.add("Missing " + ref.kind + " " + ref.target + " referenced by " + ref.from);
        return new Report(List.copyOf(pieces), List.copyOf(references), List.copyOf(errors), Collections.unmodifiableMap(types),
            pieces.isEmpty() && types.isEmpty() ? List.of() : List.of("jigsaw assembly and rotations", "placement/biome filters",
                "processors and terrain fitting", "block/entity NBT migration", "loot and gameplay dependencies", "per-world Paper structure execution"));
    }
    private static void poolElement(ResourceBundle source, List<Reference> refs, Map<String, Integer> types, String from, JsonObject element) {
        String type = PackCompiler.string(element, "element_type"); types.merge("pool_element:" + type, 1, Integer::sum);
        if(element.has("location")) reference(source, refs, from, "template", PackCompiler.string(element, "location"));
        if(element.has("processors") && element.get("processors").isJsonPrimitive()) reference(source, refs, from, "processor_list", PackCompiler.string(element, "processors"));
        if(element.has("feature")) reference(source, refs, from, "placed_feature", PackCompiler.string(element, "feature"));
        if(element.has("elements")) for(var child : PackCompiler.array(element, "elements")) poolElement(source, refs, types, from, child.getAsJsonObject());
    }
    private static void reference(ResourceBundle source, List<Reference> refs, String from, String kind, String target) {
        ResourceBundle.identifier(target); String[] parts = target.split(":", 2); String prefix = "data/" + parts[0] + "/";
        boolean local;
        if(kind.equals("template")) local = source.contains(prefix + "structure/" + parts[1] + ".nbt") || source.contains(prefix + "structures/" + parts[1] + ".nbt");
        else if(kind.equals("loot_table")) local = source.contains(prefix + "loot_table/" + parts[1] + ".json") || source.contains(prefix + "loot_tables/" + parts[1] + ".json");
        else if(kind.startsWith("tag/")) local = source.contains(prefix + "tags/" + kind.substring(4) + "/" + parts[1] + ".json");
        else local = source.contains(prefix + "worldgen/" + kind + "/" + parts[1] + ".json");
        refs.add(new Reference(from, kind, target, local ? "LOCAL" : target.startsWith("minecraft:") ? "VANILLA_EXTERNAL_UNVALIDATED" : "MISSING_CUSTOM"));
    }
    private static String id(String path, String kind, int suffix) {
        String namespace = path.split("/")[1], prefix = "data/" + namespace + "/" + kind + "/";
        return ResourceBundle.identifier(namespace + ":" + path.substring(prefix.length(), path.length() - suffix));
    }
}
