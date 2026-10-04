package com.dfsek.terra.bukkit.nms;

import com.google.gson.*;
import com.mojang.serialization.*;
import java.util.*;
import java.util.stream.Stream;
import net.minecraft.core.*;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.storage.loot.LootDataType;
import org.terra2.pack.ResourceBundle;
import org.terra2.pack.ResourceScope;

/** Detached codec graph: forward pool/loot references resolve before values are bound.
 * This class deliberately does not modify server registries or enable generation. */
public final class NativeResourceGraph {
    public record Report(String fingerprint, Map<String, Integer> decoded, List<String> errors) {
        public Report { decoded = Map.copyOf(decoded); errors = List.copyOf(errors); }
        public boolean valid() { return errors.isEmpty(); }
    }
    private final ResourceBundle source;
    private final ResourceScope scope;
    private final HolderLookup.Provider base;
    private final Map<ResourceKey<? extends Registry<?>>, Node<?>> nodes = new LinkedHashMap<>();
    private final List<String> errors = new ArrayList<>();
    private final Map<String, Integer> decoded = new TreeMap<>();
    private final HolderLookup.Provider provider = new HolderLookup.Provider() {
        public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() { return base.listRegistryKeys(); }
        @SuppressWarnings("unchecked") public <T> Optional<? extends HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> key) {
            Node<?> node = nodes.get(key);
            return node == null ? base.lookup(key) : Optional.of((HolderLookup.RegistryLookup<T>) node.lookup);
        }
    };
    private final RegistryOps<JsonElement> ops;

    public NativeResourceGraph(ResourceBundle source, HolderLookup.Provider base) {
        this.source = source; this.scope = new ResourceScope(source); this.base = base;
        RegistryDataLoader.WORLDGEN_REGISTRIES.forEach(data -> data.runWithArguments(this::declare));
        LootDataType.values().forEach(this::declareLoot);
        // Sources for built-in-registry tags (blocks/items/entities) also need a private lookup.
        base.listRegistries().forEach(lookup -> declareTags(lookup));
        ops = RegistryOps.create(JsonOps.INSTANCE, new RegistryOps.RegistryInfoLookup() {
            @SuppressWarnings("unchecked") public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> key) {
                Node<?> node = nodes.get(key);
                if(node != null) return Optional.of((RegistryOps.RegistryInfo<T>) node.info());
                return base.lookup(key).map(RegistryOps.RegistryInfo::fromRegistryLookup);
            }
            public HolderLookup.Provider lookupForValueCopyViaBuilders() { return provider; }
        });
        // All reference holders have been declared, including cycles between pools.
        for(Node<?> node : nodes.values()) node.decode();
    }
    public ResourceScope scope() { return scope; }
    public HolderLookup.Provider lookup() { return provider; }
    public Report report() { return new Report(scope.fingerprint(), decoded, errors); }
    public <T> Holder.Reference<T> resource(ResourceKey<? extends Registry<T>> registry, String original) {
        if(!report().valid()) throw new IllegalStateException("Native resource graph failed validation");
        return provider.lookupOrThrow(registry).getOrThrow(ResourceKey.create(registry, Identifier.parse(scope.resource(original))));
    }
    private <T> void declare(ResourceKey<? extends Registry<T>> key, Codec<T> codec) {
        var parent = base.lookup(key);
        if(parent.isEmpty()) { errors.add("Missing server registry " + key.identifier()); return; }
        nodes.computeIfAbsent(key, ignored -> new Node<>(key, parent.get(), codec));
    }
    private <T extends net.minecraft.world.level.storage.loot.Validatable> void declareLoot(LootDataType<T> type) {
        declare(type.registryKey(), type.codec());
    }
    @SuppressWarnings("unchecked")
    private <T> void declareTags(HolderLookup.RegistryLookup<T> parent) {
        String category = parent.key().identifier().getPath();
        if(source.paths().stream().anyMatch(path -> path.matches("data/[^/]+/tags/" + category + "/.+\\.json")))
            nodes.computeIfAbsent(parent.key(), ignored -> new Node<>((ResourceKey<? extends Registry<T>>) (ResourceKey<?>) parent.key(), parent, null));
    }
    private final class Node<T> {
        private final ResourceKey<? extends Registry<T>> key;
        private final HolderLookup.RegistryLookup<T> parent;
        private final Codec<T> codec;
        private final Map<ResourceKey<T>, Holder.Reference<T>> holders = new LinkedHashMap<>();
        private final Map<ResourceKey<T>, String> paths = new LinkedHashMap<>();
        private final Map<TagKey<T>, String> tagPaths = new LinkedHashMap<>();
        private final Map<TagKey<T>, HolderSet.Named<T>> tags = new LinkedHashMap<>();
        private final Set<TagKey<T>> resolving = new HashSet<>();
        private final HolderLookup.RegistryLookup<T> lookup;
        Node(ResourceKey<? extends Registry<T>> key, HolderLookup.RegistryLookup<T> parent, Codec<T> codec) {
            this.key = key; this.parent = parent; this.codec = codec;
            String category = key.identifier().getPath();
            for(String path : source.paths()) {
                if(!path.startsWith("data/") || !path.endsWith(".json")) continue;
                String namespace = path.split("/")[1]; String prefix = "data/" + namespace + "/";
                if(codec != null && path.startsWith(prefix + category + "/")) {
                    String id = namespace + ":" + path.substring((prefix + category + "/").length(), path.length() - 5);
                    var resourceKey = ResourceKey.create(key, Identifier.parse(scope.privateId(id)));
                    holders.put(resourceKey, Holder.Reference.createStandAlone(parent, resourceKey)); paths.put(resourceKey, path);
                }
                if(path.startsWith(prefix + "tags/" + category + "/")) {
                    String id = namespace + ":" + path.substring((prefix + "tags/" + category + "/").length(), path.length() - 5);
                    tagPaths.put(TagKey.create(key, Identifier.parse(scope.privateId(id))), path);
                }
            }
            lookup = new HolderLookup.RegistryLookup<>() {
                public ResourceKey<? extends Registry<T>> key() { return key; }
                public Lifecycle registryLifecycle() { return parent.registryLifecycle(); }
                public Stream<Holder.Reference<T>> listElements() { return Stream.concat(parent.listElements(), holders.values().stream()); }
                public Stream<HolderSet.Named<T>> listTags() {
                    tagPaths.keySet().forEach(Node.this::tag);
                    return Stream.concat(parent.listTags(), tags.values().stream());
                }
                public Optional<Holder.Reference<T>> get(ResourceKey<T> target) { return Optional.ofNullable(holders.get(target)).or(() -> parent.get(target)); }
                public Optional<T> getValueForCopying(ResourceKey<T> target) {
                    var holder = holders.get(target);
                    return holder != null && holder.isBound() ? Optional.of(holder.value()) : parent.getValueForCopying(target);
                }
                public Optional<HolderSet.Named<T>> get(TagKey<T> target) { return tagPaths.containsKey(target) ? Optional.of(tag(target)) : parent.get(target); }
            };
        }
        RegistryOps.RegistryInfo<T> info() { return new RegistryOps.RegistryInfo<>(parent, lookup, parent.registryLifecycle()); }
        void decode() {
            for(var entry : paths.entrySet()) try {
                JsonElement json = scope.rewrite(JsonParser.parseString(source.text(entry.getValue())));
                T value = codec.parse(ops, json).getOrThrow();
                Reflection.REFERENCE.invokeBindValue(holders.get(entry.getKey()), value);
                decoded.merge(key.identifier().toString(), 1, Integer::sum);
            } catch(RuntimeException | LinkageError error) { errors.add(entry.getValue() + ": " + error.getMessage()); }
            for(var tagKey : tagPaths.keySet()) try { tag(tagKey); }
            catch(RuntimeException | LinkageError error) { errors.add(tagPaths.get(tagKey) + ": " + error.getMessage()); }
        }
        HolderSet.Named<T> tag(TagKey<T> target) {
            if(tags.containsKey(target)) return tags.get(target);
            if(!resolving.add(target)) throw new IllegalArgumentException("Cyclic source tag: " + target.location());
            try {
                var root = JsonParser.parseString(source.text(tagPaths.get(target))).getAsJsonObject();
                var entries = new ArrayList<Holder<T>>();
                // replace=false retains the server's original tag, under the isolated alias.
                String path = tagPaths.get(target), ns = path.split("/")[1], prefix = "data/" + ns + "/tags/" + key.identifier().getPath() + "/";
                String original = ns + ":" + path.substring(prefix.length(), path.length() - 5);
                if(!root.has("replace") || !root.get("replace").getAsBoolean())
                    parent.get(TagKey.create(key, Identifier.parse(original))).ifPresent(set -> set.stream().forEach(entries::add));
                for(JsonElement value : root.getAsJsonArray("values")) {
                    boolean required = !value.isJsonObject() || !value.getAsJsonObject().has("required") || value.getAsJsonObject().get("required").getAsBoolean();
                    String id = value.isJsonObject() ? value.getAsJsonObject().get("id").getAsString() : value.getAsString();
                    if(id.startsWith("#")) {
                        String rewritten = scope.tag(id).substring(1);
                        var found = lookup.get(TagKey.create(key, Identifier.parse(rewritten)));
                        if(found.isEmpty() && required) throw new IllegalArgumentException("Missing tag " + id);
                        found.ifPresent(set -> set.stream().forEach(entries::add));
                    } else {
                        var found = lookup.get(ResourceKey.create(key, Identifier.parse(scope.resource(id))));
                        if(found.isEmpty() && required) throw new IllegalArgumentException("Missing tag entry " + id);
                        found.ifPresent(entries::add);
                    }
                }
                var named = new HolderSet.Named<T>(parent, target);
                Reflection.HOLDER_SET.invokeBind(named, entries.stream().distinct().toList()); tags.put(target, named); return named;
            } finally { resolving.remove(target); }
        }
    }
}
