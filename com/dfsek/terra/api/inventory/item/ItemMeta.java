package com.dfsek.terra.api.inventory.item;

import com.dfsek.terra.api.Handle;
import java.util.Map;

public interface ItemMeta extends Handle {
   void addEnchantment(Enchantment var1, int var2);

   Map<Enchantment, Integer> getEnchantments();
}
