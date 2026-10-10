package org.terra2.pack;

import java.io.IOException;
import java.util.Optional;
import java.util.function.*;

/** Resolves each ID before opening any converted artifact. Collisions are refused. */
public final class PackResolver<C, T> {
    public enum Backend { COMMUNITY, TERRAPACK }
    public record Resolved<C, T>(String id, Backend backend, C community, T terraPack) {}
    @FunctionalInterface public interface Loader<T> { T load(String id) throws IOException; }
    private final Function<String, Optional<C>> community;
    private final Consumer<String> sourceAvailable;
    private final Predicate<String> converted;
    private final Loader<T> loader;
    private final Supplier<String> diagnostics;
    public PackResolver(Function<String, Optional<C>> community, Consumer<String> sourceAvailable,
                        Predicate<String> converted, Loader<T> loader, Supplier<String> diagnostics) {
        this.community = community; this.sourceAvailable = sourceAvailable;
        this.converted = converted; this.loader = loader; this.diagnostics = diagnostics;
    }
    public Resolved<C, T> resolve(String id) throws IOException {
        var original = community.apply(id);
        boolean hasConverted = converted.test(id);
        if(original.isPresent() && hasConverted)
            throw new IllegalArgumentException("Pack ID collision: " + id + " is provided by COMMUNITY and TERRAPACK; use distinct IDs");
        if(original.isPresent()) return new Resolved<>(id, Backend.COMMUNITY, original.get(), null);
        sourceAvailable.accept(id); // Preserve loader rejection instead of pretending its source is absent.
        if(hasConverted) return new Resolved<>(id, Backend.TERRAPACK, null, loader.load(id));
        throw new IllegalArgumentException("Pack '" + id + "' was not found. Resolution Backend: NOT_FOUND\n" + diagnostics.get());
    }
}
