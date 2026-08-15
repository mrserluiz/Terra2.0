package com.dfsek.terra.api.block.state;

import com.dfsek.terra.api.Handle;
import com.dfsek.terra.api.block.BlockType;
import com.dfsek.terra.api.block.state.properties.Property;
import java.util.function.Consumer;

public interface BlockState extends Handle {
   boolean matches(BlockState var1);

   <T extends Comparable<T>> boolean has(Property<T> var1);

   <T extends Comparable<T>> T get(Property<T> var1);

   <T extends Comparable<T>> BlockState set(Property<T> var1, T var2);

   default <T extends Comparable<T>> BlockState ifProperty(Property<T> property, Consumer<BlockState> action) {
      if (this.has(property)) {
         action.accept(this);
      }

      return this;
   }

   default <T extends Comparable<T>> BlockState setIfPresent(Property<T> property, T value) {
      if (this.has(property)) {
         this.set(property, value);
      }

      return this;
   }

   BlockType getBlockType();

   default String getAsString() {
      return this.getAsString(true);
   }

   String getAsString(boolean var1);

   boolean isAir();
}
