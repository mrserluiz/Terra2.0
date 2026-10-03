package com.dfsek.terra.bukkit.world.block.data;

import java.util.regex.Pattern;

/** Separates supported legacy block entity data from Bukkit block properties. */
public record LegacyBlockSpec(String blockData, String lootTable) {
    private static final Pattern LOOT = Pattern.compile("\\{\\s*LootTable\\s*:\\s*(['\"])([a-z0-9_./:-]+)\\1\\s*\\}");

    public static LegacyBlockSpec parse(String source) {
        int start = source.indexOf('{');
        if(start < 0) return new LegacyBlockSpec(source, null);
        var match = LOOT.matcher(source.substring(start));
        if(!match.matches()) throw new IllegalArgumentException("Unsupported block entity data in " + source);
        String table = match.group(2);
        if(!table.contains(":")) table = "minecraft:" + table;
        // Older community scripts contain this misspelling of the vanilla trail ruins table.
        if(table.equals("minecraft:archaeology/trial_ruins_rare")) table = "minecraft:archaeology/trail_ruins_rare";
        if(table.equals("minecraft:archaeology/trial_ruins_common")) table = "minecraft:archaeology/trail_ruins_common";
        return new LegacyBlockSpec(source.substring(0, start), table);
    }
}
