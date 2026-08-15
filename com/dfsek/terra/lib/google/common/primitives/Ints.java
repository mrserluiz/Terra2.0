package com.dfsek.terra.lib.google.common.primitives;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Converter;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.InlineMe;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.RandomAccess;
import java.util.Spliterators;
import java.util.Spliterator.OfInt;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
public final class Ints extends IntsMethodsForWeb {
   public static final int BYTES = 4;
   public static final int MAX_POWER_OF_TWO = 1073741824;

   private Ints() {
   }

   public static int hashCode(int value) {
      return value;
   }

   public static int checkedCast(long value) {
      int result = (int)value;
      Preconditions.checkArgument(result == value, "Out of range: %s", value);
      return result;
   }

   public static int saturatedCast(long value) {
      if (value > 2147483647L) {
         return Integer.MAX_VALUE;
      } else {
         return value < -2147483648L ? Integer.MIN_VALUE : (int)value;
      }
   }

   @InlineMe(replacement = "Integer.compare(a, b)")
   public static int compare(int a, int b) {
      return Integer.compare(a, b);
   }

   public static boolean contains(int[] array, int target) {
      for (int value : array) {
         if (value == target) {
            return true;
         }
      }

      return false;
   }

   public static int indexOf(int[] array, int target) {
      return indexOf(array, target, 0, array.length);
   }

   private static int indexOf(int[] array, int target, int start, int end) {
      for (int i = start; i < end; i++) {
         if (array[i] == target) {
            return i;
         }
      }

      return -1;
   }

   public static int indexOf(int[] array, int[] target) {
      Preconditions.checkNotNull(array, "array");
      Preconditions.checkNotNull(target, "target");
      if (target.length == 0) {
         return 0;
      }

      label28:
      for (int i = 0; i < array.length - target.length + 1; i++) {
         for (int j = 0; j < target.length; j++) {
            if (array[i + j] != target[j]) {
               continue label28;
            }
         }

         return i;
      }

      return -1;
   }

   public static int lastIndexOf(int[] array, int target) {
      return lastIndexOf(array, target, 0, array.length);
   }

   private static int lastIndexOf(int[] array, int target, int start, int end) {
      for (int i = end - 1; i >= start; i--) {
         if (array[i] == target) {
            return i;
         }
      }

      return -1;
   }

   @GwtIncompatible("Available in GWT! Annotation is to avoid conflict with GWT specialization of base class.")
   public static int min(int... array) {
      Preconditions.checkArgument(array.length > 0);
      int min = array[0];

      for (int i = 1; i < array.length; i++) {
         if (array[i] < min) {
            min = array[i];
         }
      }

      return min;
   }

   @GwtIncompatible("Available in GWT! Annotation is to avoid conflict with GWT specialization of base class.")
   public static int max(int... array) {
      Preconditions.checkArgument(array.length > 0);
      int max = array[0];

      for (int i = 1; i < array.length; i++) {
         if (array[i] > max) {
            max = array[i];
         }
      }

      return max;
   }

   public static int constrainToRange(int value, int min, int max) {
      Preconditions.checkArgument(min <= max, "min (%s) must be less than or equal to max (%s)", min, max);
      return Math.min(Math.max(value, min), max);
   }

   public static int[] concat(int[]... arrays) {
      long length = 0L;

      for (int[] array : arrays) {
         length += array.length;
      }

      int[] result = new int[checkNoOverflow(length)];
      int pos = 0;

      for (int[] array : arrays) {
         System.arraycopy(array, 0, result, pos, array.length);
         pos += array.length;
      }

      return result;
   }

   private static int checkNoOverflow(long result) {
      Preconditions.checkArgument(result == (int)result, "the total number of elements (%s) in the arrays must fit in an int", result);
      return (int)result;
   }

   public static byte[] toByteArray(int value) {
      return new byte[]{(byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value};
   }

   public static int fromByteArray(byte[] bytes) {
      Preconditions.checkArgument(bytes.length >= 4, "array too small: %s < %s", bytes.length, 4);
      return fromBytes(bytes[0], bytes[1], bytes[2], bytes[3]);
   }

   public static int fromBytes(byte b1, byte b2, byte b3, byte b4) {
      return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | b4 & 0xFF;
   }

   public static Converter<String, Integer> stringConverter() {
      return Ints.IntConverter.INSTANCE;
   }

   public static int[] ensureCapacity(int[] array, int minLength, int padding) {
      Preconditions.checkArgument(minLength >= 0, "Invalid minLength: %s", minLength);
      Preconditions.checkArgument(padding >= 0, "Invalid padding: %s", padding);
      return array.length < minLength ? Arrays.copyOf(array, minLength + padding) : array;
   }

   public static String join(String separator, int... array) {
      Preconditions.checkNotNull(separator);
      if (array.length == 0) {
         return "";
      }

      StringBuilder builder = new StringBuilder(array.length * 5);
      builder.append(array[0]);

      for (int i = 1; i < array.length; i++) {
         builder.append(separator).append(array[i]);
      }

      return builder.toString();
   }

   public static Comparator<int[]> lexicographicalComparator() {
      return Ints.LexicographicalComparator.INSTANCE;
   }

   public static void sortDescending(int[] array) {
      Preconditions.checkNotNull(array);
      sortDescending(array, 0, array.length);
   }

   public static void sortDescending(int[] array, int fromIndex, int toIndex) {
      Preconditions.checkNotNull(array);
      Preconditions.checkPositionIndexes(fromIndex, toIndex, array.length);
      Arrays.sort(array, fromIndex, toIndex);
      reverse(array, fromIndex, toIndex);
   }

   public static void reverse(int[] array) {
      Preconditions.checkNotNull(array);
      reverse(array, 0, array.length);
   }

   public static void reverse(int[] array, int fromIndex, int toIndex) {
      Preconditions.checkNotNull(array);
      Preconditions.checkPositionIndexes(fromIndex, toIndex, array.length);
      int i = fromIndex;

      for (int j = toIndex - 1; i < j; j--) {
         int tmp = array[i];
         array[i] = array[j];
         array[j] = tmp;
         i++;
      }
   }

   public static void rotate(int[] array, int distance) {
      rotate(array, distance, 0, array.length);
   }

   public static void rotate(int[] array, int distance, int fromIndex, int toIndex) {
      Preconditions.checkNotNull(array);
      Preconditions.checkPositionIndexes(fromIndex, toIndex, array.length);
      if (array.length > 1) {
         int length = toIndex - fromIndex;
         int m = -distance % length;
         m = m < 0 ? m + length : m;
         int newFirstIndex = m + fromIndex;
         if (newFirstIndex != fromIndex) {
            reverse(array, fromIndex, newFirstIndex);
            reverse(array, newFirstIndex, toIndex);
            reverse(array, fromIndex, toIndex);
         }
      }
   }

   public static int[] toArray(Collection<? extends Number> collection) {
      if (collection instanceof Ints.IntArrayAsList) {
         return ((Ints.IntArrayAsList)collection).toIntArray();
      }

      Object[] boxedArray = collection.toArray();
      int len = boxedArray.length;
      int[] array = new int[len];

      for (int i = 0; i < len; i++) {
         array[i] = ((Number)Preconditions.checkNotNull(boxedArray[i])).intValue();
      }

      return array;
   }

   public static List<Integer> asList(int... backingArray) {
      return backingArray.length == 0 ? Collections.emptyList() : new Ints.IntArrayAsList(backingArray);
   }

   public static @Nullable Integer tryParse(String string) {
      return tryParse(string, 10);
   }

   public static @Nullable Integer tryParse(String string, int radix) {
      Long result = Longs.tryParse(string, radix);
      return result != null && result == result.intValue() ? result.intValue() : null;
   }

   @GwtCompatible
   private static class IntArrayAsList extends AbstractList<Integer> implements RandomAccess, Serializable {
      final int[] array;
      final int start;
      final int end;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      IntArrayAsList(int[] array) {
         this(array, 0, array.length);
      }

      IntArrayAsList(int[] array, int start, int end) {
         this.array = array;
         this.start = start;
         this.end = end;
      }

      @Override
      public int size() {
         return this.end - this.start;
      }

      @Override
      public boolean isEmpty() {
         return false;
      }

      public Integer get(int index) {
         Preconditions.checkElementIndex(index, this.size());
         return this.array[this.start + index];
      }

      public OfInt spliterator() {
         return Spliterators.spliterator(this.array, this.start, this.end, 0);
      }

      @Override
      public boolean contains(@Nullable Object target) {
         return target instanceof Integer && Ints.indexOf(this.array, (Integer)target, this.start, this.end) != -1;
      }

      @Override
      public int indexOf(@Nullable Object target) {
         if (target instanceof Integer) {
            int i = Ints.indexOf(this.array, (Integer)target, this.start, this.end);
            if (i >= 0) {
               return i - this.start;
            }
         }

         return -1;
      }

      @Override
      public int lastIndexOf(@Nullable Object target) {
         if (target instanceof Integer) {
            int i = Ints.lastIndexOf(this.array, (Integer)target, this.start, this.end);
            if (i >= 0) {
               return i - this.start;
            }
         }

         return -1;
      }

      public Integer set(int index, Integer element) {
         Preconditions.checkElementIndex(index, this.size());
         int oldValue = this.array[this.start + index];
         this.array[this.start + index] = Preconditions.checkNotNull(element);
         return oldValue;
      }

      @Override
      public List<Integer> subList(int fromIndex, int toIndex) {
         int size = this.size();
         Preconditions.checkPositionIndexes(fromIndex, toIndex, size);
         return fromIndex == toIndex ? Collections.emptyList() : new Ints.IntArrayAsList(this.array, this.start + fromIndex, this.start + toIndex);
      }

      @Override
      public boolean equals(@Nullable Object object) {
         if (object == this) {
            return true;
         }

         if (object instanceof Ints.IntArrayAsList) {
            Ints.IntArrayAsList that = (Ints.IntArrayAsList)object;
            int size = this.size();
            if (that.size() != size) {
               return false;
            }

            for (int i = 0; i < size; i++) {
               if (this.array[this.start + i] != that.array[that.start + i]) {
                  return false;
               }
            }

            return true;
         } else {
            return super.equals(object);
         }
      }

      @Override
      public int hashCode() {
         int result = 1;

         for (int i = this.start; i < this.end; i++) {
            result = 31 * result + Ints.hashCode(this.array[i]);
         }

         return result;
      }

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder(this.size() * 5);
         builder.append('[').append(this.array[this.start]);

         for (int i = this.start + 1; i < this.end; i++) {
            builder.append(", ").append(this.array[i]);
         }

         return builder.append(']').toString();
      }

      int[] toIntArray() {
         return Arrays.copyOfRange(this.array, this.start, this.end);
      }
   }

   private static final class IntConverter extends Converter<String, Integer> implements Serializable {
      static final Converter<String, Integer> INSTANCE = new Ints.IntConverter();
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 1L;

      protected Integer doForward(String value) {
         return Integer.decode(value);
      }

      protected String doBackward(Integer value) {
         return value.toString();
      }

      @Override
      public String toString() {
         return "Ints.stringConverter()";
      }

      private Object readResolve() {
         return INSTANCE;
      }
   }

   private enum LexicographicalComparator implements Comparator<int[]> {
      INSTANCE;

      public int compare(int[] left, int[] right) {
         int minLength = Math.min(left.length, right.length);

         for (int i = 0; i < minLength; i++) {
            int result = Integer.compare(left[i], right[i]);
            if (result != 0) {
               return result;
            }
         }

         return left.length - right.length;
      }

      @Override
      public String toString() {
         return "Ints.lexicographicalComparator()";
      }
   }
}
