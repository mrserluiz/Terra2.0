package com.dfsek.terra.bukkit.nms;

import java.lang.reflect.Field;
import java.util.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
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
        try { var field = LootTable.class.getDeclaredField("modifier"); field.setAccessible(true); return field; }
        catch(ReflectiveOperationException error) { throw new ExceptionInInitializerError(error); }
    }
    @SuppressWarnings("unchecked") public static synchronized void wrap(LootTable table, String key, WorldLootManager manager) {
        if(!table.getParamSet().equals(LootContextParamSets.ENTITY) || WRAPPED.contains(table)) return;
        try {
            var original = (Optional<Holder<LootItemFunction>>) FUNCTION.get(table);
            LootItemFunction scoped = new LootItemFunction() {
                @Override public ItemStack apply(ItemStack input, LootContext context) {
                    var generated = original.map(holder -> holder.value().apply(input,context)).orElse(input);
                    if(generated.isEmpty()) return generated;
                    var output = manager.freshEntityLoot(context.getLevel().getWorld(), key, CraftItemStack.asBukkitCopy(generated));
                    return output == null ? ItemStack.EMPTY : CraftItemStack.asNMSCopy(output);
                }
                // The wrapper is runtime-only; it is never exported into the source datapack.
                @Override public MapCodec<? extends LootItemFunction> codec() { return MapCodec.unit(this); }
                @Override public void validate(ValidationContext context) {
                    original.ifPresent(holder -> holder.value().validate(context));
                }
                @Override public Set<net.minecraft.util.context.ContextKey<?>> getReferencedContextParams() {
                    return original.map(holder -> holder.value().getReferencedContextParams()).orElse(Set.of());
                }
            };
            FUNCTION.set(table, Optional.of(Holder.direct(scoped))); WRAPPED.add(table);
        } catch(ReflectiveOperationException error) { throw new IllegalStateException("Cannot install fresh entity loot hook for " + key, error); }
    }
}
