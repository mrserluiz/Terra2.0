package com.dfsek.terra.lib.google.common.primitives;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import sun.misc.Unsafe;

@J2ktIncompatible
@GwtIncompatible
public final class UnsignedBytes {
   public static final byte MAX_POWER_OF_TWO = -128;
   public static final byte MAX_VALUE = -1;
   private static final int UNSIGNED_MASK = 255;

   private UnsignedBytes() {
   }

   public static int toInt(byte value) {
      return value & 0xFF;
   }

   @CanIgnoreReturnValue
   public static byte checkedCast(long value) {
      Preconditions.checkArgument(value >> 8 == 0L, "out of range: %s", value);
      return (byte)value;
   }

   public static byte saturatedCast(long value) {
      if (value > toInt((byte)-1)) {
         return -1;
      } else {
         return value < 0L ? 0 : (byte)value;
      }
   }

   public static int compare(byte a, byte b) {
      return toInt(a) - toInt(b);
   }

   public static byte min(byte... array) {
      Preconditions.checkArgument(array.length > 0);
      int min = toInt(array[0]);

      for (int i = 1; i < array.length; i++) {
         int next = toInt(array[i]);
         if (next < min) {
            min = next;
         }
      }

      return (byte)min;
   }

   public static byte max(byte... array) {
      Preconditions.checkArgument(array.length > 0);
      int max = toInt(array[0]);

      for (int i = 1; i < array.length; i++) {
         int next = toInt(array[i]);
         if (next > max) {
            max = next;
         }
      }

      return (byte)max;
   }

   public static String toString(byte x) {
      return toString(x, 10);
   }

   public static String toString(byte x, int radix) {
      Preconditions.checkArgument(radix >= 2 && radix <= 36, "radix (%s) must be between Character.MIN_RADIX and Character.MAX_RADIX", radix);
      return Integer.toString(toInt(x), radix);
   }

   @CanIgnoreReturnValue
   public static byte parseUnsignedByte(String string) {
      return parseUnsignedByte(string, 10);
   }

   @CanIgnoreReturnValue
   public static byte parseUnsignedByte(String string, int radix) {
      int parse = Integer.parseInt(Preconditions.checkNotNull(string), radix);
      if (parse >> 8 == 0) {
         return (byte)parse;
      } else {
         throw new NumberFormatException("out of range: " + parse);
      }
   }

   public static String join(String separator, byte... array) {
      Preconditions.checkNotNull(separator);
      if (array.length == 0) {
         return "";
      }

      StringBuilder builder = new StringBuilder(array.length * (3 + separator.length()));
      builder.append(toInt(array[0]));

      for (int i = 1; i < array.length; i++) {
         builder.append(separator).append(toString(array[i]));
      }

      return builder.toString();
   }

   public static Comparator<byte[]> lexicographicalComparator() {
      return UnsignedBytes.LexicographicalComparatorHolder.BEST_COMPARATOR;
   }

   @VisibleForTesting
   static Comparator<byte[]> lexicographicalComparatorJavaImpl() {
      return UnsignedBytes.LexicographicalComparatorHolder.PureJavaComparator.INSTANCE;
   }

   private static byte flip(byte b) {
      return (byte)(b ^ 128);
   }

   public static void sort(byte[] array) {
      Preconditions.checkNotNull(array);
      sort(array, 0, array.length);
   }

   public static void sort(byte[] array, int fromIndex, int toIndex) {
      Preconditions.checkNotNull(array);
      Preconditions.checkPositionIndexes(fromIndex, toIndex, array.length);

      for (int i = fromIndex; i < toIndex; i++) {
         array[i] = flip(array[i]);
      }

      Arrays.sort(array, fromIndex, toIndex);

      for (int i = fromIndex; i < toIndex; i++) {
         array[i] = flip(array[i]);
      }
   }

   public static void sortDescending(byte[] array) {
      Preconditions.checkNotNull(array);
      sortDescending(array, 0, array.length);
   }

   public static void sortDescending(byte[] array, int fromIndex, int toIndex) {
      Preconditions.checkNotNull(array);
      Preconditions.checkPositionIndexes(fromIndex, toIndex, array.length);

      for (int i = fromIndex; i < toIndex; i++) {
         array[i] = (byte)(array[i] ^ 127);
      }

      Arrays.sort(array, fromIndex, toIndex);

      for (int i = fromIndex; i < toIndex; i++) {
         array[i] = (byte)(array[i] ^ 127);
      }
   }

   enum ArraysCompareUnsignedComparator implements Comparator<byte[]> {
      INSTANCE;

      @IgnoreJRERequirement
      public int compare(byte[] left, byte[] right) {
         return Arrays.compareUnsigned(left, right);
      }
   }

   private enum ArraysCompareUnsignedComparatorMaker {
      INSTANCE {
         @IgnoreJRERequirement
         @Override
         @Nullable Comparator<byte[]> tryMakeArraysCompareUnsignedComparator() {
            try {
               Arrays.class.getMethod("compareUnsigned", byte[].class, byte[].class);
            } catch (NoSuchMethodException beforeJava9) {
               return null;
            }

            return UnsignedBytes.ArraysCompareUnsignedComparator.INSTANCE;
         }
      };

      ArraysCompareUnsignedComparatorMaker() {
      }

      @Nullable Comparator<byte[]> tryMakeArraysCompareUnsignedComparator() {
         return null;
      }
   }

   @VisibleForTesting
   static class LexicographicalComparatorHolder {
      static final String UNSAFE_COMPARATOR_NAME = UnsignedBytes.LexicographicalComparatorHolder.class.getName() + "$UnsafeComparator";
      static final Comparator<byte[]> BEST_COMPARATOR = getBestComparator();

      static Comparator<byte[]> getBestComparator() {
         Comparator<byte[]> arraysCompareUnsignedComparator = UnsignedBytes.ArraysCompareUnsignedComparatorMaker.INSTANCE
            .tryMakeArraysCompareUnsignedComparator();
         if (arraysCompareUnsignedComparator != null) {
            return arraysCompareUnsignedComparator;
         }

         try {
            Class<?> theClass = Class.forName(UNSAFE_COMPARATOR_NAME);
            Object[] constants = Objects.requireNonNull(theClass.getEnumConstants());
            return (Comparator<byte[]>)constants[0];
         } catch (Throwable t) {
            return UnsignedBytes.lexicographicalComparatorJavaImpl();
         }
      }

      enum PureJavaComparator implements Comparator<byte[]> {
         INSTANCE;

         public int compare(byte[] left, byte[] right) {
            int minLength = Math.min(left.length, right.length);

            for (int i = 0; i < minLength; i++) {
               int result = UnsignedBytes.compare(left[i], right[i]);
               if (result != 0) {
                  return result;
               }
            }

            return left.length - right.length;
         }

         @Override
         public String toString() {
            return "UnsignedBytes.lexicographicalComparator() (pure Java version)";
         }
      }

      @VisibleForTesting
      enum UnsafeComparator implements Comparator<byte[]> {
         INSTANCE;

         static final boolean BIG_ENDIAN = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);
         static final Unsafe theUnsafe = getUnsafe();
         static final int BYTE_ARRAY_BASE_OFFSET = theUnsafe.arrayBaseOffset(byte[].class);

         private static Unsafe getUnsafe() {
            try {
               return Unsafe.getUnsafe();
            } catch (SecurityException var2) {
               try {
                  return AccessController.doPrivileged(() -> {
                     Class<Unsafe> k = Unsafe.class;

                     for (Field f : k.getDeclaredFields()) {
                        f.setAccessible(true);
                        Object x = f.get(null);
                        if (k.isInstance(x)) {
                           return k.cast(x);
                        }
                     }

                     throw new NoSuchFieldError("the Unsafe");
                  });
               } catch (PrivilegedActionException e) {
                  throw new RuntimeException("Could not initialize intrinsics", e.getCause());
               }
            }
         }

         public int compare(byte[] left, byte[] right) {
            int stride = 8;
            int minLength = Math.min(left.length, right.length);
            int strideLimit = minLength & ~(stride - 1);

            int i;
            for (i = 0; i < strideLimit; i += stride) {
               long lw = theUnsafe.getLong(left, (long)BYTE_ARRAY_BASE_OFFSET + i);
               long rw = theUnsafe.getLong(right, (long)BYTE_ARRAY_BASE_OFFSET + i);
               if (lw != rw) {
                  if (BIG_ENDIAN) {
                     return Long.compareUnsigned(lw, rw);
                  }

                  int n = Long.numberOfTrailingZeros(lw ^ rw) & -8;
                  return (int)(lw >>> n & 255L) - (int)(rw >>> n & 255L);
               }
            }

            while (i < minLength) {
               int result = UnsignedBytes.compare(left[i], right[i]);
               if (result != 0) {
                  return result;
               }

               i++;
            }

            return left.length - right.length;
         }

         @Override
         public String toString() {
            return "UnsignedBytes.lexicographicalComparator() (sun.misc.Unsafe version)";
         }

         static {
            if (!Objects.equals(System.getProperty("sun.arch.data.model"), "64")
               || BYTE_ARRAY_BASE_OFFSET % 8 != 0
               || theUnsafe.arrayIndexScale(byte[].class) != 1) {
               throw new Error();
            }
         }
      }
   }
}
