package com.dfsek.terra.api.structure.configured;

import com.dfsek.terra.api.registry.key.StringIdentifiable;
import com.dfsek.terra.api.structure.Structure;
import com.dfsek.terra.api.structure.StructureSpawn;
import com.dfsek.terra.api.util.Range;
import com.dfsek.terra.api.util.collection.ProbabilityCollection;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public interface ConfiguredStructure extends StringIdentifiable {
   ProbabilityCollection<Structure> getStructure();

   Range getSpawnStart();

   StructureSpawn getSpawn();
}
