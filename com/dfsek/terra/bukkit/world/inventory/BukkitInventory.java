package com.dfsek.terra.bukkit.world.inventory;

import com.dfsek.terra.api.inventory.Inventory;
import com.dfsek.terra.api.inventory.ItemStack;

public class BukkitInventory implements Inventory {
   private final org.bukkit.inventory.Inventory delegate;

   public BukkitInventory(org.bukkit.inventory.Inventory delegate) {
      this.delegate = delegate;
   }

   @Override
   public void setItem(int slot, ItemStack newStack) {
      this.delegate.setItem(slot, ((BukkitItemStack)newStack).getHandle());
   }

   @Override
   public int getSize() {
      return this.delegate.getSize();
   }

   @Override
   public ItemStack getItem(int slot) {
      org.bukkit.inventory.ItemStack itemStack = this.delegate.getItem(slot);
      return itemStack == null ? null : new BukkitItemStack(itemStack);
   }

   public org.bukkit.inventory.Inventory getHandle() {
      return this.delegate;
   }
}
