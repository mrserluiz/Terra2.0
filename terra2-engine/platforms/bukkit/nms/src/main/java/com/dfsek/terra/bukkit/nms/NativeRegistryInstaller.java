package com.dfsek.terra.bukkit.nms;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;

/** Installs stable private identities. Structure sets remain private activation inventories. */
public final class NativeRegistryInstaller {
    private NativeRegistryInstaller() {}
    public static void install(NativeResourceGraph graph, HolderLookup.Provider live) {
        if(!graph.report().valid()) throw new IllegalArgumentException("Cannot install a partial native graph");
        // Validate all identities before adding anything. No existing resource is replaced.
        graph.lookup().listRegistryKeys().forEach(key -> preflight(graph, live, key));
        graph.lookup().listRegistryKeys().forEach(key -> installRegistry(graph, live, key));
    }
    @SuppressWarnings({"rawtypes", "unchecked"}) private static void preflight(NativeResourceGraph graph, HolderLookup.Provider live, ResourceKey key) {
        if(key.equals(Registries.STRUCTURE_SET)) return;
        var lookup = live.lookupOrThrow(key);
        graph.sourceLookup(key).listElements().forEach(raw -> {
            Holder.Reference holder = (Holder.Reference) raw;
            if(!(lookup instanceof MappedRegistry)) throw new IllegalStateException("Native registry is not writable: " + key);
            Optional<Holder.Reference> existing = lookup.get(holder.key());
            if(existing.isPresent() && existing.get().value() != holder.value()) throw new IllegalStateException("Persistent alias conflict: " + holder.key());
        });
    }
    @SuppressWarnings({"rawtypes", "unchecked"}) private static void installRegistry(NativeResourceGraph graph, HolderLookup.Provider live, ResourceKey key) {
        if(key.equals(Registries.STRUCTURE_SET)) return;
        var lookup = live.lookupOrThrow(key);
        var sources = graph.sourceLookup(key).listElements().toList();
        var tags = graph.lookup().lookupOrThrow(key).listTags()
            .filter(tag -> ((HolderSet.Named) tag).key().location().getNamespace().equals("terra2"))
            .filter(tag -> ((HolderSet.Named) tag).key().location().getPath().startsWith(graph.scope().fingerprint() + "/")).toList();
        if(sources.isEmpty() && tags.isEmpty()) return;
        if(!(lookup instanceof MappedRegistry registry)) throw new IllegalStateException("Native registry is not writable: " + key);
        Reflection.MAPPED_REGISTRY.setFrozen(registry, false);
        try {
            for(Object raw : sources) {
                Holder.Reference source = (Holder.Reference) raw;
                Holder.Reference canonical = (Holder.Reference) registry.get(source.key()).orElseGet(() -> registry.register(source.key(), source.value(), RegistrationInfo.BUILT_IN));
                if(!canonical.areComponentsBound()) canonical.bindComponents(DataComponentMap.EMPTY);
                if(!source.areComponentsBound()) source.bindComponents(DataComponentMap.EMPTY);
            }
        } finally { Reflection.MAPPED_REGISTRY.setFrozen(registry, true); }
        Map<TagKey<Object>, List<Holder<Object>>> additions = new HashMap<>();
        for(Object raw : tags) {
            HolderSet.Named tag = (HolderSet.Named) raw;
            additions.put(tag.key(), tag.stream().toList());
        }
        AwfulBukkitHacks.addPrivateTags(registry, additions);
        for(Object raw : sources) {
            Holder.Reference source = (Holder.Reference) raw;
            Holder.Reference canonical = (Holder.Reference) registry.get(source.key()).orElseThrow();
            Reflection.REFERENCE.invokeBindTags(source, canonical.tags().toList());
        }
    }
}
