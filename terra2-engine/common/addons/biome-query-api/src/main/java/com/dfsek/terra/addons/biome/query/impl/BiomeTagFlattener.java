package com.dfsek.terra.addons.biome.query.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BiomeTagFlattener {
    private final Map<String, Integer> indices;
    private final int size;

    public BiomeTagFlattener(List<String> tags) {
        Map<String, Integer> indexed = new HashMap<>();
        for(int i = 0; i < tags.size(); i++) indexed.putIfAbsent(tags.get(i), i);
        this.indices = Map.copyOf(indexed);
        this.size = tags.size();
    }

    public int index(String tag) {
        return indices.getOrDefault(tag, -1);
    }

    public int size() {
        return size;
    }
}
