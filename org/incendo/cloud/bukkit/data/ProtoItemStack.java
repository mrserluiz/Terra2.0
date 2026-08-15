package org.incendo.cloud.bukkit.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ProtoItemStack {
   @NonNull Material material();

   boolean hasExtraData();

   @NonNull ItemStack createItemStack(int stackSize, boolean respectMaximumStackSize) throws IllegalArgumentException;
}
