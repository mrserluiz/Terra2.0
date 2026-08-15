package com.dfsek.terra.api.inventory;

import com.dfsek.terra.api.Handle;

public interface Inventory extends Handle {
   void setItem(int var1, ItemStack var2);

   int getSize();

   ItemStack getItem(int var1);
}
