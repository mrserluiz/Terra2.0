package com.dfsek.terra.api.inventory;

import com.dfsek.terra.api.Handle;

public interface Item extends Handle {
   ItemStack newItemStack(int var1);

   double getMaxDurability();
}
