package org.terra2.api.visual;

import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Set;

/** Generator-neutral RGB requests. Colors are exact 24-bit values, never biome names. */
public record VisualProfile(String id, Map<String, Integer> colors) {
    public static final Set<String> ROLES = Set.of("grass", "foliage", "dry_foliage", "water", "sky", "fog", "water_fog");
    public VisualProfile {
        if(id == null || !id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) throw new IllegalArgumentException("Invalid visual profile ID");
        if(colors == null || colors.isEmpty()) throw new IllegalArgumentException("Empty visual profile");
        var copy = new TreeMap<String, Integer>();
        colors.forEach((role, rgb) -> {
            if(!ROLES.contains(role) || rgb == null || rgb < 0 || rgb > 0xFFFFFF)
                throw new IllegalArgumentException("Invalid RGB request: " + role);
            copy.put(role, rgb);
        });
        colors = Collections.unmodifiableMap(copy);
    }
}
