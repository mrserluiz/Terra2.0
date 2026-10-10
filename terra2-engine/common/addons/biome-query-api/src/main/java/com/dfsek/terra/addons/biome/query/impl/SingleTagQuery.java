package com.dfsek.terra.addons.biome.query.impl;

import java.util.function.Predicate;

import com.dfsek.terra.addons.biome.query.BiomeQueryAPIAddon;
import com.dfsek.terra.api.world.biome.Biome;


public class SingleTagQuery implements Predicate<Biome> {
    private final String tag;
    public SingleTagQuery(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(Biome biome) {
        var holder = biome.getContext().get(BiomeQueryAPIAddon.BIOME_TAG_KEY);
        // Indices belong to one pack's tag table. A missing tag never matches.
        int index = holder.getFlattener().index(tag);
        return index >= 0 && holder.get(index);
    }
}
