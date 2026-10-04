package com.dfsek.terra.bukkit.nms;

import java.io.*;
import java.util.*;
import net.minecraft.nbt.*;
import org.terra2.pack.*;

/** Known decorative compatibility adaptation; all originals remain in the converted archive. */
public final class NativePaperTemplateMigration implements TemplateMigration.Backend {
    private final org.terra2.adapter.vanilla.paper.PaperTemplateMigration delegate = new org.terra2.adapter.vanilla.paper.PaperTemplateMigration();
    private final Set<String> adaptations = new LinkedHashSet<>();
    @Override public int targetDataVersion() { return delegate.targetDataVersion(); }
    @Override public List<String> adaptations() { return List.copyOf(adaptations); }
    @Override public byte[] migrate(byte[] source) throws IOException {
        var root = NativeTemplateScope.read(source); boolean adapted = false;
        List<ListTag> palettes = new ArrayList<>();
        if(root.contains("palette")) palettes.add(root.getListOrEmpty("palette"));
        for(Tag entry : root.getListOrEmpty("palettes")) palettes.add((ListTag) entry);
        for(ListTag palette : palettes) for(Tag entry : palette) {
            var state = (CompoundTag) entry;
            if(state.getStringOr("Name", "").equals("waystones:deepslate_waystone")) {
                state.putString("Name", "minecraft:lodestone"); state.remove("Properties"); adapted = true;
            }
        }
        if(adapted) {
            for(Tag entry : root.getListOrEmpty("blocks")) {
                var block = (CompoundTag) entry; var nbt = block.getCompoundOrEmpty("nbt");
                if(!nbt.getStringOr("id", "").equals("waystones:waystone")) continue;
                if(!emptyWaystone(nbt)) throw new IOException("Waystone with custom data/inventory cannot become a decorative lodestone");
                block.remove("nbt");
            }
            adaptations.add("waystones:deepslate_waystone -> minecraft:lodestone (decorative only; Waystones teleportation is unavailable on Paper)");
            source = NativeTemplateScope.write(root);
        }
        return delegate.migrate(source);
    }
    private static boolean emptyWaystone(CompoundTag nbt) {
        if(!Set.of("id", "UUID", "Items", "x", "y", "z").containsAll(nbt.keySet())) return false;
        var items = nbt.getCompoundOrEmpty("Items");
        return Set.of("Size", "Items").containsAll(items.keySet()) && items.getListOrEmpty("Items").isEmpty();
    }
    @Override public boolean acceptsBlockEntityRemoval(StructureNbt.Block block, StructureNbt.Template before,
                                                     StructureNbt.Block next, StructureNbt.Template after) {
        if(!"waystones:waystone".equals(block.nbt().get("id")) || !Set.of("id", "UUID", "Items", "x", "y", "z").containsAll(block.nbt().keySet())) return false;
        if(!(block.nbt().get("Items") instanceof Map<?, ?> items) || !(items.get("Items") instanceof List<?> list) || !list.isEmpty()) return false;
        for(int i = 0; i < before.palettes().size(); i++)
            if(!before.palettes().get(i).get(block.state()).name().equals("waystones:deepslate_waystone")
                    || !after.palettes().get(i).get(next.state()).name().equals("minecraft:lodestone")) return false;
        return true;
    }
}
