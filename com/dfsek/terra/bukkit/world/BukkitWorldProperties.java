package com.dfsek.terra.bukkit.world;

import com.dfsek.terra.api.world.info.WorldProperties;
import org.bukkit.generator.WorldInfo;

public class BukkitWorldProperties implements WorldProperties {
   private final WorldInfo delegate;

   public BukkitWorldProperties(WorldInfo delegate) {
      this.delegate = delegate;
   }

   @Override
   public Object getHandle() {
      return this.delegate;
   }

   @Override
   public long getSeed() {
      return this.delegate.getSeed();
   }

   @Override
   public int getMaxHeight() {
      return this.delegate.getMaxHeight();
   }

   @Override
   public int getMinHeight() {
      return this.delegate.getMinHeight();
   }

   @Override
   public int hashCode() {
      return this.delegate.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof WorldProperties that ? this.delegate.equals(that.getHandle()) : false;
   }
}
