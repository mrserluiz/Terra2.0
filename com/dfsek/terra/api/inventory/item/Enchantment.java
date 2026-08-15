package com.dfsek.terra.api.inventory.item;

import com.dfsek.terra.api.Handle;
import com.dfsek.terra.api.inventory.ItemStack;

public interface Enchantment extends Handle {
   boolean canEnchantItem(ItemStack var1);

   boolean conflictsWith(Enchantment var1);

   String getID();

   int getMaxLevel();
}
