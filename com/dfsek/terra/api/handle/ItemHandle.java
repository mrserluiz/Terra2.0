package com.dfsek.terra.api.handle;

import com.dfsek.terra.api.inventory.Item;
import com.dfsek.terra.api.inventory.item.Enchantment;
import java.util.Set;

public interface ItemHandle {
   Item createItem(String var1);

   Enchantment getEnchantment(String var1);

   Set<Enchantment> getEnchantments();
}
