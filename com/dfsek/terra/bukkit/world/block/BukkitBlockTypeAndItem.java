package com.dfsek.terra.bukkit.world.block;

import com.dfsek.terra.api.block.BlockType;
import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.inventory.Item;
import com.dfsek.terra.api.inventory.ItemStack;
import com.dfsek.terra.bukkit.world.BukkitAdapter;
import org.bukkit.Material;

public class BukkitBlockTypeAndItem implements BlockType, Item {
   private final Material delegate;

   public BukkitBlockTypeAndItem(Material delegate) {
      this.delegate = delegate;
   }

   public Material getHandle() {
      return this.delegate;
   }

   @Override
   public BlockState getDefaultState() {
      return BukkitAdapter.adapt(this.delegate.createBlockData());
   }

   @Override
   public boolean isSolid() {
      return this.delegate.isOccluding();
   }

   @Override
   public boolean isWater() {
      return this.delegate == Material.WATER;
   }

   @Override
   public ItemStack newItemStack(int amount) {
      return BukkitAdapter.adapt(new org.bukkit.inventory.ItemStack(this.delegate, amount));
   }

   @Override
   public double getMaxDurability() {
      return this.delegate.getMaxDurability();
   }

   @Override
   public int hashCode() {
      return this.delegate.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof BukkitBlockTypeAndItem) ? false : this.delegate == ((BukkitBlockTypeAndItem)obj).delegate;
   }
}
