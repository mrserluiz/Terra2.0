package com.dfsek.terra.bukkit.nms;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.terra2.pack.ResourceScope;

/** Restores stable template identities immediately; decodes geometry only when generation needs it. */
public final class NativeTemplateRepository extends ConcurrentHashMap<Identifier, Optional<StructureTemplate>> {
    private record Asset(Path file, ResourceScope scope) {}
    private final Map<Identifier, Asset> assets = new ConcurrentHashMap<>();
    public static synchronized void install(StructureTemplateManager manager, Map<Identifier, Path> paths, ResourceScope scope) {
        NativeTemplateRepository repository;
        if(manager.structureRepository instanceof NativeTemplateRepository existing) repository = existing;
        else {
            repository = new NativeTemplateRepository(); repository.putAll(manager.structureRepository);
            try {
                var field = StructureTemplateManager.class.getDeclaredField("structureRepository"); field.setAccessible(true); field.set(manager, repository);
            } catch(ReflectiveOperationException error) { throw new IllegalStateException("Cannot restore private template repository", error); }
        }
        var target = repository;
        paths.forEach((id, file) -> {
            var previous = target.assets.putIfAbsent(id, new Asset(file, scope));
            if(previous != null && !previous.file.equals(file)) throw new IllegalStateException("Private template identity conflict: " + id);
        });
    }
    @Override public Optional<StructureTemplate> computeIfAbsent(Identifier id,
            Function<? super Identifier, ? extends Optional<StructureTemplate>> fallback) {
        return super.computeIfAbsent(id, key -> {
            var asset = assets.get(key); if(asset == null) return fallback.apply(key);
            try {
                if(Files.isSymbolicLink(asset.file) || Files.size(asset.file) > 8L * 1024 * 1024)
                    throw new IOException("Invalid private template file: " + asset.file);
                var nbt = NativeTemplateScope.rewrite(NativeTemplateScope.read(Files.readAllBytes(asset.file)), asset.scope);
                var template = new StructureTemplate(); template.load(net.minecraft.core.registries.BuiltInRegistries.BLOCK, nbt);
                return Optional.of(template);
            } catch(IOException error) { throw new UncheckedIOException("Cannot load private template " + key, error); }
        });
    }
}
