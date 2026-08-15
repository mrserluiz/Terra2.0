package com.dfsek.terra.lib.commons.io.file;

import java.math.BigInteger;
import java.util.Objects;

public class Counters {
   public static Counters.Counter bigIntegerCounter() {
      return new Counters.BigIntegerCounter();
   }

   public static Counters.PathCounters bigIntegerPathCounters() {
      return new Counters.BigIntegerPathCounters();
   }

   public static Counters.Counter longCounter() {
      return new Counters.LongCounter();
   }

   public static Counters.PathCounters longPathCounters() {
      return new Counters.LongPathCounters();
   }

   public static Counters.Counter noopCounter() {
      return Counters.NoopCounter.INSTANCE;
   }

   public static Counters.PathCounters noopPathCounters() {
      return Counters.NoopPathCounters.INSTANCE;
   }

   private static class AbstractPathCounters implements Counters.PathCounters {
      private final Counters.Counter byteCounter;
      private final Counters.Counter directoryCounter;
      private final Counters.Counter fileCounter;

      protected AbstractPathCounters(Counters.Counter byteCounter, Counters.Counter directoryCounter, Counters.Counter fileCounter) {
         this.byteCounter = byteCounter;
         this.directoryCounter = directoryCounter;
         this.fileCounter = fileCounter;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }

         if (!(obj instanceof Counters.AbstractPathCounters)) {
            return false;
         }

         Counters.AbstractPathCounters other = (Counters.AbstractPathCounters)obj;
         return Objects.equals(this.byteCounter, other.byteCounter)
            && Objects.equals(this.directoryCounter, other.directoryCounter)
            && Objects.equals(this.fileCounter, other.fileCounter);
      }

      @Override
      public Counters.Counter getByteCounter() {
         return this.byteCounter;
      }

      @Override
      public Counters.Counter getDirectoryCounter() {
         return this.directoryCounter;
      }

      @Override
      public Counters.Counter getFileCounter() {
         return this.fileCounter;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.byteCounter, this.directoryCounter, this.fileCounter);
      }

      @Override
      public void reset() {
         this.byteCounter.reset();
         this.directoryCounter.reset();
         this.fileCounter.reset();
      }

      @Override
      public String toString() {
         return String.format("%,d files, %,d directories, %,d bytes", this.fileCounter.get(), this.directoryCounter.get(), this.byteCounter.get());
      }
   }

   private static final class BigIntegerCounter implements Counters.Counter {
      private BigInteger value = BigInteger.ZERO;

      private BigIntegerCounter() {
      }

      @Override
      public void add(long val) {
         this.value = this.value.add(BigInteger.valueOf(val));
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }

         if (!(obj instanceof Counters.Counter)) {
            return false;
         }

         Counters.Counter other = (Counters.Counter)obj;
         return Objects.equals(this.value, other.getBigInteger());
      }

      @Override
      public long get() {
         return this.value.longValueExact();
      }

      @Override
      public BigInteger getBigInteger() {
         return this.value;
      }

      @Override
      public Long getLong() {
         return this.value.longValueExact();
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.value);
      }

      @Override
      public void increment() {
         this.value = this.value.add(BigInteger.ONE);
      }

      @Override
      public void reset() {
         this.value = BigInteger.ZERO;
      }

      @Override
      public String toString() {
         return this.value.toString();
      }
   }

   private static final class BigIntegerPathCounters extends Counters.AbstractPathCounters {
      protected BigIntegerPathCounters() {
         super(Counters.bigIntegerCounter(), Counters.bigIntegerCounter(), Counters.bigIntegerCounter());
      }
   }

   public interface Counter {
      void add(long var1);

      long get();

      BigInteger getBigInteger();

      Long getLong();

      void increment();

      default void reset() {
      }
   }

   private static final class LongCounter implements Counters.Counter {
      private long value;

      private LongCounter() {
      }

      @Override
      public void add(long add) {
         this.value += add;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }

         if (!(obj instanceof Counters.Counter)) {
            return false;
         }

         Counters.Counter other = (Counters.Counter)obj;
         return this.value == other.get();
      }

      @Override
      public long get() {
         return this.value;
      }

      @Override
      public BigInteger getBigInteger() {
         return BigInteger.valueOf(this.value);
      }

      @Override
      public Long getLong() {
         return this.value;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.value);
      }

      @Override
      public void increment() {
         this.value++;
      }

      @Override
      public void reset() {
         this.value = 0L;
      }

      @Override
      public String toString() {
         return Long.toString(this.value);
      }
   }

   private static final class LongPathCounters extends Counters.AbstractPathCounters {
      protected LongPathCounters() {
         super(Counters.longCounter(), Counters.longCounter(), Counters.longCounter());
      }
   }

   private static final class NoopCounter implements Counters.Counter {
      static final Counters.NoopCounter INSTANCE = new Counters.NoopCounter();

      @Override
      public void add(long add) {
      }

      @Override
      public long get() {
         return 0L;
      }

      @Override
      public BigInteger getBigInteger() {
         return BigInteger.ZERO;
      }

      @Override
      public Long getLong() {
         return 0L;
      }

      @Override
      public void increment() {
      }

      @Override
      public String toString() {
         return "0";
      }
   }

   private static final class NoopPathCounters extends Counters.AbstractPathCounters {
      static final Counters.NoopPathCounters INSTANCE = new Counters.NoopPathCounters();

      private NoopPathCounters() {
         super(Counters.noopCounter(), Counters.noopCounter(), Counters.noopCounter());
      }
   }

   public interface PathCounters {
      Counters.Counter getByteCounter();

      Counters.Counter getDirectoryCounter();

      Counters.Counter getFileCounter();

      default void reset() {
      }
   }
}
