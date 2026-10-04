package com.dfsek.terra.bukkit.nms;

import java.io.*;
import java.util.*;
import net.minecraft.nbt.*;
import org.terra2.pack.ResourceScope;

/** Rewrites resource identities after data fixing, preserving typed NBT and connector labels. */
public final class NativeTemplateScope {
    private NativeTemplateScope() {}
    public static CompoundTag rewrite(CompoundTag original, ResourceScope scope) {
        var copy = original.copy();
        visit(copy, scope, "", 0);
        return copy;
    }
    private static void visit(Tag value, ResourceScope scope, String context, int depth) {
        if(depth > 64) throw new IllegalArgumentException("Template NBT nesting exceeds budget");
        if(value instanceof ListTag list) {
            list.forEach(entry -> visit(entry, scope, context, depth + 1));
        } else if(value instanceof CompoundTag compound) {
            boolean jigsaw = compound.getStringOr("id", "").equals("minecraft:jigsaw");
            for(String key : new ArrayList<>(compound.keySet())) {
                Tag child = compound.get(key);
                if(child instanceof StringTag && (key.equals("LootTable") || key.equals("DeathLootTable")
                        || key.equals("loot_table") || key.equals("normal_config") || key.equals("ominous_config")
                        || (jigsaw && key.equals("pool")))) {
                    compound.putString(key, scope.resource(compound.getString(key).orElseThrow()));
                } else visit(child, scope, key, depth + 1);
                if(Set.of("minecraft:enchantments", "minecraft:stored_enchantments", "levels").contains(context)) {
                    String alias = key.contains(":") ? scope.resource(key) : key;
                    if(!alias.equals(key)) { compound.put(alias, compound.remove(key)); }
                }
            }
        }
    }
    public static CompoundTag read(byte[] bytes) throws IOException {
        var input = new ByteArrayInputStream(bytes);
        return bytes.length >= 2 && (bytes[0] & 255) == 31 && (bytes[1] & 255) == 139
            ? NbtIo.readCompressed(input, NbtAccounter.create(16L * 1024 * 1024))
            : NbtIo.read(new DataInputStream(input), NbtAccounter.create(16L * 1024 * 1024));
    }
    public static byte[] write(CompoundTag template) throws IOException {
        var output = new ByteArrayOutputStream(); NbtIo.writeCompressed(template, output); return output.toByteArray();
    }
}
