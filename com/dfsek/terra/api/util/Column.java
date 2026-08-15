package com.dfsek.terra.api.util;

import com.dfsek.terra.api.util.function.IntIntObjConsumer;
import com.dfsek.terra.api.util.function.IntObjConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface Column<T> {
   int getMinY();

   int getMaxY();

   int getX();

   int getZ();

   T get(int var1);

   default void forEach(Consumer<T> consumer) {
      for (int y = this.getMinY(); y < this.getMaxY(); y++) {
         consumer.accept(this.get(y));
      }
   }

   default void forEach(IntObjConsumer<T> consumer) {
      for (int y = this.getMinY(); y < this.getMaxY(); y++) {
         consumer.accept(y, this.get(y));
      }
   }

   default void forRanges(int resolution, IntIntObjConsumer<T> consumer) {
      int min = this.getMinY();
      int y = min;
      T runningObj = this.get(y);
      int runningMin = min;
      int max = this.getMaxY() - 1;

      while (true) {
         y += resolution;
         if (y > max) {
            consumer.accept(runningMin, this.getMaxY(), runningObj);
            return;
         }

         T current = this.get(y);
         if (!current.equals(runningObj)) {
            consumer.accept(runningMin, y, runningObj);
            runningMin = y;
            runningObj = current;
         }
      }
   }

   default List<? extends T> asList() {
      List<T> list = new ArrayList<>();
      this.forEach(list::add);
      return list;
   }
}
