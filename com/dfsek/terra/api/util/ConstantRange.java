package com.dfsek.terra.api.util;

import java.util.Iterator;
import java.util.Random;
import org.jetbrains.annotations.NotNull;

public class ConstantRange implements Range {
   private int min;
   private int max;

   public ConstantRange(int min, int max) {
      if (min >= max) {
         throw new IllegalArgumentException("Minimum must not be greater than or equal to maximum!");
      }

      this.max = max;
      this.min = min;
   }

   @Override
   public Range multiply(int mult) {
      this.min *= mult;
      this.max *= mult;
      return this;
   }

   @Override
   public Range reflect(int pt) {
      return new ConstantRange(2 * pt - this.getMax(), 2 * pt - this.getMin());
   }

   @Override
   public int get(Random r) {
      return r.nextInt(this.min, this.max);
   }

   @Override
   public Range intersects(Range other) {
      try {
         return new ConstantRange(Math.max(this.getMin(), other.getMin()), Math.min(this.getMax(), other.getMax()));
      } catch (IllegalArgumentException e) {
         return null;
      }
   }

   @Override
   public Range add(int add) {
      this.min += add;
      this.max += add;
      return this;
   }

   @Override
   public Range sub(int sub) {
      this.min -= sub;
      this.max -= sub;
      return this;
   }

   @NotNull
   @Override
   public Iterator<Integer> iterator() {
      return new ConstantRange.RangeIterator(this);
   }

   @Override
   public boolean isInRange(int test) {
      return test >= this.min && test < this.max;
   }

   @Override
   public int getMax() {
      return this.max;
   }

   @Override
   public Range setMax(int max) {
      this.max = max;
      return this;
   }

   @Override
   public int getMin() {
      return this.min;
   }

   @Override
   public Range setMin(int min) {
      this.min = min;
      return this;
   }

   @Override
   public int getRange() {
      return this.max - this.min;
   }

   @Override
   public int hashCode() {
      return this.min * 31 + this.max;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ConstantRange)) {
         return false;
      }

      Range other = (Range)obj;
      return other.getMin() == this.getMin() && other.getMax() == this.getMax();
   }

   @Override
   public String toString() {
      return "Min: " + this.getMin() + ", Max:" + this.getMax();
   }

   private static class RangeIterator implements Iterator<Integer> {
      private final Range m;
      private Integer current;

      public RangeIterator(Range m) {
         this.m = m;
         this.current = m.getMin();
      }

      @Override
      public boolean hasNext() {
         return this.current < this.m.getMax();
      }

      public Integer next() {
         Integer var1 = this.current;
         this.current = this.current + 1;
         return this.current - 1;
      }
   }
}
