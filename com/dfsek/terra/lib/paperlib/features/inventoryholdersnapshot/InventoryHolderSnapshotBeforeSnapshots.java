package com.dfsek.terra.lib.paperlib.features.inventoryholdersnapshot;

import org.bukkit.inventory.Inventory;

public class InventoryHolderSnapshotBeforeSnapshots implements InventoryHolderSnapshot {
   @Override
   public InventoryHolderSnapshotResult getHolder(Inventory inventory, boolean useSnapshot) {
      return new InventoryHolderSnapshotResult(false, inventory.getHolder());
   }
}
