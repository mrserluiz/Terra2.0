package com.dfsek.terra.lib.google.common.primitives;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Converter;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Strings;
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
import java.util.Spliterator.OfDouble;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
public final class Doubles extends DoublesMethodsForWeb {
   public static final int BYTES = 8;
   @GwtIncompatible
   static final Pattern FLOATING_POINT_PATTERN = fpPattern();

   private Doubles() {
   }

   public static int hashCode(double value) {
      return Double.valueOf(value).hashCode();
   }

   @InlineMe(replacement = "Double.compare(a, b)")
   public static int compare(double a, double b) {
      return Double.compare(a, b);
   }

   public static boolean isFinite(double value) {
      return Double.NEGATIVE_INFINITY < value && value < Double.POSITIVE_INFINITY;
   }

   public static boolean contains(double[] array, double target) {
      for (double value : array) {
         if (value == target) {
            return true;
         }
      }

      return false;
   }

   public static int indexOf(double[] array, double target) {
      return indexOf(array, target, 0, array.length);
   }

   private static int indexOf(double[] array, double target, int start, int end) {
      for (int i = start; i < end; i++) {
         if (array[i] == target) {
            return i;
         }
      }

      return -1;
   }

   public static int indexOf(double[] array, double[] target) {
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

   public static int lastIndexOf(double[] array, double target) {
      return lastIndexOf(array, target, 0, array.length);
   }

   private static int lastIndexOf(double[] array, double target, int start, int end) {
      for (int i = end - 1; i >= start; i--) {
         if (array[i] == target) {
            return i;
         }
      }

      return -1;
   }

   @GwtIncompatible("Available in GWT! Annotation is to avoid conflict with GWT specialization of base class.")
   public static double min(double... array) {
      Preconditions.checkArgument(array.length > 0);
      double min = array[0];

      for (int i = 1; i < array.length; i++) {
         min = Math.min(min, array[i]);
      }

      return min;
   }

   @GwtIncompatible("Available in GWT! Annotation is to avoid conflict with GWT specialization of base class.")
   public static double max(double... array) {
      Preconditions.checkArgument(array.length > 0);
      double max = array[0];

      for (int i = 1; i < array.length; i++) {
         max = Math.max(max, array[i]);
      }

      return max;
   }

   public static double constrainToRange(double value, double min, double max) {
      if (min <= max) {
         return Math.min(Math.max(value, min), max);
      } else {
         throw new IllegalArgumentException(Strings.lenientFormat("min (%s) must be less than or equal to max (%s)", min, max));
      }
   }

   public static double[] concat(double[]... arrays) {
      long length = 0L;

      for (double[] array : arrays) {
         length += array.length;
      }

      double[] result = new double[checkNoOverflow(length)];
      int pos = 0;

      for (double[] array : arrays) {
         System.arraycopy(array, 0, result, pos, array.length);
         pos += array.length;
      }

      return result;
   }

   private static int checkNoOverflow(long result) {
      Preconditions.checkArgument(result == (int)result, "the total number of elements (%s) in the arrays must fit in an int", result);
      return (int)result;
   }

   public static Converter<String, Double> stringConverter() {
      return Doubles.DoubleConverter.INSTANCE;
   }

   public static double[] ensureCapacity(double[] array, int minLength, int padding) {
      Preconditions.checkArgument(minLength >= 0, "Invalid minLength: %s", minLength);
      Preconditions.checkArgument(padding >= 0, "Invalid padding: %s", padding);
      return array.length < minLength ? Arrays.copyOf(array, minLength + padding) : array;
   }

   public static String join(String separator, double... array) {
      Preconditions.checkNotNull(separator);
      if (array.length == 0) {
         return "";
      }

      StringBuilder builder = new StringBuilder(array.length * 12);
      builder.append(array[0]);

      for (int i = 1; i < array.length; i++) {
         builder.append(separator).append(array[i]);
      }

      return builder.toString();
   }

   public static Comparator<double[]> lexicographicalComparator() {
      return Doubles.LexicographicalComparator.INSTANCE;
   }

   public static void sortDescending(double[] array) {
      Preconditions.checkNotNull(array);
      sortDescending(array, 0, array.length);
   }

   public static void sortDescending(double[] array, int fromIndex, int toIndex) {
      Preconditions.checkNotNull(array);
      Preconditions.checkPositionIndexes(fromIndex, toIndex, array.length);
      Arrays.sort(array, fromIndex, toIndex);
      reverse(array, fromIndex, toIndex);
   }

   public static void reverse(double[] array) {
      Preconditions.checkNotNull(array);
      reverse(array, 0, array.length);
   }

   public static void reverse(double[] array, int fromIndex, int toIndex) {
      Preconditions.checkNotNull(array);
      Preconditions.checkPositionIndexes(fromIndex, toIndex, array.length);
      int i = fromIndex;

      for (int j = toIndex - 1; i < j; j--) {
         double tmp = array[i];
         array[i] = array[j];
         array[j] = tmp;
         i++;
      }
   }

   public static void rotate(double[] array, int distance) {
      rotate(array, distance, 0, array.length);
   }

   public static void rotate(double[] array, int distance, int fromIndex, int toIndex) {
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

   public static double[] toArray(Collection<? extends Number> collection) {
      if (collection instanceof Doubles.DoubleArrayAsList) {
         return ((Doubles.DoubleArrayAsList)collection).toDoubleArray();
      }

      Object[] boxedArray = collection.toArray();
      int len = boxedArray.length;
      double[] array = new double[len];

      for (int i = 0; i < len; i++) {
         array[i] = ((Number)Preconditions.checkNotNull(boxedArray[i])).doubleValue();
      }

      return array;
   }

   public static List<Double> asList(double... backingArray) {
      return backingArray.length == 0 ? Collections.emptyList() : new Doubles.DoubleArrayAsList(backingArray);
   }

   @GwtIncompatible
   private static Pattern fpPattern() {
      String decimal = "(?:\\d+#(?:\\.\\d*#)?|\\.\\d+#)";
      String completeDec = decimal + "(?:[eE][+-]?\\d+#)?[fFdD]?";
      String hex = "(?:[0-9a-fA-F]+#(?:\\.[0-9a-fA-F]*#)?|\\.[0-9a-fA-F]+#)";
      String completeHex = "0[xX]" + hex + "[pP][+-]?\\d+#[fFdD]?";
      String fpPattern = "[+-]?(?:NaN|Infinity|" + completeDec + "|" + completeHex + ")";
      fpPattern = fpPattern.replace("#", "+");
      return Pattern.compile(fpPattern);
   }

   @GwtIncompatible
   public static @Nullable Double tryParse(String string) {
      if (FLOATING_POINT_PATTERN.matcher(string).matches()) {
         try {
            return Double.parseDouble(string);
         } catch (NumberFormatException var2) {
         }
      }

      return null;
   }

   @GwtCompatible
   private static class DoubleArrayAsList extends AbstractList<Double> implements RandomAccess, Serializable {
      final double[] array;
      final int start;
      final int end;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      DoubleArrayAsList(double[] array) {
         this(array, 0, array.length);
      }

      DoubleArrayAsList(double[] array, int start, int end) {
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

      public Double get(int index) {
         Preconditions.checkElementIndex(index, this.size());
         return this.array[this.start + index];
      }

      public OfDouble spliterator() {
         return Spliterators.spliterator(this.array, this.start, this.end, 0);
      }

      @Override
      public boolean contains(@Nullable Object target) {
         return target instanceof Double && Doubles.indexOf(this.array, (Double)target, this.start, this.end) != -1;
      }

      @Override
      public int indexOf(@Nullable Object target) {
         if (target instanceof Double) {
            int i = Doubles.indexOf(this.array, (Double)target, this.start, this.end);
            if (i >= 0) {
               return i - this.start;
            }
         }

         return -1;
      }

      @Override
      public int lastIndexOf(@Nullable Object target) {
         if (target instanceof Double) {
            int i = Doubles.lastIndexOf(this.array, (Double)target, this.start, this.end);
            if (i >= 0) {
               return i - this.start;
            }
         }

         return -1;
      }

      public Double set(int index, Double element) {
         Preconditions.checkElementIndex(index, this.size());
         double oldValue = this.array[this.start + index];
         this.array[this.start + index] = Preconditions.checkNotNull(element);
         return oldValue;
      }

      @Override
      public List<Double> subList(int fromIndex, int toIndex) {
         int size = this.size();
         Preconditions.checkPositionIndexes(fromIndex, toIndex, size);
         return fromIndex == toIndex ? Collections.emptyList() : new Doubles.DoubleArrayAsList(this.array, this.start + fromIndex, this.start + toIndex);
      }

      @Override
      public boolean equals(@Nullable Object object) {
         if (object == this) {
            return true;
         }

         if (object instanceof Doubles.DoubleArrayAsList) {
            Doubles.DoubleArrayAsList that = (Doubles.DoubleArrayAsList)object;
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
            result = 31 * result + Doubles.hashCode(this.array[i]);
         }

         return result;
      }

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder(this.size() * 12);
         builder.append('[').append(this.array[this.start]);

         for (int i = this.start + 1; i < this.end; i++) {
            builder.append(", ").append(this.array[i]);
         }

         return builder.append(']').toString();
      }

      double[] toDoubleArray() {
         return Arrays.copyOfRange(this.array, this.start, this.end);
      }
   }

   private static final class DoubleConverter extends Converter<String, Double> implements Serializable {
      static final Converter<String, Double> INSTANCE = new Doubles.DoubleConverter();
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 1L;

      protected Double doForward(String value) {
         return Double.valueOf(value);
      }

      protected String doBackward(Double value) {
         return value.toString();
      }

      @Override
      public String toString() {
         return "Doubles.stringConverter()";
      }

      private Object readResolve() {
         return INSTANCE;
      }
   }

   private enum LexicographicalComparator implements Comparator<double[]> {
      INSTANCE;

      public int compare(double[] left, double[] right) {
         int minLength = Math.min(left.length, right.length);

         for (int i = 0; i < minLength; i++) {
            int result = Double.compare(left[i], right[i]);
            if (result != 0) {
               return result;
            }
         }

         return left.length - right.length;
      }

      @Override
      public String toString() {
         return "Doubles.lexicographicalComparator()";
      }
   }
}
