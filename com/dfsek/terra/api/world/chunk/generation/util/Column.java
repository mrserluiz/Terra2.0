package com.dfsek.terra.api.world.chunk.generation.util;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.structure.feature.BinaryColumn;
import com.dfsek.terra.api.util.function.IntToBooleanFunction;
import com.dfsek.terra.api.world.WritableWorld;
import java.util.function.IntConsumer;

public class Column<T extends WritableWorld> {
   private final int x;
   private final int z;
   private final int min;
   private final int max;
   private final T world;

   public Column(int x, int z, T world) {
      this(x, z, world, world.getMinHeight(), world.getMaxHeight());
   }

   public Column(int x, int z, T world, int min, int max) {
      this.x = x;
      this.z = z;
      this.world = world;
      this.max = max;
      this.min = min;
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }

   public BlockState getBlock(int y) {
      return this.world.getBlockState(this.x, y, this.z);
   }

   public T getWorld() {
      return this.world;
   }

   public int getMinY() {
      return this.min;
   }

   public int getMaxY() {
      return this.max;
   }

   public void forEach(IntConsumer function) {
      for (int y = this.world.getMinHeight(); y < this.world.getMaxHeight(); y++) {
         function.accept(y);
      }
   }

   public Column<T> clamp(int min, int max) {
      if (min >= max) {
         throw new IllegalArgumentException("Min greater than or equal to max: " + min + ", " + max);
      } else {
         return new Column<>(this.x, this.z, this.world, min, max);
      }
   }

   public BinaryColumn newBinaryColumn(IntToBooleanFunction function) {
      return new BinaryColumn(this.getMinY(), this.getMaxY(), function);
   }

   public Column.BinaryColumnBuilder newBinaryColumn() {
      return new Column.BinaryColumnBuilder(this);
   }

   public Column<T> adjacent(int offsetX, int offsetZ) {
      return (Column<T>)this.world.column(this.x + offsetX, this.z + offsetZ);
   }

   public static class BinaryColumnBuilder {
      private final boolean[] arr;
      private final Column<?> column;

      public BinaryColumnBuilder(Column<?> column) {
         this.column = column;
         this.arr = new boolean[column.getMaxY() - column.getMinY()];
      }

      public BinaryColumn build() {
         return new BinaryColumn(this.column.getMinY(), this.column.getMaxY(), this.arr);
      }

      public Column.BinaryColumnBuilder set(int y) {
         this.arr[y - this.column.getMinY()] = true;
         return this;
      }
   }
}
