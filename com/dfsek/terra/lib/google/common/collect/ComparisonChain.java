package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.InlineMe;
import java.util.Comparator;

@GwtCompatible
public abstract class ComparisonChain {
   private static final ComparisonChain ACTIVE = new ComparisonChain() {
      @Override
      public ComparisonChain compare(Comparable<?> left, Comparable<?> right) {
         return this.classify(((Comparable<Comparable<?>>)left).compareTo(right));
      }

      @Override
      public <T> ComparisonChain compare(@ParametricNullness T left, @ParametricNullness T right, Comparator<T> comparator) {
         return this.classify(comparator.compare(left, right));
      }

      @Override
      public ComparisonChain compare(int left, int right) {
         return this.classify(Integer.compare(left, right));
      }

      @Override
      public ComparisonChain compare(long left, long right) {
         return this.classify(Long.compare(left, right));
      }

      @Override
      public ComparisonChain compare(float left, float right) {
         return this.classify(Float.compare(left, right));
      }

      @Override
      public ComparisonChain compare(double left, double right) {
         return this.classify(Double.compare(left, right));
      }

      @Override
      public ComparisonChain compareTrueFirst(boolean left, boolean right) {
         return this.classify(Boolean.compare(right, left));
      }

      @Override
      public ComparisonChain compareFalseFirst(boolean left, boolean right) {
         return this.classify(Boolean.compare(left, right));
      }

      ComparisonChain classify(int result) {
         return result < 0 ? ComparisonChain.LESS : (result > 0 ? ComparisonChain.GREATER : ComparisonChain.ACTIVE);
      }

      @Override
      public int result() {
         return 0;
      }
   };
   private static final ComparisonChain LESS = new ComparisonChain.InactiveComparisonChain(-1);
   private static final ComparisonChain GREATER = new ComparisonChain.InactiveComparisonChain(1);

   private ComparisonChain() {
   }

   public static ComparisonChain start() {
      return ACTIVE;
   }

   public abstract ComparisonChain compare(Comparable<?> left, Comparable<?> right);

   public abstract <T> ComparisonChain compare(@ParametricNullness T left, @ParametricNullness T right, Comparator<T> comparator);

   public abstract ComparisonChain compare(int left, int right);

   public abstract ComparisonChain compare(long left, long right);

   public abstract ComparisonChain compare(float left, float right);

   public abstract ComparisonChain compare(double left, double right);

   @Deprecated
   @InlineMe(replacement = "this.compareFalseFirst(left, right)")
   public final ComparisonChain compare(Boolean left, Boolean right) {
      return this.compareFalseFirst(left, right);
   }

   public abstract ComparisonChain compareTrueFirst(boolean left, boolean right);

   public abstract ComparisonChain compareFalseFirst(boolean left, boolean right);

   public abstract int result();

   private static final class InactiveComparisonChain extends ComparisonChain {
      final int result;

      InactiveComparisonChain(int result) {
         this.result = result;
      }

      @Override
      public ComparisonChain compare(Comparable<?> left, Comparable<?> right) {
         return this;
      }

      @Override
      public <T> ComparisonChain compare(@ParametricNullness T left, @ParametricNullness T right, Comparator<T> comparator) {
         return this;
      }

      @Override
      public ComparisonChain compare(int left, int right) {
         return this;
      }

      @Override
      public ComparisonChain compare(long left, long right) {
         return this;
      }

      @Override
      public ComparisonChain compare(float left, float right) {
         return this;
      }

      @Override
      public ComparisonChain compare(double left, double right) {
         return this;
      }

      @Override
      public ComparisonChain compareTrueFirst(boolean left, boolean right) {
         return this;
      }

      @Override
      public ComparisonChain compareFalseFirst(boolean left, boolean right) {
         return this;
      }

      @Override
      public int result() {
         return this.result;
      }
   }
}
