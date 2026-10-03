package com.dfsek.terra.bukkit.util;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import com.dfsek.terra.api.entity.EntityType;
import com.dfsek.terra.bukkit.world.entity.BukkitEntityType;


public class BukkitUtils {
    private static final Logger logger = LoggerFactory.getLogger(BukkitUtils.class);

    public static boolean isLiquid(BlockData blockState) {
        Material material = blockState.getMaterial();
        return material == Material.WATER || material == Material.LAVA;
    }

    public static EntityType getEntityType(String id) {
        if(!id.startsWith("minecraft:")) throw new IllegalArgumentException("Invalid entity identifier " + id);
        Boolean showBottom = null;
        int tagStart = id.indexOf('{');
        if(tagStart >= 0) {
            String tag = id.substring(tagStart);
            var matcher = java.util.regex.Pattern.compile("\\{\\s*ShowBottom\\s*:\\s*([01])[bB]?\\s*}").matcher(tag);
            String base = id.substring(0, tagStart);
            if(!base.equals("minecraft:end_crystal") || !matcher.matches()) {
                throw new IllegalArgumentException("Unsupported entity data: " + id);
            }
            showBottom = matcher.group(1).equals("1");
            id = base;
        }
        String entityID = id.toUpperCase(Locale.ROOT).substring(10);

        return new BukkitEntityType(switch(entityID) {
            case "END_CRYSTAL" -> org.bukkit.entity.EntityType.END_CRYSTAL;
            case "ENDER_CRYSTAL" -> throw new IllegalArgumentException(
                "Invalid entity identifier " + id); // make sure this issue can't happen the other way around.
            default -> org.bukkit.entity.EntityType.valueOf(entityID);
        }, showBottom);
    }
}
