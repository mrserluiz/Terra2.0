package com.dfsek.terra.api.util;

import java.util.Iterator;
import java.util.Random;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public interface Range extends Iterable<Integer> {
   Range multiply(int var1);

   Range reflect(int var1);

   int get(Random var1);

   Range intersects(Range var1);

   Range add(int var1);

   Range sub(int var1);

   @NotNull
   @Override
   Iterator<Integer> iterator();

   boolean isInRange(int var1);

   int getMax();

   Range setMax(int var1);

   int getMin();

   Range setMin(int var1);

   int getRange();

   default <T> T ifInRange(int y, T inRange, T notInRange) {
      return this.isInRange(y) ? inRange : notInRange;
   }

   default <T> T ifInRange(int y, Supplier<T> inRange, Supplier<T> notInRange) {
      return this.isInRange(y) ? inRange.get() : notInRange.get();
   }

   default <T> T ifInRange(int y, Supplier<T> inRange, T notInRange) {
      return this.isInRange(y) ? inRange.get() : notInRange;
   }

   default <T> T ifInRange(int y, T inRange, Supplier<T> notInRange) {
      return this.isInRange(y) ? inRange : notInRange.get();
   }
}
