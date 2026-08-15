package com.dfsek.terra.api.event.events.world.generation;

import com.dfsek.terra.api.block.entity.Container;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.event.events.AbstractCancellable;
import com.dfsek.terra.api.event.events.PackEvent;
import com.dfsek.terra.api.structure.LootTable;
import com.dfsek.terra.api.structure.Structure;
import com.dfsek.terra.api.util.vector.Vector3;
import org.jetbrains.annotations.NotNull;

public class LootPopulateEvent extends AbstractCancellable implements PackEvent {
   private final Container container;
   private final ConfigPack pack;
   private final Structure structure;
   private LootTable table;

   public LootPopulateEvent(Container container, LootTable table, ConfigPack pack, Structure structure) {
      this.container = container;
      this.table = table;
      this.pack = pack;
      this.structure = structure;
   }

   @Override
   public ConfigPack getPack() {
      return this.pack;
   }

   public Vector3 getPosition() {
      return this.container.getPosition();
   }

   public Container getContainer() {
      return this.container;
   }

   public LootTable getTable() {
      return this.table;
   }

   public void setTable(@NotNull LootTable table) {
      this.table = table;
   }

   public Structure getStructure() {
      return this.structure;
   }
}
