package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.primitives.Ints;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.NoSuchElementException;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class DiscreteDomain<C extends Comparable> {
   final boolean supportsFastOffset;

   public static DiscreteDomain<Integer> integers() {
      return DiscreteDomain.IntegerDomain.INSTANCE;
   }

   public static DiscreteDomain<Long> longs() {
      return DiscreteDomain.LongDomain.INSTANCE;
   }

   public static DiscreteDomain<BigInteger> bigIntegers() {
      return DiscreteDomain.BigIntegerDomain.INSTANCE;
   }

   protected DiscreteDomain() {
      this(false);
   }

   private DiscreteDomain(boolean supportsFastOffset) {
      this.supportsFastOffset = supportsFastOffset;
   }

   C offset(C origin, long distance) {
      C current = origin;
      CollectPreconditions.checkNonnegative(distance, "distance");

      for (long i = 0L; i < distance; i++) {
         current = this.next(current);
         if (current == null) {
            throw new IllegalArgumentException("overflowed computing offset(" + origin + ", " + distance + ")");
         }
      }

      return current;
   }

   public abstract @Nullable C next(C value);

   public abstract @Nullable C previous(C value);

   public abstract long distance(C start, C end);

   @CanIgnoreReturnValue
   public C minValue() {
      throw new NoSuchElementException();
   }

   @CanIgnoreReturnValue
   public C maxValue() {
      throw new NoSuchElementException();
   }

   private static final class BigIntegerDomain extends DiscreteDomain<BigInteger> implements Serializable {
      private static final DiscreteDomain.BigIntegerDomain INSTANCE = new DiscreteDomain.BigIntegerDomain();
      private static final BigInteger MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);
      private static final BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      BigIntegerDomain() {
         super(true);
      }

      public BigInteger next(BigInteger value) {
         return value.add(BigInteger.ONE);
      }

      public BigInteger previous(BigInteger value) {
         return value.subtract(BigInteger.ONE);
      }

      BigInteger offset(BigInteger origin, long distance) {
         CollectPreconditions.checkNonnegative(distance, "distance");
         return origin.add(BigInteger.valueOf(distance));
      }

      public long distance(BigInteger start, BigInteger end) {
         return end.subtract(start).max(MIN_LONG).min(MAX_LONG).longValue();
      }

      private Object readResolve() {
         return INSTANCE;
      }

      @Override
      public String toString() {
         return "DiscreteDomain.bigIntegers()";
      }
   }

   private static final class IntegerDomain extends DiscreteDomain<Integer> implements Serializable {
      private static final DiscreteDomain.IntegerDomain INSTANCE = new DiscreteDomain.IntegerDomain();
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      IntegerDomain() {
         super(true);
      }

      public @Nullable Integer next(Integer value) {
         int i = value;
         return i == Integer.MAX_VALUE ? null : i + 1;
      }

      public @Nullable Integer previous(Integer value) {
         int i = value;
         return i == Integer.MIN_VALUE ? null : i - 1;
      }

      Integer offset(Integer origin, long distance) {
         CollectPreconditions.checkNonnegative(distance, "distance");
         return Ints.checkedCast(origin.longValue() + distance);
      }

      public long distance(Integer start, Integer end) {
         return (long)end.intValue() - start.intValue();
      }

      public Integer minValue() {
         return Integer.MIN_VALUE;
      }

      public Integer maxValue() {
         return Integer.MAX_VALUE;
      }

      private Object readResolve() {
         return INSTANCE;
      }

      @Override
      public String toString() {
         return "DiscreteDomain.integers()";
      }
   }

   private static final class LongDomain extends DiscreteDomain<Long> implements Serializable {
      private static final DiscreteDomain.LongDomain INSTANCE = new DiscreteDomain.LongDomain();
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      LongDomain() {
         super(true);
      }

      public @Nullable Long next(Long value) {
         long l = value;
         return l == Long.MAX_VALUE ? null : l + 1L;
      }

      public @Nullable Long previous(Long value) {
         long l = value;
         return l == Long.MIN_VALUE ? null : l - 1L;
      }

      Long offset(Long origin, long distance) {
         CollectPreconditions.checkNonnegative(distance, "distance");
         long result = origin + distance;
         if (result < 0L) {
            Preconditions.checkArgument(origin < 0L, "overflow");
         }

         return result;
      }

      public long distance(Long start, Long end) {
         long result = end - start;
         if (end > start && result < 0L) {
            return Long.MAX_VALUE;
         } else {
            return end < start && result > 0L ? Long.MIN_VALUE : result;
         }
      }

      public Long minValue() {
         return Long.MIN_VALUE;
      }

      public Long maxValue() {
         return Long.MAX_VALUE;
      }

      private Object readResolve() {
         return INSTANCE;
      }

      @Override
      public String toString() {
         return "DiscreteDomain.longs()";
      }
   }
}
