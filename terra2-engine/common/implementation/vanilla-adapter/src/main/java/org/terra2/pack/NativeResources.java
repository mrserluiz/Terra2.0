package org.terra2.pack;

import java.util.*;

/** Immutable source and data-fixed templates; activation is performed by the bound Paper runtime. */
public record NativeResources(ResourceBundle source, Map<String, byte[]> templates, int dataVersion) {
    public NativeResources {
        var copy = new TreeMap<String, byte[]>(); templates.forEach((path, bytes) -> copy.put(path, bytes.clone()));
        templates = Collections.unmodifiableMap(copy);
    }
    @Override public Map<String, byte[]> templates() {
        var copy = new TreeMap<String, byte[]>(); templates.forEach((path, bytes) -> copy.put(path, bytes.clone())); return Collections.unmodifiableMap(copy);
    }
}
