package com.dfsek.terra.lib.google.common.primitives;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
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
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class Booleans {
   private Booleans() {
   }

   public static Comparator<Boolean> trueFirst() {
      return Booleans.BooleanComparator.TRUE_FIRST;
   }

   public static Comparator<Boolean> falseFirst() {
      return Booleans.BooleanComparator.FALSE_FIRST;
   }

   public static int hashCode(boolean value) {
      return value ? 1231 : 1237;
   }

   @InlineMe(replacement = "Boolean.compare(a, b)")
   public static int compare(boolean a, boolean b) {
      return Boolean.compare(a, b);
   }

   public static boolean contains(boolean[] array, boolean target) {
      for (boolean value : array) {
         if (value == target) {
            return true;
         }
      }

      return false;
   }

   public static int indexOf(boolean[] array, boolean target) {
      return indexOf(array, target, 0, array.length);
   }

   private static int indexOf(boolean[] array, boolean target, int start, int end) {
      for (int i = start; i < end; i++) {
         if (array[i] == target) {
            return i;
         }
      }

      return -1;
   }

   public static int indexOf(boolean[] array, boolean[] target) {
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

   public static int lastIndexOf(boolean[] array, boolean target) {
      return lastIndexOf(array, target, 0, array.length);
   }

   private static int lastIndexOf(boolean[] array, boolean target, int start, int end) {
      for (int i = end - 1; i >= start; i--) {
         if (array[i] == target) {
            return i;
         }
      }

      return -1;
   }

   public static boolean[] concat(boolean[]... arrays) {
      long length = 0L;

      for (boolean[] array : arrays) {
         length += array.length;
      }

      boolean[] result = new boolean[checkNoOverflow(length)];
      int pos = 0;

      for (boolean[] array : arrays) {
         System.arraycopy(array, 0, result, pos, array.length);
         pos += array.length;
      }

      return result;
   }

   private static int checkNoOverflow(long result) {
      Preconditions.checkArgument(result == (int)result, "the total number of elements (%s) in the arrays must fit in an int", result);
      return (int)result;
   }

   public static boolean[] ensureCapacity(boolean[] array, int minLength, int padding) {
      Preconditions.checkArgument(minLength >= 0, "Invalid minLength: %s", minLength);
      Preconditions.checkArgument(padding >= 0, "Invalid padding: %s", padding);
      return array.length < minLength ? Arrays.copyOf(array, minLength + padding) : array;
   }

   public static String join(String separator, boolean... array) {
      Preconditions.checkNotNull(separator);
      if (array.length == 0) {
         return "";
      }

      StringBuilder builder = new StringBuilder(array.length * 7);
      builder.append(array[0]);

      for (int i = 1; i < array.length; i++) {
         builder.append(separator).append(array[i]);
      }

      return builder.toString();
   }

   public static Comparator<boolean[]> lexicographicalComparator() {
      return Booleans.LexicographicalComparator.INSTANCE;
   }

   public static boolean[] toArray(Collection<Boolean> collection) {
      if (collection instanceof Booleans.BooleanArrayAsList) {
         return ((Booleans.BooleanArrayAsList)collection).toBooleanArray();
      }

      Object[] boxedArray = collection.toArray();
      int len = boxedArray.length;
      boolean[] array = new boolean[len];

      for (int i = 0; i < len; i++) {
         array[i] = (Boolean)Preconditions.checkNotNull(boxedArray[i]);
      }

      return array;
   }

   public static List<Boolean> asList(boolean... backingArray) {
      return backingArray.length == 0 ? Collections.emptyList() : new Booleans.BooleanArrayAsList(backingArray);
   }

   public static int countTrue(boolean... values) {
      int count = 0;

      for (boolean value : values) {
         if (value) {
            count++;
         }
      }

      return count;
   }

   public static void reverse(boolean[] array) {
      Preconditions.checkNotNull(array);
      reverse(array, 0, array.length);
   }

   public static void reverse(boolean[] array, int fromIndex, int toIndex) {
      Preconditions.checkNotNull(array);
      Preconditions.checkPositionIndexes(fromIndex, toIndex, array.length);
      int i = fromIndex;

      for (int j = toIndex - 1; i < j; j--) {
         boolean tmp = array[i];
         array[i] = array[j];
         array[j] = tmp;
         i++;
      }
   }

   public static void rotate(boolean[] array, int distance) {
      rotate(array, distance, 0, array.length);
   }

   public static void rotate(boolean[] array, int distance, int fromIndex, int toIndex) {
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

   @GwtCompatible
   private static class BooleanArrayAsList extends AbstractList<Boolean> implements RandomAccess, Serializable {
      final boolean[] array;
      final int start;
      final int end;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      BooleanArrayAsList(boolean[] array) {
         this(array, 0, array.length);
      }

      BooleanArrayAsList(boolean[] array, int start, int end) {
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

      public Boolean get(int index) {
         Preconditions.checkElementIndex(index, this.size());
         return this.array[this.start + index];
      }

      @Override
      public boolean contains(@Nullable Object target) {
         return target instanceof Boolean && Booleans.indexOf(this.array, (Boolean)target, this.start, this.end) != -1;
      }

      @Override
      public int indexOf(@Nullable Object target) {
         if (target instanceof Boolean) {
            int i = Booleans.indexOf(this.array, (Boolean)target, this.start, this.end);
            if (i >= 0) {
               return i - this.start;
            }
         }

         return -1;
      }

      @Override
      public int lastIndexOf(@Nullable Object target) {
         if (target instanceof Boolean) {
            int i = Booleans.lastIndexOf(this.array, (Boolean)target, this.start, this.end);
            if (i >= 0) {
               return i - this.start;
            }
         }

         return -1;
      }

      public Boolean set(int index, Boolean element) {
         Preconditions.checkElementIndex(index, this.size());
         boolean oldValue = this.array[this.start + index];
         this.array[this.start + index] = Preconditions.checkNotNull(element);
         return oldValue;
      }

      @Override
      public List<Boolean> subList(int fromIndex, int toIndex) {
         int size = this.size();
         Preconditions.checkPositionIndexes(fromIndex, toIndex, size);
         return fromIndex == toIndex ? Collections.emptyList() : new Booleans.BooleanArrayAsList(this.array, this.start + fromIndex, this.start + toIndex);
      }

      @Override
      public boolean equals(@Nullable Object object) {
         if (object == this) {
            return true;
         }

         if (object instanceof Booleans.BooleanArrayAsList) {
            Booleans.BooleanArrayAsList that = (Booleans.BooleanArrayAsList)object;
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
            result = 31 * result + Booleans.hashCode(this.array[i]);
         }

         return result;
      }

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder(this.size() * 7);
         builder.append(this.array[this.start] ? "[true" : "[false");

         for (int i = this.start + 1; i < this.end; i++) {
            builder.append(this.array[i] ? ", true" : ", false");
         }

         return builder.append(']').toString();
      }

      boolean[] toBooleanArray() {
         return Arrays.copyOfRange(this.array, this.start, this.end);
      }
   }

   private enum BooleanComparator implements Comparator<Boolean> {
      TRUE_FIRST(1, "Booleans.trueFirst()"),
      FALSE_FIRST(-1, "Booleans.falseFirst()");

      private final int trueValue;
      private final String toString;

      BooleanComparator(int trueValue, String toString) {
         this.trueValue = trueValue;
         this.toString = toString;
      }

      public int compare(Boolean a, Boolean b) {
         int aVal = a ? this.trueValue : 0;
         int bVal = b ? this.trueValue : 0;
         return bVal - aVal;
      }

      @Override
      public String toString() {
         return this.toString;
      }
   }

   private enum LexicographicalComparator implements Comparator<boolean[]> {
      INSTANCE;

      public int compare(boolean[] left, boolean[] right) {
         int minLength = Math.min(left.length, right.length);

         for (int i = 0; i < minLength; i++) {
            int result = Boolean.compare(left[i], right[i]);
            if (result != 0) {
               return result;
            }
         }

         return left.length - right.length;
      }

      @Override
      public String toString() {
         return "Booleans.lexicographicalComparator()";
      }
   }
}
