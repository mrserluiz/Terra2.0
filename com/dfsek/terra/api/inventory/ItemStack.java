package com.dfsek.terra.api.inventory;

import com.dfsek.terra.api.Handle;
import com.dfsek.terra.api.inventory.item.Damageable;
import com.dfsek.terra.api.inventory.item.ItemMeta;

public interface ItemStack extends Handle {
   int getAmount();

   void setAmount(int var1);

   Item getType();

   ItemMeta getItemMeta();

   void setItemMeta(ItemMeta var1);

   default boolean isDamageable() {
      return this.getItemMeta() instanceof Damageable;
   }
}
