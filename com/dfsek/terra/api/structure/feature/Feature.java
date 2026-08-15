package com.dfsek.terra.api.structure.feature;

import com.dfsek.terra.api.registry.key.StringIdentifiable;
import com.dfsek.terra.api.structure.Structure;
import com.dfsek.terra.api.world.WritableWorld;

public interface Feature extends StringIdentifiable {
   Structure getStructure(WritableWorld var1, int var2, int var3, int var4);

   Distributor getDistributor();

   Locator getLocator();
}
