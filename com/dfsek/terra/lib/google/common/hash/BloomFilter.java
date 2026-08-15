package com.dfsek.terra.lib.google.common.hash;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Objects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Predicate;
import com.dfsek.terra.lib.google.common.math.DoubleMath;
import com.dfsek.terra.lib.google.common.math.LongMath;
import com.dfsek.terra.lib.google.common.primitives.SignedBytes;
import com.dfsek.terra.lib.google.common.primitives.UnsignedBytes;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.InlineMe;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.RoundingMode;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import org.jspecify.annotations.Nullable;

@Beta
public final class BloomFilter<T> implements Predicate<T>, Serializable {
   private final BloomFilterStrategies.LockFreeBitArray bits;
   private final int numHashFunctions;
   private final Funnel<? super T> funnel;
   private final BloomFilter.Strategy strategy;
   private static final double LOG_TWO = Math.log(2.0);
   private static final double SQUARED_LOG_TWO = LOG_TWO * LOG_TWO;
   private static final long serialVersionUID = -889275714L;

   private BloomFilter(BloomFilterStrategies.LockFreeBitArray bits, int numHashFunctions, Funnel<? super T> funnel, BloomFilter.Strategy strategy) {
      Preconditions.checkArgument(numHashFunctions > 0, "numHashFunctions (%s) must be > 0", numHashFunctions);
      Preconditions.checkArgument(numHashFunctions <= 255, "numHashFunctions (%s) must be <= 255", numHashFunctions);
      this.bits = Preconditions.checkNotNull(bits);
      this.numHashFunctions = numHashFunctions;
      this.funnel = Preconditions.checkNotNull(funnel);
      this.strategy = Preconditions.checkNotNull(strategy);
   }

   public BloomFilter<T> copy() {
      return new BloomFilter<>(this.bits.copy(), this.numHashFunctions, this.funnel, this.strategy);
   }

   public boolean mightContain(@ParametricNullness T object) {
      return this.strategy.mightContain(object, this.funnel, this.numHashFunctions, this.bits);
   }

   @Deprecated
   @InlineMe(replacement = "this.mightContain(input)")
   @Override
   public boolean apply(@ParametricNullness T input) {
      return this.mightContain(input);
   }

   @Deprecated
   @InlineMe(replacement = "this.mightContain(input)")
   @Override
   public boolean test(@ParametricNullness T input) {
      return this.mightContain(input);
   }

   @CanIgnoreReturnValue
   public boolean put(@ParametricNullness T object) {
      return this.strategy.put(object, this.funnel, this.numHashFunctions, this.bits);
   }

   public double expectedFpp() {
      return Math.pow((double)this.bits.bitCount() / this.bitSize(), this.numHashFunctions);
   }

   public long approximateElementCount() {
      long bitSize = this.bits.bitSize();
      long bitCount = this.bits.bitCount();
      double fractionOfBitsSet = (double)bitCount / bitSize;
      return DoubleMath.roundToLong(-Math.log1p(-fractionOfBitsSet) * bitSize / this.numHashFunctions, RoundingMode.HALF_UP);
   }

   @VisibleForTesting
   long bitSize() {
      return this.bits.bitSize();
   }

   public boolean isCompatible(BloomFilter<T> that) {
      Preconditions.checkNotNull(that);
      return this != that
         && this.numHashFunctions == that.numHashFunctions
         && this.bitSize() == that.bitSize()
         && this.strategy.equals(that.strategy)
         && this.funnel.equals(that.funnel);
   }

   public void putAll(BloomFilter<T> that) {
      Preconditions.checkNotNull(that);
      Preconditions.checkArgument(this != that, "Cannot combine a BloomFilter with itself.");
      Preconditions.checkArgument(
         this.numHashFunctions == that.numHashFunctions,
         "BloomFilters must have the same number of hash functions (%s != %s)",
         this.numHashFunctions,
         that.numHashFunctions
      );
      Preconditions.checkArgument(
         this.bitSize() == that.bitSize(), "BloomFilters must have the same size underlying bit arrays (%s != %s)", this.bitSize(), that.bitSize()
      );
      Preconditions.checkArgument(this.strategy.equals(that.strategy), "BloomFilters must have equal strategies (%s != %s)", this.strategy, that.strategy);
      Preconditions.checkArgument(this.funnel.equals(that.funnel), "BloomFilters must have equal funnels (%s != %s)", this.funnel, that.funnel);
      this.bits.putAll(that.bits);
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (object == this) {
         return true;
      }

      if (!(object instanceof BloomFilter)) {
         return false;
      }

      BloomFilter<?> that = (BloomFilter<?>)object;
      return this.numHashFunctions == that.numHashFunctions
         && this.funnel.equals(that.funnel)
         && this.bits.equals(that.bits)
         && this.strategy.equals(that.strategy);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.numHashFunctions, this.funnel, this.strategy, this.bits);
   }

   public static <T> Collector<T, ?, BloomFilter<T>> toBloomFilter(Funnel<? super T> funnel, long expectedInsertions) {
      return toBloomFilter(funnel, expectedInsertions, 0.03);
   }

   public static <T> Collector<T, ?, BloomFilter<T>> toBloomFilter(Funnel<? super T> funnel, long expectedInsertions, double fpp) {
      Preconditions.checkNotNull(funnel);
      Preconditions.checkArgument(expectedInsertions >= 0L, "Expected insertions (%s) must be >= 0", expectedInsertions);
      Preconditions.checkArgument(fpp > 0.0, "False positive probability (%s) must be > 0.0", fpp);
      Preconditions.checkArgument(fpp < 1.0, "False positive probability (%s) must be < 1.0", fpp);
      return Collector.of(() -> create(funnel, expectedInsertions, fpp), BloomFilter::put, (bf1, bf2) -> {
         bf1.putAll(bf2);
         return bf1;
      }, Characteristics.UNORDERED, Characteristics.CONCURRENT);
   }

   public static <T> BloomFilter<T> create(Funnel<? super T> funnel, int expectedInsertions, double fpp) {
      return create(funnel, (long)expectedInsertions, fpp);
   }

   public static <T> BloomFilter<T> create(Funnel<? super T> funnel, long expectedInsertions, double fpp) {
      return create(funnel, expectedInsertions, fpp, BloomFilterStrategies.MURMUR128_MITZ_64);
   }

   @VisibleForTesting
   static <T> BloomFilter<T> create(Funnel<? super T> funnel, long expectedInsertions, double fpp, BloomFilter.Strategy strategy) {
      Preconditions.checkNotNull(funnel);
      Preconditions.checkArgument(expectedInsertions >= 0L, "Expected insertions (%s) must be >= 0", expectedInsertions);
      Preconditions.checkArgument(fpp > 0.0, "False positive probability (%s) must be > 0.0", fpp);
      Preconditions.checkArgument(fpp < 1.0, "False positive probability (%s) must be < 1.0", fpp);
      Preconditions.checkNotNull(strategy);
      if (expectedInsertions == 0L) {
         expectedInsertions = 1L;
      }

      long numBits = optimalNumOfBits(expectedInsertions, fpp);
      int numHashFunctions = optimalNumOfHashFunctions(fpp);

      try {
         return new BloomFilter<>(new BloomFilterStrategies.LockFreeBitArray(numBits), numHashFunctions, funnel, strategy);
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("Could not create BloomFilter of " + numBits + " bits", e);
      }
   }

   public static <T> BloomFilter<T> create(Funnel<? super T> funnel, int expectedInsertions) {
      return create(funnel, (long)expectedInsertions);
   }

   public static <T> BloomFilter<T> create(Funnel<? super T> funnel, long expectedInsertions) {
      return create(funnel, expectedInsertions, 0.03);
   }

   @VisibleForTesting
   static int optimalNumOfHashFunctions(double p) {
      return Math.max(1, (int)Math.round(-Math.log(p) / LOG_TWO));
   }

   @VisibleForTesting
   static long optimalNumOfBits(long n, double p) {
      if (p == 0.0) {
         p = Double.MIN_VALUE;
      }

      return (long)(-n * Math.log(p) / SQUARED_LOG_TWO);
   }

   private Object writeReplace() {
      return new BloomFilter.SerialForm<>(this);
   }

   private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("Use SerializedForm");
   }

   public void writeTo(OutputStream out) throws IOException {
      DataOutputStream dout = new DataOutputStream(out);
      dout.writeByte(SignedBytes.checkedCast(this.strategy.ordinal()));
      dout.writeByte(UnsignedBytes.checkedCast(this.numHashFunctions));
      dout.writeInt(this.bits.data.length());

      for (int i = 0; i < this.bits.data.length(); i++) {
         dout.writeLong(this.bits.data.get(i));
      }
   }

   public static <T> BloomFilter<T> readFrom(InputStream in, Funnel<? super T> funnel) throws IOException {
      Preconditions.checkNotNull(in, "InputStream");
      Preconditions.checkNotNull(funnel, "Funnel");
      int strategyOrdinal = -1;
      int numHashFunctions = -1;
      int dataLength = -1;

      try {
         DataInputStream din = new DataInputStream(in);
         strategyOrdinal = din.readByte();
         numHashFunctions = UnsignedBytes.toInt(din.readByte());
         dataLength = din.readInt();
         BloomFilter.Strategy strategy = BloomFilterStrategies.values()[strategyOrdinal];
         BloomFilterStrategies.LockFreeBitArray dataArray = new BloomFilterStrategies.LockFreeBitArray(LongMath.checkedMultiply(dataLength, 64L));

         for (int i = 0; i < dataLength; i++) {
            dataArray.putData(i, din.readLong());
         }

         return new BloomFilter<>(dataArray, numHashFunctions, funnel, strategy);
      } catch (IOException e) {
         throw e;
      } catch (Exception e) {
         String message = "Unable to deserialize BloomFilter from InputStream. strategyOrdinal: "
            + strategyOrdinal
            + " numHashFunctions: "
            + numHashFunctions
            + " dataLength: "
            + dataLength;
         throw new IOException(message, e);
      }
   }

   private static class SerialForm<T> implements Serializable {
      final long[] data;
      final int numHashFunctions;
      final Funnel<? super T> funnel;
      final BloomFilter.Strategy strategy;
      private static final long serialVersionUID = 1L;

      SerialForm(BloomFilter<T> bf) {
         this.data = BloomFilterStrategies.LockFreeBitArray.toPlainArray(bf.bits.data);
         this.numHashFunctions = bf.numHashFunctions;
         this.funnel = bf.funnel;
         this.strategy = bf.strategy;
      }

      Object readResolve() {
         return new BloomFilter(new BloomFilterStrategies.LockFreeBitArray(this.data), this.numHashFunctions, this.funnel, this.strategy);
      }
   }

   interface Strategy extends Serializable {
      <T> boolean put(@ParametricNullness T object, Funnel<? super T> funnel, int numHashFunctions, BloomFilterStrategies.LockFreeBitArray bits);

      <T> boolean mightContain(@ParametricNullness T object, Funnel<? super T> funnel, int numHashFunctions, BloomFilterStrategies.LockFreeBitArray bits);

      int ordinal();
   }
}
