package com.dfsek.terra.bukkit.nms;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.terra2.adapter.vanilla.paper.WorldLootManager;

/** Intercepts only newly produced entity-table items, never equipment or an existing item drop. */
public final class NativeEntityLootBridge {
    private static final Set<LootTable> WRAPPED = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Field FUNCTION = field();
    private NativeEntityLootBridge() {}
    private static Field field() {
        try { var field = LootTable.class.getDeclaredField("compositeFunction"); field.setAccessible(true); return field; }
        catch(ReflectiveOperationException error) { throw new ExceptionInInitializerError(error); }
    }
    @SuppressWarnings("unchecked") public static synchronized void wrap(LootTable table, String key, WorldLootManager manager) {
        if(!table.getParamSet().equals(LootContextParamSets.ENTITY) || WRAPPED.contains(table)) return;
        try {
            var original = (BiFunction<ItemStack, LootContext, ItemStack>) FUNCTION.get(table);
            BiFunction<ItemStack, LootContext, ItemStack> scoped = (input, context) -> {
                var generated = original.apply(input, context);
                if(generated.isEmpty()) return generated;
                var output = manager.freshEntityLoot(context.getLevel().getWorld(), key, CraftItemStack.asBukkitCopy(generated));
                return output == null ? ItemStack.EMPTY : CraftItemStack.asNMSCopy(output);
            };
            FUNCTION.set(table, scoped); WRAPPED.add(table);
        } catch(ReflectiveOperationException error) { throw new IllegalStateException("Cannot install fresh entity loot hook for " + key, error); }
    }
}
