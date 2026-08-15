package com.dfsek.terra.api.structure.feature;

import com.dfsek.terra.api.util.Range;
import com.dfsek.terra.api.util.function.IntToBooleanFunction;
import com.dfsek.terra.api.util.generic.Lazy;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

public class BinaryColumn {
   private static final BinaryColumn NULL = new BinaryColumn(0, 1, y -> false);
   private final IntToBooleanFunction data;
   private final int minY;
   private final int maxY;
   private final Lazy<boolean[]> results;

   public BinaryColumn(int minY, int maxY, IntToBooleanFunction data) {
      this.minY = minY;
      this.maxY = maxY;
      this.results = Lazy.lazy(() -> {
         boolean[] res = new boolean[maxY - minY];

         for (int y = minY; y < maxY; y++) {
            res[y - minY] = this.get(y);
         }

         return res;
      });
      if (maxY <= minY) {
         throw new IllegalArgumentException("Max y must be greater than min y");
      }

      this.data = data;
   }

   public BinaryColumn(int minY, int maxY, boolean[] data) {
      this.minY = minY;
      this.maxY = maxY;
      this.results = Lazy.lazy(() -> data);
      if (maxY <= minY) {
         throw new IllegalArgumentException("Max y must be greater than min y");
      }

      this.data = y -> data[y - minY];
   }

   public BinaryColumn(Range y, IntToBooleanFunction data) {
      this(y.getMin(), y.getMax(), data);
   }

   public static BinaryColumn getNull() {
      return NULL;
   }

   public boolean get(int y) {
      return this.data.apply(y);
   }

   public boolean contains(int y) {
      return y >= this.minY && y < this.maxY;
   }

   public void forEach(IntConsumer consumer) {
      boolean[] results = this.results.value();

      for (int y = this.minY; y < this.maxY; y++) {
         if (results[y - this.minY]) {
            consumer.accept(y);
         }
      }
   }

   public BinaryColumn and(BinaryColumn that) {
      int bigMinY = Math.max(this.minY, that.minY);
      int smallMaxY = Math.min(this.maxY, that.maxY);
      return bigMinY >= smallMaxY ? getNull() : new BinaryColumn(bigMinY, smallMaxY, y -> this.get(y) && that.get(y));
   }

   public BinaryColumn or(BinaryColumn that) {
      return this.or(that, (a, b) -> a.getAsBoolean() || b.getAsBoolean());
   }

   public BinaryColumn xor(BinaryColumn that) {
      return this.or(that, (a, b) -> a.getAsBoolean() ^ b.getAsBoolean());
   }

   private BinaryColumn or(BinaryColumn that, BinaryColumn.BooleanBinaryOperator operator) {
      int smallMinY = Math.min(this.minY, that.minY);
      int bigMaxY = Math.max(this.maxY, that.maxY);
      return new BinaryColumn(smallMinY, bigMaxY, y -> operator.apply(() -> this.get(y), () -> that.get(y)));
   }

   private interface BooleanBinaryOperator {
      boolean apply(BooleanSupplier var1, BooleanSupplier var2);
   }
}
