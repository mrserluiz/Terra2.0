package com.dfsek.terra.api.structure;

import com.dfsek.terra.api.inventory.Inventory;
import com.dfsek.terra.api.inventory.ItemStack;
import java.util.List;
import java.util.Random;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public interface LootTable {
   void fillInventory(Inventory var1, Random var2);

   List<ItemStack> getLoot(Random var1);
}
