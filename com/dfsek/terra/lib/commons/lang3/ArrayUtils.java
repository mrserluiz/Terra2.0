package com.dfsek.terra.lib.commons.lang3;

import com.dfsek.terra.lib.commons.lang3.builder.EqualsBuilder;
import com.dfsek.terra.lib.commons.lang3.builder.HashCodeBuilder;
import com.dfsek.terra.lib.commons.lang3.builder.ToStringBuilder;
import com.dfsek.terra.lib.commons.lang3.builder.ToStringStyle;
import com.dfsek.terra.lib.commons.lang3.math.NumberUtils;
import com.dfsek.terra.lib.commons.lang3.mutable.MutableInt;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class ArrayUtils {
   public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
   public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];
   public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
   public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = new Byte[0];
   public static final char[] EMPTY_CHAR_ARRAY = new char[0];
   public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];
   public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
   public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
   public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];
   public static final Field[] EMPTY_FIELD_ARRAY = new Field[0];
   public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
   public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY = new Float[0];
   public static final int[] EMPTY_INT_ARRAY = new int[0];
   public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];
   public static final long[] EMPTY_LONG_ARRAY = new long[0];
   public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];
   public static final Method[] EMPTY_METHOD_ARRAY = new Method[0];
   public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
   public static final short[] EMPTY_SHORT_ARRAY = new short[0];
   public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = new Short[0];
   public static final String[] EMPTY_STRING_ARRAY = new String[0];
   public static final Throwable[] EMPTY_THROWABLE_ARRAY = new Throwable[0];
   public static final Type[] EMPTY_TYPE_ARRAY = new Type[0];
   public static final int INDEX_NOT_FOUND = -1;

   public static boolean[] add(boolean[] array, boolean element) {
      boolean[] newArray = (boolean[])copyArrayGrow1(array, boolean.class);
      newArray[newArray.length - 1] = element;
      return newArray;
   }

   @Deprecated
   public static boolean[] add(boolean[] array, int index, boolean element) {
      return (boolean[])add(array, index, element, boolean.class);
   }

   public static byte[] add(byte[] array, byte element) {
      byte[] newArray = (byte[])copyArrayGrow1(array, byte.class);
      newArray[newArray.length - 1] = element;
      return newArray;
   }

   @Deprecated
   public static byte[] add(byte[] array, int index, byte element) {
      return (byte[])add(array, index, element, byte.class);
   }

   public static char[] add(char[] array, char element) {
      char[] newArray = (char[])copyArrayGrow1(array, char.class);
      newArray[newArray.length - 1] = element;
      return newArray;
   }

   @Deprecated
   public static char[] add(char[] array, int index, char element) {
      return (char[])add(array, index, element, char.class);
   }

   public static double[] add(double[] array, double element) {
      double[] newArray = (double[])copyArrayGrow1(array, double.class);
      newArray[newArray.length - 1] = element;
      return newArray;
   }

   @Deprecated
   public static double[] add(double[] array, int index, double element) {
      return (double[])add(array, index, element, double.class);
   }

   public static float[] add(float[] array, float element) {
      float[] newArray = (float[])copyArrayGrow1(array, float.class);
      newArray[newArray.length - 1] = element;
      return newArray;
   }

   @Deprecated
   public static float[] add(float[] array, int index, float element) {
      return (float[])add(array, index, element, float.class);
   }

   public static int[] add(int[] array, int element) {
      int[] newArray = (int[])copyArrayGrow1(array, int.class);
      newArray[newArray.length - 1] = element;
      return newArray;
   }

   @Deprecated
   public static int[] add(int[] array, int index, int element) {
      return (int[])add(array, index, element, int.class);
   }

   @Deprecated
   public static long[] add(long[] array, int index, long element) {
      return (long[])add(array, index, element, long.class);
   }

   public static long[] add(long[] array, long element) {
      long[] newArray = (long[])copyArrayGrow1(array, long.class);
      newArray[newArray.length - 1] = element;
      return newArray;
   }

   private static Object add(Object array, int index, Object element, Class<?> clazz) {
      if (array == null) {
         if (index != 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: 0");
         }

         Object joinedArray = Array.newInstance(clazz, 1);
         Array.set(joinedArray, 0, element);
         return joinedArray;
      } else {
         int length = Array.getLength(array);
         if (index <= length && index >= 0) {
            Object result = arraycopy(array, 0, 0, index, () -> Array.newInstance(clazz, length + 1));
            Array.set(result, index, element);
            if (index < length) {
               System.arraycopy(array, index, result, index + 1, length - index);
            }

            return result;
         } else {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
         }
      }
   }

   @Deprecated
   public static short[] add(short[] array, int index, short element) {
      return (short[])add(array, index, element, short.class);
   }

   public static short[] add(short[] array, short element) {
      short[] newArray = (short[])copyArrayGrow1(array, short.class);
      newArray[newArray.length - 1] = element;
      return newArray;
   }

   @Deprecated
   public static <T> T[] add(T[] array, int index, T element) {
      Class<T> clazz;
      if (array != null) {
         clazz = getComponentType(array);
      } else {
         if (element == null) {
            throw new IllegalArgumentException("Array and element cannot both be null");
         }

         clazz = ObjectUtils.getClass(element);
      }

      return (T[])((Object[])add(array, index, element, clazz));
   }

   public static <T> T[] add(T[] array, T element) {
      Class<?> type;
      if (array != null) {
         type = array.getClass().getComponentType();
      } else {
         if (element == null) {
            throw new IllegalArgumentException("Arguments cannot both be null");
         }

         type = element.getClass();
      }

      T[] newArray = (T[])copyArrayGrow1(array, type);
      newArray[newArray.length - 1] = element;
      return newArray;
   }

   public static boolean[] addAll(boolean[] array1, boolean... array2) {
      if (array1 == null) {
         return clone(array2);
      }

      if (array2 == null) {
         return clone(array1);
      }

      boolean[] joinedArray = new boolean[array1.length + array2.length];
      System.arraycopy(array1, 0, joinedArray, 0, array1.length);
      System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
      return joinedArray;
   }

   public static byte[] addAll(byte[] array1, byte... array2) {
      if (array1 == null) {
         return clone(array2);
      }

      if (array2 == null) {
         return clone(array1);
      }

      byte[] joinedArray = new byte[array1.length + array2.length];
      System.arraycopy(array1, 0, joinedArray, 0, array1.length);
      System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
      return joinedArray;
   }

   public static char[] addAll(char[] array1, char... array2) {
      if (array1 == null) {
         return clone(array2);
      }

      if (array2 == null) {
         return clone(array1);
      }

      char[] joinedArray = new char[array1.length + array2.length];
      System.arraycopy(array1, 0, joinedArray, 0, array1.length);
      System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
      return joinedArray;
   }

   public static double[] addAll(double[] array1, double... array2) {
      if (array1 == null) {
         return clone(array2);
      }

      if (array2 == null) {
         return clone(array1);
      }

      double[] joinedArray = new double[array1.length + array2.length];
      System.arraycopy(array1, 0, joinedArray, 0, array1.length);
      System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
      return joinedArray;
   }

   public static float[] addAll(float[] array1, float... array2) {
      if (array1 == null) {
         return clone(array2);
      }

      if (array2 == null) {
         return clone(array1);
      }

      float[] joinedArray = new float[array1.length + array2.length];
      System.arraycopy(array1, 0, joinedArray, 0, array1.length);
      System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
      return joinedArray;
   }

   public static int[] addAll(int[] array1, int... array2) {
      if (array1 == null) {
         return clone(array2);
      }

      if (array2 == null) {
         return clone(array1);
      }

      int[] joinedArray = new int[array1.length + array2.length];
      System.arraycopy(array1, 0, joinedArray, 0, array1.length);
      System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
      return joinedArray;
   }

   public static long[] addAll(long[] array1, long... array2) {
      if (array1 == null) {
         return clone(array2);
      }

      if (array2 == null) {
         return clone(array1);
      }

      long[] joinedArray = new long[array1.length + array2.length];
      System.arraycopy(array1, 0, joinedArray, 0, array1.length);
      System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
      return joinedArray;
   }

   public static short[] addAll(short[] array1, short... array2) {
      if (array1 == null) {
         return clone(array2);
      }

      if (array2 == null) {
         return clone(array1);
      }

      short[] joinedArray = new short[array1.length + array2.length];
      System.arraycopy(array1, 0, joinedArray, 0, array1.length);
      System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
      return joinedArray;
   }

   public static <T> T[] addAll(T[] array1, T... array2) {
      if (array1 == null) {
         return (T[])clone(array2);
      }

      if (array2 == null) {
         return (T[])clone(array1);
      }

      Class<T> type1 = getComponentType(array1);
      T[] joinedArray = (T[])arraycopy(array1, 0, 0, array1.length, () -> newInstance(type1, array1.length + array2.length));

      try {
         System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
         return joinedArray;
      } catch (ArrayStoreException ase) {
         Class<?> type2 = array2.getClass().getComponentType();
         if (!type1.isAssignableFrom(type2)) {
            throw new IllegalArgumentException("Cannot store " + type2.getName() + " in an array of " + type1.getName(), ase);
         } else {
            throw ase;
         }
      }
   }

   public static boolean[] addFirst(boolean[] array, boolean element) {
      return array == null ? add(array, element) : insert(0, array, element);
   }

   public static byte[] addFirst(byte[] array, byte element) {
      return array == null ? add(array, element) : insert(0, array, element);
   }

   public static char[] addFirst(char[] array, char element) {
      return array == null ? add(array, element) : insert(0, array, element);
   }

   public static double[] addFirst(double[] array, double element) {
      return array == null ? add(array, element) : insert(0, array, element);
   }

   public static float[] addFirst(float[] array, float element) {
      return array == null ? add(array, element) : insert(0, array, element);
   }

   public static int[] addFirst(int[] array, int element) {
      return array == null ? add(array, element) : insert(0, array, element);
   }

   public static long[] addFirst(long[] array, long element) {
      return array == null ? add(array, element) : insert(0, array, element);
   }

   public static short[] addFirst(short[] array, short element) {
      return array == null ? add(array, element) : insert(0, array, element);
   }

   public static <T> T[] addFirst(T[] array, T element) {
      return (T[])(array == null ? add(array, element) : insert(0, array, element));
   }

   public static <T> T arraycopy(T source, int sourcePos, int destPos, int length, Function<Integer, T> allocator) {
      return arraycopy(source, sourcePos, allocator.apply(length), destPos, length);
   }

   public static <T> T arraycopy(T source, int sourcePos, int destPos, int length, Supplier<T> allocator) {
      return arraycopy(source, sourcePos, allocator.get(), destPos, length);
   }

   public static <T> T arraycopy(T source, int sourcePos, T dest, int destPos, int length) {
      System.arraycopy(source, sourcePos, dest, destPos, length);
      return dest;
   }

   public static boolean[] clone(boolean[] array) {
      return array != null ? (boolean[])array.clone() : null;
   }

   public static byte[] clone(byte[] array) {
      return array != null ? (byte[])array.clone() : null;
   }

   public static char[] clone(char[] array) {
      return array != null ? (char[])array.clone() : null;
   }

   public static double[] clone(double[] array) {
      return array != null ? (double[])array.clone() : null;
   }

   public static float[] clone(float[] array) {
      return array != null ? (float[])array.clone() : null;
   }

   public static int[] clone(int[] array) {
      return array != null ? (int[])array.clone() : null;
   }

   public static long[] clone(long[] array) {
      return array != null ? (long[])array.clone() : null;
   }

   public static short[] clone(short[] array) {
      return array != null ? (short[])array.clone() : null;
   }

   public static <T> T[] clone(T[] array) {
      return (T[])(array != null ? (Object[])array.clone() : null);
   }

   public static boolean contains(boolean[] array, boolean valueToFind) {
      return indexOf(array, valueToFind) != -1;
   }

   public static boolean contains(byte[] array, byte valueToFind) {
      return indexOf(array, valueToFind) != -1;
   }

   public static boolean contains(char[] array, char valueToFind) {
      return indexOf(array, valueToFind) != -1;
   }

   public static boolean contains(double[] array, double valueToFind) {
      return indexOf(array, valueToFind) != -1;
   }

   public static boolean contains(double[] array, double valueToFind, double tolerance) {
      return indexOf(array, valueToFind, 0, tolerance) != -1;
   }

   public static boolean contains(float[] array, float valueToFind) {
      return indexOf(array, valueToFind) != -1;
   }

   public static boolean contains(int[] array, int valueToFind) {
      return indexOf(array, valueToFind) != -1;
   }

   public static boolean contains(long[] array, long valueToFind) {
      return indexOf(array, valueToFind) != -1;
   }

   public static boolean contains(Object[] array, Object objectToFind) {
      return indexOf(array, objectToFind) != -1;
   }

   public static boolean contains(short[] array, short valueToFind) {
      return indexOf(array, valueToFind) != -1;
   }

   public static boolean containsAny(Object[] array, Object... objectsToFind) {
      return com.dfsek.terra.lib.commons.lang3.stream.Streams.of(objectsToFind).anyMatch(e -> contains(array, e));
   }

   private static Object copyArrayGrow1(Object array, Class<?> newArrayComponentType) {
      if (array != null) {
         int arrayLength = Array.getLength(array);
         Object newArray = Array.newInstance(array.getClass().getComponentType(), arrayLength + 1);
         System.arraycopy(array, 0, newArray, 0, arrayLength);
         return newArray;
      } else {
         return Array.newInstance(newArrayComponentType, 1);
      }
   }

   public static <T> T get(T[] array, int index) {
      return get(array, index, null);
   }

   public static <T> T get(T[] array, int index, T defaultValue) {
      return isArrayIndexValid(array, index) ? array[index] : defaultValue;
   }

   public static <T> Class<T> getComponentType(T[] array) {
      return ClassUtils.getComponentType(ObjectUtils.getClass((T)array));
   }

   public static int getLength(Object array) {
      return array != null ? Array.getLength(array) : 0;
   }

   public static int hashCode(Object array) {
      return new HashCodeBuilder().append(array).toHashCode();
   }

   public static BitSet indexesOf(boolean[] array, boolean valueToFind) {
      return indexesOf(array, valueToFind, 0);
   }

   public static BitSet indexesOf(boolean[] array, boolean valueToFind, int startIndex) {
      BitSet bitSet = new BitSet();
      if (array == null) {
         return bitSet;
      }

      while (startIndex < array.length) {
         startIndex = indexOf(array, valueToFind, startIndex);
         if (startIndex == -1) {
            break;
         }

         bitSet.set(startIndex);
         startIndex++;
      }

      return bitSet;
   }

   public static BitSet indexesOf(byte[] array, byte valueToFind) {
      return indexesOf(array, valueToFind, 0);
   }

   public static BitSet indexesOf(byte[] array, byte valueToFind, int startIndex) {
      BitSet bitSet = new BitSet();
      if (array == null) {
         return bitSet;
      }

      while (startIndex < array.length) {
         startIndex = indexOf(array, valueToFind, startIndex);
         if (startIndex == -1) {
            break;
         }

         bitSet.set(startIndex);
         startIndex++;
      }

      return bitSet;
   }

   public static BitSet indexesOf(char[] array, char valueToFind) {
      return indexesOf(array, valueToFind, 0);
   }

   public static BitSet indexesOf(char[] array, char valueToFind, int startIndex) {
      BitSet bitSet = new BitSet();
      if (array == null) {
         return bitSet;
      }

      while (startIndex < array.length) {
         startIndex = indexOf(array, valueToFind, startIndex);
         if (startIndex == -1) {
            break;
         }

         bitSet.set(startIndex);
         startIndex++;
      }

      return bitSet;
   }

   public static BitSet indexesOf(double[] array, double valueToFind) {
      return indexesOf(array, valueToFind, 0);
   }

   public static BitSet indexesOf(double[] array, double valueToFind, double tolerance) {
      return indexesOf(array, valueToFind, 0, tolerance);
   }

   public static BitSet indexesOf(double[] array, double valueToFind, int startIndex) {
      BitSet bitSet = new BitSet();
      if (array == null) {
         return bitSet;
      }

      while (startIndex < array.length) {
         startIndex = indexOf(array, valueToFind, startIndex);
         if (startIndex == -1) {
            break;
         }

         bitSet.set(startIndex);
         startIndex++;
      }

      return bitSet;
   }

   public static BitSet indexesOf(double[] array, double valueToFind, int startIndex, double tolerance) {
      BitSet bitSet = new BitSet();
      if (array == null) {
         return bitSet;
      }

      while (startIndex < array.length) {
         startIndex = indexOf(array, valueToFind, startIndex, tolerance);
         if (startIndex == -1) {
            break;
         }

         bitSet.set(startIndex);
         startIndex++;
      }

      return bitSet;
   }

   public static BitSet indexesOf(float[] array, float valueToFind) {
      return indexesOf(array, valueToFind, 0);
   }

   public static BitSet indexesOf(float[] array, float valueToFind, int startIndex) {
      BitSet bitSet = new BitSet();
      if (array == null) {
         return bitSet;
      }

      while (startIndex < array.length) {
         startIndex = indexOf(array, valueToFind, startIndex);
         if (startIndex == -1) {
            break;
         }

         bitSet.set(startIndex);
         startIndex++;
      }

      return bitSet;
   }

   public static BitSet indexesOf(int[] array, int valueToFind) {
      return indexesOf(array, valueToFind, 0);
   }

   public static BitSet indexesOf(int[] array, int valueToFind, int startIndex) {
      BitSet bitSet = new BitSet();
      if (array == null) {
         return bitSet;
      }

      while (startIndex < array.length) {
         startIndex = indexOf(array, valueToFind, startIndex);
         if (startIndex == -1) {
            break;
         }

         bitSet.set(startIndex);
         startIndex++;
      }

      return bitSet;
   }

   public static BitSet indexesOf(long[] array, long valueToFind) {
      return indexesOf(array, valueToFind, 0);
   }

   public static BitSet indexesOf(long[] array, long valueToFind, int startIndex) {
      BitSet bitSet = new BitSet();
      if (array == null) {
         return bitSet;
      }

      while (startIndex < array.length) {
         startIndex = indexOf(array, valueToFind, startIndex);
         if (startIndex == -1) {
            break;
         }

         bitSet.set(startIndex);
         startIndex++;
      }

      return bitSet;
   }

   public static BitSet indexesOf(Object[] array, Object objectToFind) {
      return indexesOf(array, objectToFind, 0);
   }

   public static BitSet indexesOf(Object[] array, Object objectToFind, int startIndex) {
      BitSet bitSet = new BitSet();
      if (array == null) {
         return bitSet;
      }

      while (startIndex < array.length) {
         startIndex = indexOf(array, objectToFind, startIndex);
         if (startIndex == -1) {
            break;
         }

         bitSet.set(startIndex);
         startIndex++;
      }

      return bitSet;
   }

   public static BitSet indexesOf(short[] array, short valueToFind) {
      return indexesOf(array, valueToFind, 0);
   }

   public static BitSet indexesOf(short[] array, short valueToFind, int startIndex) {
      BitSet bitSet = new BitSet();
      if (array == null) {
         return bitSet;
      }

      while (startIndex < array.length) {
         startIndex = indexOf(array, valueToFind, startIndex);
         if (startIndex == -1) {
            break;
         }

         bitSet.set(startIndex);
         startIndex++;
      }

      return bitSet;
   }

   public static int indexOf(boolean[] array, boolean valueToFind) {
      return indexOf(array, valueToFind, 0);
   }

   public static int indexOf(boolean[] array, boolean valueToFind, int startIndex) {
      if (isEmpty(array)) {
         return -1;
      }

      for (int i = max0(startIndex); i < array.length; i++) {
         if (valueToFind == array[i]) {
            return i;
         }
      }

      return -1;
   }

   public static int indexOf(byte[] array, byte valueToFind) {
      return indexOf(array, valueToFind, 0);
   }

   public static int indexOf(byte[] array, byte valueToFind, int startIndex) {
      if (array == null) {
         return -1;
      }

      for (int i = max0(startIndex); i < array.length; i++) {
         if (valueToFind == array[i]) {
            return i;
         }
      }

      return -1;
   }

   public static int indexOf(char[] array, char valueToFind) {
      return indexOf(array, valueToFind, 0);
   }

   public static int indexOf(char[] array, char valueToFind, int startIndex) {
      if (array == null) {
         return -1;
      }

      for (int i = max0(startIndex); i < array.length; i++) {
         if (valueToFind == array[i]) {
            return i;
         }
      }

      return -1;
   }

   public static int indexOf(double[] array, double valueToFind) {
      return indexOf(array, valueToFind, 0);
   }

   public static int indexOf(double[] array, double valueToFind, double tolerance) {
      return indexOf(array, valueToFind, 0, tolerance);
   }

   public static int indexOf(double[] array, double valueToFind, int startIndex) {
      if (isEmpty(array)) {
         return -1;
      }

      boolean searchNaN = Double.isNaN(valueToFind);

      for (int i = max0(startIndex); i < array.length; i++) {
         double element = array[i];
         if (valueToFind == element || searchNaN && Double.isNaN(element)) {
            return i;
         }
      }

      return -1;
   }

   public static int indexOf(double[] array, double valueToFind, int startIndex, double tolerance) {
      if (isEmpty(array)) {
         return -1;
      }

      double min = valueToFind - tolerance;
      double max = valueToFind + tolerance;

      for (int i = max0(startIndex); i < array.length; i++) {
         if (array[i] >= min && array[i] <= max) {
            return i;
         }
      }

      return -1;
   }

   public static int indexOf(float[] array, float valueToFind) {
      return indexOf(array, valueToFind, 0);
   }

   public static int indexOf(float[] array, float valueToFind, int startIndex) {
      if (isEmpty(array)) {
         return -1;
      }

      boolean searchNaN = Float.isNaN(valueToFind);

      for (int i = max0(startIndex); i < array.length; i++) {
         float element = array[i];
         if (valueToFind == element || searchNaN && Float.isNaN(element)) {
            return i;
         }
      }

      return -1;
   }

   public static int indexOf(int[] array, int valueToFind) {
      return indexOf(array, valueToFind, 0);
   }

   public static int indexOf(int[] array, int valueToFind, int startIndex) {
      if (array == null) {
         return -1;
      }

      for (int i = max0(startIndex); i < array.length; i++) {
         if (valueToFind == array[i]) {
            return i;
         }
      }

      return -1;
   }

   public static int indexOf(long[] array, long valueToFind) {
      return indexOf(array, valueToFind, 0);
   }

   public static int indexOf(long[] array, long valueToFind, int startIndex) {
      if (array == null) {
         return -1;
      }

      for (int i = max0(startIndex); i < array.length; i++) {
         if (valueToFind == array[i]) {
            return i;
         }
      }

      return -1;
   }

   public static int indexOf(Object[] array, Object objectToFind) {
      return indexOf(array, objectToFind, 0);
   }

   public static int indexOf(Object[] array, Object objectToFind, int startIndex) {
      if (array == null) {
         return -1;
      }

      startIndex = max0(startIndex);
      if (objectToFind == null) {
         for (int i = startIndex; i < array.length; i++) {
            if (array[i] == null) {
               return i;
            }
         }
      } else {
         for (int i = startIndex; i < array.length; i++) {
            if (objectToFind.equals(array[i])) {
               return i;
            }
         }
      }

      return -1;
   }

   public static int indexOf(short[] array, short valueToFind) {
      return indexOf(array, valueToFind, 0);
   }

   public static int indexOf(short[] array, short valueToFind, int startIndex) {
      if (array == null) {
         return -1;
      }

      for (int i = max0(startIndex); i < array.length; i++) {
         if (valueToFind == array[i]) {
            return i;
         }
      }

      return -1;
   }

   public static boolean[] insert(int index, boolean[] array, boolean... values) {
      if (array == null) {
         return null;
      }

      if (isEmpty(values)) {
         return clone(array);
      }

      if (index >= 0 && index <= array.length) {
         boolean[] result = new boolean[array.length + values.length];
         System.arraycopy(values, 0, result, index, values.length);
         if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
         }

         if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
         }

         return result;
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
      }
   }

   public static byte[] insert(int index, byte[] array, byte... values) {
      if (array == null) {
         return null;
      }

      if (isEmpty(values)) {
         return clone(array);
      }

      if (index >= 0 && index <= array.length) {
         byte[] result = new byte[array.length + values.length];
         System.arraycopy(values, 0, result, index, values.length);
         if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
         }

         if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
         }

         return result;
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
      }
   }

   public static char[] insert(int index, char[] array, char... values) {
      if (array == null) {
         return null;
      }

      if (isEmpty(values)) {
         return clone(array);
      }

      if (index >= 0 && index <= array.length) {
         char[] result = new char[array.length + values.length];
         System.arraycopy(values, 0, result, index, values.length);
         if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
         }

         if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
         }

         return result;
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
      }
   }

   public static double[] insert(int index, double[] array, double... values) {
      if (array == null) {
         return null;
      }

      if (isEmpty(values)) {
         return clone(array);
      }

      if (index >= 0 && index <= array.length) {
         double[] result = new double[array.length + values.length];
         System.arraycopy(values, 0, result, index, values.length);
         if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
         }

         if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
         }

         return result;
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
      }
   }

   public static float[] insert(int index, float[] array, float... values) {
      if (array == null) {
         return null;
      }

      if (isEmpty(values)) {
         return clone(array);
      }

      if (index >= 0 && index <= array.length) {
         float[] result = new float[array.length + values.length];
         System.arraycopy(values, 0, result, index, values.length);
         if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
         }

         if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
         }

         return result;
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
      }
   }

   public static int[] insert(int index, int[] array, int... values) {
      if (array == null) {
         return null;
      }

      if (isEmpty(values)) {
         return clone(array);
      }

      if (index >= 0 && index <= array.length) {
         int[] result = new int[array.length + values.length];
         System.arraycopy(values, 0, result, index, values.length);
         if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
         }

         if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
         }

         return result;
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
      }
   }

   public static long[] insert(int index, long[] array, long... values) {
      if (array == null) {
         return null;
      }

      if (isEmpty(values)) {
         return clone(array);
      }

      if (index >= 0 && index <= array.length) {
         long[] result = new long[array.length + values.length];
         System.arraycopy(values, 0, result, index, values.length);
         if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
         }

         if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
         }

         return result;
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
      }
   }

   public static short[] insert(int index, short[] array, short... values) {
      if (array == null) {
         return null;
      }

      if (isEmpty(values)) {
         return clone(array);
      }

      if (index >= 0 && index <= array.length) {
         short[] result = new short[array.length + values.length];
         System.arraycopy(values, 0, result, index, values.length);
         if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
         }

         if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
         }

         return result;
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
      }
   }

   @SafeVarargs
   public static <T> T[] insert(int index, T[] array, T... values) {
      if (array == null) {
         return null;
      }

      if (isEmpty(values)) {
         return (T[])clone(array);
      }

      if (index >= 0 && index <= array.length) {
         Class<T> type = getComponentType(array);
         int length = array.length + values.length;
         T[] result = newInstance(type, length);
         System.arraycopy(values, 0, result, index, values.length);
         if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
         }

         if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
         }

         return result;
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
      }
   }

   private static boolean isArrayEmpty(Object array) {
      return getLength(array) == 0;
   }

   public static <T> boolean isArrayIndexValid(T[] array, int index) {
      return index >= 0 && getLength(array) > index;
   }

   public static boolean isEmpty(boolean[] array) {
      return isArrayEmpty(array);
   }

   public static boolean isEmpty(byte[] array) {
      return isArrayEmpty(array);
   }

   public static boolean isEmpty(char[] array) {
      return isArrayEmpty(array);
   }

   public static boolean isEmpty(double[] array) {
      return isArrayEmpty(array);
   }

   public static boolean isEmpty(float[] array) {
      return isArrayEmpty(array);
   }

   public static boolean isEmpty(int[] array) {
      return isArrayEmpty(array);
   }

   public static boolean isEmpty(long[] array) {
      return isArrayEmpty(array);
   }

   public static boolean isEmpty(Object[] array) {
      return isArrayEmpty(array);
   }

   public static boolean isEmpty(short[] array) {
      return isArrayEmpty(array);
   }

   @Deprecated
   public static boolean isEquals(Object array1, Object array2) {
      return new EqualsBuilder().append(array1, array2).isEquals();
   }

   public static boolean isNotEmpty(boolean[] array) {
      return !isEmpty(array);
   }

   public static boolean isNotEmpty(byte[] array) {
      return !isEmpty(array);
   }

   public static boolean isNotEmpty(char[] array) {
      return !isEmpty(array);
   }

   public static boolean isNotEmpty(double[] array) {
      return !isEmpty(array);
   }

   public static boolean isNotEmpty(float[] array) {
      return !isEmpty(array);
   }

   public static boolean isNotEmpty(int[] array) {
      return !isEmpty(array);
   }

   public static boolean isNotEmpty(long[] array) {
      return !isEmpty(array);
   }

   public static boolean isNotEmpty(short[] array) {
      return !isEmpty(array);
   }

   public static <T> boolean isNotEmpty(T[] array) {
      return !isEmpty(array);
   }

   public static boolean isSameLength(boolean[] array1, boolean[] array2) {
      return getLength(array1) == getLength(array2);
   }

   public static boolean isSameLength(byte[] array1, byte[] array2) {
      return getLength(array1) == getLength(array2);
   }

   public static boolean isSameLength(char[] array1, char[] array2) {
      return getLength(array1) == getLength(array2);
   }

   public static boolean isSameLength(double[] array1, double[] array2) {
      return getLength(array1) == getLength(array2);
   }

   public static boolean isSameLength(float[] array1, float[] array2) {
      return getLength(array1) == getLength(array2);
   }

   public static boolean isSameLength(int[] array1, int[] array2) {
      return getLength(array1) == getLength(array2);
   }

   public static boolean isSameLength(long[] array1, long[] array2) {
      return getLength(array1) == getLength(array2);
   }

   public static boolean isSameLength(Object array1, Object array2) {
      return getLength(array1) == getLength(array2);
   }

   public static boolean isSameLength(Object[] array1, Object[] array2) {
      return getLength(array1) == getLength(array2);
   }

   public static boolean isSameLength(short[] array1, short[] array2) {
      return getLength(array1) == getLength(array2);
   }

   public static boolean isSameType(Object array1, Object array2) {
      if (array1 != null && array2 != null) {
         return array1.getClass().getName().equals(array2.getClass().getName());
      } else {
         throw new IllegalArgumentException("The Array must not be null");
      }
   }

   public static boolean isSorted(boolean[] array) {
      if (getLength(array) < 2) {
         return true;
      }

      boolean previous = array[0];

      for (boolean current : array) {
         if (BooleanUtils.compare(previous, current) > 0) {
            return false;
         }

         previous = current;
      }

      return true;
   }

   public static boolean isSorted(byte[] array) {
      if (getLength(array) < 2) {
         return true;
      }

      byte previous = array[0];

      for (byte current : array) {
         if (NumberUtils.compare(previous, current) > 0) {
            return false;
         }

         previous = current;
      }

      return true;
   }

   public static boolean isSorted(char[] array) {
      if (getLength(array) < 2) {
         return true;
      }

      char previous = array[0];

      for (char current : array) {
         if (CharUtils.compare(previous, current) > 0) {
            return false;
         }

         previous = current;
      }

      return true;
   }

   public static boolean isSorted(double[] array) {
      if (getLength(array) < 2) {
         return true;
      }

      double previous = array[0];

      for (double current : array) {
         if (Double.compare(previous, current) > 0) {
            return false;
         }

         previous = current;
      }

      return true;
   }

   public static boolean isSorted(float[] array) {
      if (getLength(array) < 2) {
         return true;
      }

      float previous = array[0];

      for (float current : array) {
         if (Float.compare(previous, current) > 0) {
            return false;
         }

         previous = current;
      }

      return true;
   }

   public static boolean isSorted(int[] array) {
      if (getLength(array) < 2) {
         return true;
      }

      int previous = array[0];

      for (int current : array) {
         if (NumberUtils.compare(previous, current) > 0) {
            return false;
         }

         previous = current;
      }

      return true;
   }

   public static boolean isSorted(long[] array) {
      if (getLength(array) < 2) {
         return true;
      }

      long previous = array[0];

      for (long current : array) {
         if (NumberUtils.compare(previous, current) > 0) {
            return false;
         }

         previous = current;
      }

      return true;
   }

   public static boolean isSorted(short[] array) {
      if (getLength(array) < 2) {
         return true;
      }

      short previous = array[0];

      for (short current : array) {
         if (NumberUtils.compare(previous, current) > 0) {
            return false;
         }

         previous = current;
      }

      return true;
   }

   public static <T extends Comparable<? super T>> boolean isSorted(T[] array) {
      return isSorted(array, Comparable::compareTo);
   }

   public static <T> boolean isSorted(T[] array, Comparator<T> comparator) {
      Objects.requireNonNull(comparator, "comparator");
      if (getLength(array) < 2) {
         return true;
      }

      T previous = array[0];

      for (T current : array) {
         if (comparator.compare(previous, current) > 0) {
            return false;
         }

         previous = current;
      }

      return true;
   }

   public static int lastIndexOf(boolean[] array, boolean valueToFind) {
      return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
   }

   public static int lastIndexOf(boolean[] array, boolean valueToFind, int startIndex) {
      if (!isEmpty(array) && startIndex >= 0) {
         if (startIndex >= array.length) {
            startIndex = array.length - 1;
         }

         for (int i = startIndex; i >= 0; i--) {
            if (valueToFind == array[i]) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static int lastIndexOf(byte[] array, byte valueToFind) {
      return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
   }

   public static int lastIndexOf(byte[] array, byte valueToFind, int startIndex) {
      if (array != null && startIndex >= 0) {
         if (startIndex >= array.length) {
            startIndex = array.length - 1;
         }

         for (int i = startIndex; i >= 0; i--) {
            if (valueToFind == array[i]) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static int lastIndexOf(char[] array, char valueToFind) {
      return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
   }

   public static int lastIndexOf(char[] array, char valueToFind, int startIndex) {
      if (array != null && startIndex >= 0) {
         if (startIndex >= array.length) {
            startIndex = array.length - 1;
         }

         for (int i = startIndex; i >= 0; i--) {
            if (valueToFind == array[i]) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static int lastIndexOf(double[] array, double valueToFind) {
      return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
   }

   public static int lastIndexOf(double[] array, double valueToFind, double tolerance) {
      return lastIndexOf(array, valueToFind, Integer.MAX_VALUE, tolerance);
   }

   public static int lastIndexOf(double[] array, double valueToFind, int startIndex) {
      if (!isEmpty(array) && startIndex >= 0) {
         if (startIndex >= array.length) {
            startIndex = array.length - 1;
         }

         for (int i = startIndex; i >= 0; i--) {
            if (valueToFind == array[i]) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static int lastIndexOf(double[] array, double valueToFind, int startIndex, double tolerance) {
      if (!isEmpty(array) && startIndex >= 0) {
         if (startIndex >= array.length) {
            startIndex = array.length - 1;
         }

         double min = valueToFind - tolerance;
         double max = valueToFind + tolerance;

         for (int i = startIndex; i >= 0; i--) {
            if (array[i] >= min && array[i] <= max) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static int lastIndexOf(float[] array, float valueToFind) {
      return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
   }

   public static int lastIndexOf(float[] array, float valueToFind, int startIndex) {
      if (!isEmpty(array) && startIndex >= 0) {
         if (startIndex >= array.length) {
            startIndex = array.length - 1;
         }

         for (int i = startIndex; i >= 0; i--) {
            if (valueToFind == array[i]) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static int lastIndexOf(int[] array, int valueToFind) {
      return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
   }

   public static int lastIndexOf(int[] array, int valueToFind, int startIndex) {
      if (array != null && startIndex >= 0) {
         if (startIndex >= array.length) {
            startIndex = array.length - 1;
         }

         for (int i = startIndex; i >= 0; i--) {
            if (valueToFind == array[i]) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static int lastIndexOf(long[] array, long valueToFind) {
      return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
   }

   public static int lastIndexOf(long[] array, long valueToFind, int startIndex) {
      if (array != null && startIndex >= 0) {
         if (startIndex >= array.length) {
            startIndex = array.length - 1;
         }

         for (int i = startIndex; i >= 0; i--) {
            if (valueToFind == array[i]) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static int lastIndexOf(Object[] array, Object objectToFind) {
      return lastIndexOf(array, objectToFind, Integer.MAX_VALUE);
   }

   public static int lastIndexOf(Object[] array, Object objectToFind, int startIndex) {
      if (array != null && startIndex >= 0) {
         if (startIndex >= array.length) {
            startIndex = array.length - 1;
         }

         if (objectToFind == null) {
            for (int i = startIndex; i >= 0; i--) {
               if (array[i] == null) {
                  return i;
               }
            }
         } else if (array.getClass().getComponentType().isInstance(objectToFind)) {
            for (int i = startIndex; i >= 0; i--) {
               if (objectToFind.equals(array[i])) {
                  return i;
               }
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static int lastIndexOf(short[] array, short valueToFind) {
      return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
   }

   public static int lastIndexOf(short[] array, short valueToFind, int startIndex) {
      if (array != null && startIndex >= 0) {
         if (startIndex >= array.length) {
            startIndex = array.length - 1;
         }

         for (int i = startIndex; i >= 0; i--) {
            if (valueToFind == array[i]) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private static int max0(int other) {
      return Math.max(0, other);
   }

   public static <T> T[] newInstance(Class<T> componentType, int length) {
      return (T[])((Object[])Array.newInstance(componentType, length));
   }

   public static <T> T[] nullTo(T[] array, T[] defaultArray) {
      return isEmpty(array) ? defaultArray : array;
   }

   public static boolean[] nullToEmpty(boolean[] array) {
      return isEmpty(array) ? EMPTY_BOOLEAN_ARRAY : array;
   }

   public static Boolean[] nullToEmpty(Boolean[] array) {
      return nullTo(array, EMPTY_BOOLEAN_OBJECT_ARRAY);
   }

   public static byte[] nullToEmpty(byte[] array) {
      return isEmpty(array) ? EMPTY_BYTE_ARRAY : array;
   }

   public static Byte[] nullToEmpty(Byte[] array) {
      return nullTo(array, EMPTY_BYTE_OBJECT_ARRAY);
   }

   public static char[] nullToEmpty(char[] array) {
      return isEmpty(array) ? EMPTY_CHAR_ARRAY : array;
   }

   public static Character[] nullToEmpty(Character[] array) {
      return nullTo(array, EMPTY_CHARACTER_OBJECT_ARRAY);
   }

   public static Class<?>[] nullToEmpty(Class<?>[] array) {
      return nullTo(array, EMPTY_CLASS_ARRAY);
   }

   public static double[] nullToEmpty(double[] array) {
      return isEmpty(array) ? EMPTY_DOUBLE_ARRAY : array;
   }

   public static Double[] nullToEmpty(Double[] array) {
      return nullTo(array, EMPTY_DOUBLE_OBJECT_ARRAY);
   }

   public static float[] nullToEmpty(float[] array) {
      return isEmpty(array) ? EMPTY_FLOAT_ARRAY : array;
   }

   public static Float[] nullToEmpty(Float[] array) {
      return nullTo(array, EMPTY_FLOAT_OBJECT_ARRAY);
   }

   public static int[] nullToEmpty(int[] array) {
      return isEmpty(array) ? EMPTY_INT_ARRAY : array;
   }

   public static Integer[] nullToEmpty(Integer[] array) {
      return nullTo(array, EMPTY_INTEGER_OBJECT_ARRAY);
   }

   public static long[] nullToEmpty(long[] array) {
      return isEmpty(array) ? EMPTY_LONG_ARRAY : array;
   }

   public static Long[] nullToEmpty(Long[] array) {
      return nullTo(array, EMPTY_LONG_OBJECT_ARRAY);
   }

   public static Object[] nullToEmpty(Object[] array) {
      return nullTo(array, EMPTY_OBJECT_ARRAY);
   }

   public static short[] nullToEmpty(short[] array) {
      return isEmpty(array) ? EMPTY_SHORT_ARRAY : array;
   }

   public static Short[] nullToEmpty(Short[] array) {
      return nullTo(array, EMPTY_SHORT_OBJECT_ARRAY);
   }

   public static String[] nullToEmpty(String[] array) {
      return nullTo(array, EMPTY_STRING_ARRAY);
   }

   public static <T> T[] nullToEmpty(T[] array, Class<T[]> type) {
      if (type == null) {
         throw new IllegalArgumentException("The type must not be null");
      } else {
         return (T[])(array == null ? (Object[])type.cast(Array.newInstance(type.getComponentType(), 0)) : array);
      }
   }

   private static ThreadLocalRandom random() {
      return ThreadLocalRandom.current();
   }

   public static boolean[] remove(boolean[] array, int index) {
      return (boolean[])remove(array, index);
   }

   public static byte[] remove(byte[] array, int index) {
      return (byte[])remove(array, index);
   }

   public static char[] remove(char[] array, int index) {
      return (char[])remove(array, index);
   }

   public static double[] remove(double[] array, int index) {
      return (double[])remove(array, index);
   }

   public static float[] remove(float[] array, int index) {
      return (float[])remove(array, index);
   }

   public static int[] remove(int[] array, int index) {
      return (int[])remove(array, index);
   }

   public static long[] remove(long[] array, int index) {
      return (long[])remove(array, index);
   }

   private static Object remove(Object array, int index) {
      int length = getLength(array);
      if (index >= 0 && index < length) {
         Object result = Array.newInstance(array.getClass().getComponentType(), length - 1);
         System.arraycopy(array, 0, result, 0, index);
         if (index < length - 1) {
            System.arraycopy(array, index + 1, result, index, length - index - 1);
         }

         return result;
      } else {
         throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
      }
   }

   public static short[] remove(short[] array, int index) {
      return (short[])remove(array, index);
   }

   public static <T> T[] remove(T[] array, int index) {
      return (T[])((Object[])remove(array, index));
   }

   public static boolean[] removeAll(boolean[] array, int... indices) {
      return (boolean[])removeAll(array, indices);
   }

   public static byte[] removeAll(byte[] array, int... indices) {
      return (byte[])removeAll(array, indices);
   }

   public static char[] removeAll(char[] array, int... indices) {
      return (char[])removeAll(array, indices);
   }

   public static double[] removeAll(double[] array, int... indices) {
      return (double[])removeAll(array, indices);
   }

   public static float[] removeAll(float[] array, int... indices) {
      return (float[])removeAll(array, indices);
   }

   public static int[] removeAll(int[] array, int... indices) {
      return (int[])removeAll(array, indices);
   }

   public static long[] removeAll(long[] array, int... indices) {
      return (long[])removeAll(array, indices);
   }

   static Object removeAll(Object array, BitSet indices) {
      if (array == null) {
         return null;
      }

      int srcLength = getLength(array);
      int removals = indices.cardinality();
      Object result = Array.newInstance(array.getClass().getComponentType(), srcLength - removals);
      int srcIndex = 0;
      int destIndex = 0;

      int set;
      while ((set = indices.nextSetBit(srcIndex)) != -1) {
         int count = set - srcIndex;
         if (count > 0) {
            System.arraycopy(array, srcIndex, result, destIndex, count);
            destIndex += count;
         }

         srcIndex = indices.nextClearBit(set);
      }

      int count = srcLength - srcIndex;
      if (count > 0) {
         System.arraycopy(array, srcIndex, result, destIndex, count);
      }

      return result;
   }

   static Object removeAll(Object array, int... indices) {
      if (array == null) {
         return null;
      }

      int length = getLength(array);
      int diff = 0;
      int[] clonedIndices = ArraySorter.sort(clone(indices));
      if (isNotEmpty(clonedIndices)) {
         int i = clonedIndices.length;
         int prevIndex = length;

         while (--i >= 0) {
            int index = clonedIndices[i];
            if (index < 0 || index >= length) {
               throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
            }

            if (index < prevIndex) {
               diff++;
               prevIndex = index;
            }
         }
      }

      Object result = Array.newInstance(array.getClass().getComponentType(), length - diff);
      if (diff < length && clonedIndices != null) {
         int end = length;
         int dest = length - diff;

         for (int i = clonedIndices.length - 1; i >= 0; i--) {
            int index = clonedIndices[i];
            if (end - index > 1) {
               int cp = end - index - 1;
               dest -= cp;
               System.arraycopy(array, index + 1, result, dest, cp);
            }

            end = index;
         }

         if (end > 0) {
            System.arraycopy(array, 0, result, 0, end);
         }
      }

      return result;
   }

   public static short[] removeAll(short[] array, int... indices) {
      return (short[])removeAll(array, indices);
   }

   public static <T> T[] removeAll(T[] array, int... indices) {
      return (T[])((Object[])removeAll(array, indices));
   }

   @Deprecated
   public static boolean[] removeAllOccurences(boolean[] array, boolean element) {
      return (boolean[])removeAll(array, indexesOf(array, element));
   }

   @Deprecated
   public static byte[] removeAllOccurences(byte[] array, byte element) {
      return (byte[])removeAll(array, indexesOf(array, element));
   }

   @Deprecated
   public static char[] removeAllOccurences(char[] array, char element) {
      return (char[])removeAll(array, indexesOf(array, element));
   }

   @Deprecated
   public static double[] removeAllOccurences(double[] array, double element) {
      return (double[])removeAll(array, indexesOf(array, element));
   }

   @Deprecated
   public static float[] removeAllOccurences(float[] array, float element) {
      return (float[])removeAll(array, indexesOf(array, element));
   }

   @Deprecated
   public static int[] removeAllOccurences(int[] array, int element) {
      return (int[])removeAll(array, indexesOf(array, element));
   }

   @Deprecated
   public static long[] removeAllOccurences(long[] array, long element) {
      return (long[])removeAll(array, indexesOf(array, element));
   }

   @Deprecated
   public static short[] removeAllOccurences(short[] array, short element) {
      return (short[])removeAll(array, indexesOf(array, element));
   }

   @Deprecated
   public static <T> T[] removeAllOccurences(T[] array, T element) {
      return (T[])((Object[])removeAll(array, indexesOf(array, element)));
   }

   public static boolean[] removeAllOccurrences(boolean[] array, boolean element) {
      return (boolean[])removeAll(array, indexesOf(array, element));
   }

   public static byte[] removeAllOccurrences(byte[] array, byte element) {
      return (byte[])removeAll(array, indexesOf(array, element));
   }

   public static char[] removeAllOccurrences(char[] array, char element) {
      return (char[])removeAll(array, indexesOf(array, element));
   }

   public static double[] removeAllOccurrences(double[] array, double element) {
      return (double[])removeAll(array, indexesOf(array, element));
   }

   public static float[] removeAllOccurrences(float[] array, float element) {
      return (float[])removeAll(array, indexesOf(array, element));
   }

   public static int[] removeAllOccurrences(int[] array, int element) {
      return (int[])removeAll(array, indexesOf(array, element));
   }

   public static long[] removeAllOccurrences(long[] array, long element) {
      return (long[])removeAll(array, indexesOf(array, element));
   }

   public static short[] removeAllOccurrences(short[] array, short element) {
      return (short[])removeAll(array, indexesOf(array, element));
   }

   public static <T> T[] removeAllOccurrences(T[] array, T element) {
      return (T[])((Object[])removeAll(array, indexesOf(array, element)));
   }

   public static boolean[] removeElement(boolean[] array, boolean element) {
      int index = indexOf(array, element);
      return index == -1 ? clone(array) : remove(array, index);
   }

   public static byte[] removeElement(byte[] array, byte element) {
      int index = indexOf(array, element);
      return index == -1 ? clone(array) : remove(array, index);
   }

   public static char[] removeElement(char[] array, char element) {
      int index = indexOf(array, element);
      return index == -1 ? clone(array) : remove(array, index);
   }

   public static double[] removeElement(double[] array, double element) {
      int index = indexOf(array, element);
      return index == -1 ? clone(array) : remove(array, index);
   }

   public static float[] removeElement(float[] array, float element) {
      int index = indexOf(array, element);
      return index == -1 ? clone(array) : remove(array, index);
   }

   public static int[] removeElement(int[] array, int element) {
      int index = indexOf(array, element);
      return index == -1 ? clone(array) : remove(array, index);
   }

   public static long[] removeElement(long[] array, long element) {
      int index = indexOf(array, element);
      return index == -1 ? clone(array) : remove(array, index);
   }

   public static short[] removeElement(short[] array, short element) {
      int index = indexOf(array, element);
      return index == -1 ? clone(array) : remove(array, index);
   }

   public static <T> T[] removeElement(T[] array, Object element) {
      int index = indexOf(array, element);
      return (T[])(index == -1 ? clone(array) : remove(array, index));
   }

   public static boolean[] removeElements(boolean[] array, boolean... values) {
      if (!isEmpty(array) && !isEmpty(values)) {
         HashMap<Boolean, MutableInt> occurrences = new HashMap<>(2);

         for (boolean v : values) {
            Boolean boxed = v;
            MutableInt count = occurrences.get(boxed);
            if (count == null) {
               occurrences.put(boxed, new MutableInt(1));
            } else {
               count.increment();
            }
         }

         BitSet toRemove = new BitSet();

         for (int i = 0; i < array.length; i++) {
            boolean key = array[i];
            MutableInt count = occurrences.get(key);
            if (count != null) {
               if (count.decrementAndGet() == 0) {
                  occurrences.remove(key);
               }

               toRemove.set(i);
            }
         }

         return (boolean[])removeAll(array, toRemove);
      } else {
         return clone(array);
      }
   }

   public static byte[] removeElements(byte[] array, byte... values) {
      if (!isEmpty(array) && !isEmpty(values)) {
         Map<Byte, MutableInt> occurrences = new HashMap<>(values.length);

         for (byte v : values) {
            Byte boxed = v;
            MutableInt count = occurrences.get(boxed);
            if (count == null) {
               occurrences.put(boxed, new MutableInt(1));
            } else {
               count.increment();
            }
         }

         BitSet toRemove = new BitSet();

         for (int i = 0; i < array.length; i++) {
            byte key = array[i];
            MutableInt count = occurrences.get(key);
            if (count != null) {
               if (count.decrementAndGet() == 0) {
                  occurrences.remove(key);
               }

               toRemove.set(i);
            }
         }

         return (byte[])removeAll(array, toRemove);
      } else {
         return clone(array);
      }
   }

   public static char[] removeElements(char[] array, char... values) {
      if (!isEmpty(array) && !isEmpty(values)) {
         HashMap<Character, MutableInt> occurrences = new HashMap<>(values.length);

         for (char v : values) {
            Character boxed = v;
            MutableInt count = occurrences.get(boxed);
            if (count == null) {
               occurrences.put(boxed, new MutableInt(1));
            } else {
               count.increment();
            }
         }

         BitSet toRemove = new BitSet();

         for (int i = 0; i < array.length; i++) {
            char key = array[i];
            MutableInt count = occurrences.get(key);
            if (count != null) {
               if (count.decrementAndGet() == 0) {
                  occurrences.remove(key);
               }

               toRemove.set(i);
            }
         }

         return (char[])removeAll(array, toRemove);
      } else {
         return clone(array);
      }
   }

   public static double[] removeElements(double[] array, double... values) {
      if (!isEmpty(array) && !isEmpty(values)) {
         HashMap<Double, MutableInt> occurrences = new HashMap<>(values.length);

         for (double v : values) {
            Double boxed = v;
            MutableInt count = occurrences.get(boxed);
            if (count == null) {
               occurrences.put(boxed, new MutableInt(1));
            } else {
               count.increment();
            }
         }

         BitSet toRemove = new BitSet();

         for (int i = 0; i < array.length; i++) {
            double key = array[i];
            MutableInt count = occurrences.get(key);
            if (count != null) {
               if (count.decrementAndGet() == 0) {
                  occurrences.remove(key);
               }

               toRemove.set(i);
            }
         }

         return (double[])removeAll(array, toRemove);
      } else {
         return clone(array);
      }
   }

   public static float[] removeElements(float[] array, float... values) {
      if (!isEmpty(array) && !isEmpty(values)) {
         HashMap<Float, MutableInt> occurrences = new HashMap<>(values.length);

         for (float v : values) {
            Float boxed = v;
            MutableInt count = occurrences.get(boxed);
            if (count == null) {
               occurrences.put(boxed, new MutableInt(1));
            } else {
               count.increment();
            }
         }

         BitSet toRemove = new BitSet();

         for (int i = 0; i < array.length; i++) {
            float key = array[i];
            MutableInt count = occurrences.get(key);
            if (count != null) {
               if (count.decrementAndGet() == 0) {
                  occurrences.remove(key);
               }

               toRemove.set(i);
            }
         }

         return (float[])removeAll(array, toRemove);
      } else {
         return clone(array);
      }
   }

   public static int[] removeElements(int[] array, int... values) {
      if (!isEmpty(array) && !isEmpty(values)) {
         HashMap<Integer, MutableInt> occurrences = new HashMap<>(values.length);

         for (int v : values) {
            Integer boxed = v;
            MutableInt count = occurrences.get(boxed);
            if (count == null) {
               occurrences.put(boxed, new MutableInt(1));
            } else {
               count.increment();
            }
         }

         BitSet toRemove = new BitSet();

         for (int i = 0; i < array.length; i++) {
            int key = array[i];
            MutableInt count = occurrences.get(key);
            if (count != null) {
               if (count.decrementAndGet() == 0) {
                  occurrences.remove(key);
               }

               toRemove.set(i);
            }
         }

         return (int[])removeAll(array, toRemove);
      } else {
         return clone(array);
      }
   }

   public static long[] removeElements(long[] array, long... values) {
      if (!isEmpty(array) && !isEmpty(values)) {
         HashMap<Long, MutableInt> occurrences = new HashMap<>(values.length);

         for (long v : values) {
            Long boxed = v;
            MutableInt count = occurrences.get(boxed);
            if (count == null) {
               occurrences.put(boxed, new MutableInt(1));
            } else {
               count.increment();
            }
         }

         BitSet toRemove = new BitSet();

         for (int i = 0; i < array.length; i++) {
            long key = array[i];
            MutableInt count = occurrences.get(key);
            if (count != null) {
               if (count.decrementAndGet() == 0) {
                  occurrences.remove(key);
               }

               toRemove.set(i);
            }
         }

         return (long[])removeAll(array, toRemove);
      } else {
         return clone(array);
      }
   }

   public static short[] removeElements(short[] array, short... values) {
      if (!isEmpty(array) && !isEmpty(values)) {
         HashMap<Short, MutableInt> occurrences = new HashMap<>(values.length);

         for (short v : values) {
            Short boxed = v;
            MutableInt count = occurrences.get(boxed);
            if (count == null) {
               occurrences.put(boxed, new MutableInt(1));
            } else {
               count.increment();
            }
         }

         BitSet toRemove = new BitSet();

         for (int i = 0; i < array.length; i++) {
            short key = array[i];
            MutableInt count = occurrences.get(key);
            if (count != null) {
               if (count.decrementAndGet() == 0) {
                  occurrences.remove(key);
               }

               toRemove.set(i);
            }
         }

         return (short[])removeAll(array, toRemove);
      } else {
         return clone(array);
      }
   }

   @SafeVarargs
   public static <T> T[] removeElements(T[] array, T... values) {
      if (!isEmpty(array) && !isEmpty(values)) {
         HashMap<T, MutableInt> occurrences = new HashMap<>(values.length);

         for (T v : values) {
            MutableInt count = occurrences.get(v);
            if (count == null) {
               occurrences.put(v, new MutableInt(1));
            } else {
               count.increment();
            }
         }

         BitSet toRemove = new BitSet();

         for (int i = 0; i < array.length; i++) {
            T key = array[i];
            MutableInt count = occurrences.get(key);
            if (count != null) {
               if (count.decrementAndGet() == 0) {
                  occurrences.remove(key);
               }

               toRemove.set(i);
            }
         }

         return (T[])((Object[])removeAll(array, toRemove));
      } else {
         return (T[])clone(array);
      }
   }

   public static void reverse(boolean[] array) {
      if (array != null) {
         reverse(array, 0, array.length);
      }
   }

   public static void reverse(boolean[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array != null) {
         int i = Math.max(startIndexInclusive, 0);

         for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; i++) {
            boolean tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
         }
      }
   }

   public static void reverse(byte[] array) {
      if (array != null) {
         reverse(array, 0, array.length);
      }
   }

   public static void reverse(byte[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array != null) {
         int i = Math.max(startIndexInclusive, 0);

         for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; i++) {
            byte tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
         }
      }
   }

   public static void reverse(char[] array) {
      if (array != null) {
         reverse(array, 0, array.length);
      }
   }

   public static void reverse(char[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array != null) {
         int i = Math.max(startIndexInclusive, 0);

         for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; i++) {
            char tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
         }
      }
   }

   public static void reverse(double[] array) {
      if (array != null) {
         reverse(array, 0, array.length);
      }
   }

   public static void reverse(double[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array != null) {
         int i = Math.max(startIndexInclusive, 0);

         for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; i++) {
            double tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
         }
      }
   }

   public static void reverse(float[] array) {
      if (array != null) {
         reverse(array, 0, array.length);
      }
   }

   public static void reverse(float[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array != null) {
         int i = Math.max(startIndexInclusive, 0);

         for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; i++) {
            float tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
         }
      }
   }

   public static void reverse(int[] array) {
      if (array != null) {
         reverse(array, 0, array.length);
      }
   }

   public static void reverse(int[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array != null) {
         int i = Math.max(startIndexInclusive, 0);

         for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; i++) {
            int tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
         }
      }
   }

   public static void reverse(long[] array) {
      if (array != null) {
         reverse(array, 0, array.length);
      }
   }

   public static void reverse(long[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array != null) {
         int i = Math.max(startIndexInclusive, 0);

         for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; i++) {
            long tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
         }
      }
   }

   public static void reverse(Object[] array) {
      if (array != null) {
         reverse(array, 0, array.length);
      }
   }

   public static void reverse(Object[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array != null) {
         int i = Math.max(startIndexInclusive, 0);

         for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; i++) {
            Object tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
         }
      }
   }

   public static void reverse(short[] array) {
      if (array != null) {
         reverse(array, 0, array.length);
      }
   }

   public static void reverse(short[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array != null) {
         int i = Math.max(startIndexInclusive, 0);

         for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; i++) {
            short tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
         }
      }
   }

   public static <T> T[] setAll(T[] array, IntFunction<? extends T> generator) {
      if (array != null && generator != null) {
         Arrays.setAll(array, generator);
      }

      return array;
   }

   public static <T> T[] setAll(T[] array, Supplier<? extends T> generator) {
      if (array != null && generator != null) {
         for (int i = 0; i < array.length; i++) {
            array[i] = (T)generator.get();
         }
      }

      return array;
   }

   public static void shift(boolean[] array, int offset) {
      if (array != null) {
         shift(array, 0, array.length, offset);
      }
   }

   public static void shift(boolean[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
      if (array != null && startIndexInclusive < array.length - 1 && endIndexExclusive > 0) {
         startIndexInclusive = max0(startIndexInclusive);
         endIndexExclusive = Math.min(endIndexExclusive, array.length);
         int n = endIndexExclusive - startIndexInclusive;
         if (n > 1) {
            offset %= n;
            if (offset < 0) {
               offset += n;
            }

            while (n > 1 && offset > 0) {
               int nOffset = n - offset;
               if (offset > nOffset) {
                  swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                  n = offset;
                  offset -= nOffset;
               } else {
                  if (offset >= nOffset) {
                     swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                     break;
                  }

                  swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                  startIndexInclusive += offset;
                  n = nOffset;
               }
            }
         }
      }
   }

   public static void shift(byte[] array, int offset) {
      if (array != null) {
         shift(array, 0, array.length, offset);
      }
   }

   public static void shift(byte[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
      if (array != null && startIndexInclusive < array.length - 1 && endIndexExclusive > 0) {
         startIndexInclusive = max0(startIndexInclusive);
         endIndexExclusive = Math.min(endIndexExclusive, array.length);
         int n = endIndexExclusive - startIndexInclusive;
         if (n > 1) {
            offset %= n;
            if (offset < 0) {
               offset += n;
            }

            while (n > 1 && offset > 0) {
               int nOffset = n - offset;
               if (offset > nOffset) {
                  swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                  n = offset;
                  offset -= nOffset;
               } else {
                  if (offset >= nOffset) {
                     swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                     break;
                  }

                  swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                  startIndexInclusive += offset;
                  n = nOffset;
               }
            }
         }
      }
   }

   public static void shift(char[] array, int offset) {
      if (array != null) {
         shift(array, 0, array.length, offset);
      }
   }

   public static void shift(char[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
      if (array != null && startIndexInclusive < array.length - 1 && endIndexExclusive > 0) {
         startIndexInclusive = max0(startIndexInclusive);
         endIndexExclusive = Math.min(endIndexExclusive, array.length);
         int n = endIndexExclusive - startIndexInclusive;
         if (n > 1) {
            offset %= n;
            if (offset < 0) {
               offset += n;
            }

            while (n > 1 && offset > 0) {
               int nOffset = n - offset;
               if (offset > nOffset) {
                  swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                  n = offset;
                  offset -= nOffset;
               } else {
                  if (offset >= nOffset) {
                     swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                     break;
                  }

                  swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                  startIndexInclusive += offset;
                  n = nOffset;
               }
            }
         }
      }
   }

   public static void shift(double[] array, int offset) {
      if (array != null) {
         shift(array, 0, array.length, offset);
      }
   }

   public static void shift(double[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
      if (array != null && startIndexInclusive < array.length - 1 && endIndexExclusive > 0) {
         startIndexInclusive = max0(startIndexInclusive);
         endIndexExclusive = Math.min(endIndexExclusive, array.length);
         int n = endIndexExclusive - startIndexInclusive;
         if (n > 1) {
            offset %= n;
            if (offset < 0) {
               offset += n;
            }

            while (n > 1 && offset > 0) {
               int nOffset = n - offset;
               if (offset > nOffset) {
                  swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                  n = offset;
                  offset -= nOffset;
               } else {
                  if (offset >= nOffset) {
                     swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                     break;
                  }

                  swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                  startIndexInclusive += offset;
                  n = nOffset;
               }
            }
         }
      }
   }

   public static void shift(float[] array, int offset) {
      if (array != null) {
         shift(array, 0, array.length, offset);
      }
   }

   public static void shift(float[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
      if (array != null && startIndexInclusive < array.length - 1 && endIndexExclusive > 0) {
         startIndexInclusive = max0(startIndexInclusive);
         endIndexExclusive = Math.min(endIndexExclusive, array.length);
         int n = endIndexExclusive - startIndexInclusive;
         if (n > 1) {
            offset %= n;
            if (offset < 0) {
               offset += n;
            }

            while (n > 1 && offset > 0) {
               int nOffset = n - offset;
               if (offset > nOffset) {
                  swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                  n = offset;
                  offset -= nOffset;
               } else {
                  if (offset >= nOffset) {
                     swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                     break;
                  }

                  swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                  startIndexInclusive += offset;
                  n = nOffset;
               }
            }
         }
      }
   }

   public static void shift(int[] array, int offset) {
      if (array != null) {
         shift(array, 0, array.length, offset);
      }
   }

   public static void shift(int[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
      if (array != null && startIndexInclusive < array.length - 1 && endIndexExclusive > 0) {
         startIndexInclusive = max0(startIndexInclusive);
         endIndexExclusive = Math.min(endIndexExclusive, array.length);
         int n = endIndexExclusive - startIndexInclusive;
         if (n > 1) {
            offset %= n;
            if (offset < 0) {
               offset += n;
            }

            while (n > 1 && offset > 0) {
               int nOffset = n - offset;
               if (offset > nOffset) {
                  swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                  n = offset;
                  offset -= nOffset;
               } else {
                  if (offset >= nOffset) {
                     swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                     break;
                  }

                  swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                  startIndexInclusive += offset;
                  n = nOffset;
               }
            }
         }
      }
   }

   public static void shift(long[] array, int offset) {
      if (array != null) {
         shift(array, 0, array.length, offset);
      }
   }

   public static void shift(long[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
      if (array != null && startIndexInclusive < array.length - 1 && endIndexExclusive > 0) {
         startIndexInclusive = max0(startIndexInclusive);
         endIndexExclusive = Math.min(endIndexExclusive, array.length);
         int n = endIndexExclusive - startIndexInclusive;
         if (n > 1) {
            offset %= n;
            if (offset < 0) {
               offset += n;
            }

            while (n > 1 && offset > 0) {
               int nOffset = n - offset;
               if (offset > nOffset) {
                  swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                  n = offset;
                  offset -= nOffset;
               } else {
                  if (offset >= nOffset) {
                     swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                     break;
                  }

                  swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                  startIndexInclusive += offset;
                  n = nOffset;
               }
            }
         }
      }
   }

   public static void shift(Object[] array, int offset) {
      if (array != null) {
         shift(array, 0, array.length, offset);
      }
   }

   public static void shift(Object[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
      if (array != null && startIndexInclusive < array.length - 1 && endIndexExclusive > 0) {
         startIndexInclusive = max0(startIndexInclusive);
         endIndexExclusive = Math.min(endIndexExclusive, array.length);
         int n = endIndexExclusive - startIndexInclusive;
         if (n > 1) {
            offset %= n;
            if (offset < 0) {
               offset += n;
            }

            while (n > 1 && offset > 0) {
               int nOffset = n - offset;
               if (offset > nOffset) {
                  swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                  n = offset;
                  offset -= nOffset;
               } else {
                  if (offset >= nOffset) {
                     swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                     break;
                  }

                  swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                  startIndexInclusive += offset;
                  n = nOffset;
               }
            }
         }
      }
   }

   public static void shift(short[] array, int offset) {
      if (array != null) {
         shift(array, 0, array.length, offset);
      }
   }

   public static void shift(short[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
      if (array != null && startIndexInclusive < array.length - 1 && endIndexExclusive > 0) {
         startIndexInclusive = max0(startIndexInclusive);
         endIndexExclusive = Math.min(endIndexExclusive, array.length);
         int n = endIndexExclusive - startIndexInclusive;
         if (n > 1) {
            offset %= n;
            if (offset < 0) {
               offset += n;
            }

            while (n > 1 && offset > 0) {
               int nOffset = n - offset;
               if (offset > nOffset) {
                  swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                  n = offset;
                  offset -= nOffset;
               } else {
                  if (offset >= nOffset) {
                     swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                     break;
                  }

                  swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                  startIndexInclusive += offset;
                  n = nOffset;
               }
            }
         }
      }
   }

   public static void shuffle(boolean[] array) {
      shuffle(array, random());
   }

   public static void shuffle(boolean[] array, Random random) {
      for (int i = array.length; i > 1; i--) {
         swap(array, i - 1, random.nextInt(i), 1);
      }
   }

   public static void shuffle(byte[] array) {
      shuffle(array, random());
   }

   public static void shuffle(byte[] array, Random random) {
      for (int i = array.length; i > 1; i--) {
         swap(array, i - 1, random.nextInt(i), 1);
      }
   }

   public static void shuffle(char[] array) {
      shuffle(array, random());
   }

   public static void shuffle(char[] array, Random random) {
      for (int i = array.length; i > 1; i--) {
         swap(array, i - 1, random.nextInt(i), 1);
      }
   }

   public static void shuffle(double[] array) {
      shuffle(array, random());
   }

   public static void shuffle(double[] array, Random random) {
      for (int i = array.length; i > 1; i--) {
         swap(array, i - 1, random.nextInt(i), 1);
      }
   }

   public static void shuffle(float[] array) {
      shuffle(array, random());
   }

   public static void shuffle(float[] array, Random random) {
      for (int i = array.length; i > 1; i--) {
         swap(array, i - 1, random.nextInt(i), 1);
      }
   }

   public static void shuffle(int[] array) {
      shuffle(array, random());
   }

   public static void shuffle(int[] array, Random random) {
      for (int i = array.length; i > 1; i--) {
         swap(array, i - 1, random.nextInt(i), 1);
      }
   }

   public static void shuffle(long[] array) {
      shuffle(array, random());
   }

   public static void shuffle(long[] array, Random random) {
      for (int i = array.length; i > 1; i--) {
         swap(array, i - 1, random.nextInt(i), 1);
      }
   }

   public static void shuffle(Object[] array) {
      shuffle(array, random());
   }

   public static void shuffle(Object[] array, Random random) {
      for (int i = array.length; i > 1; i--) {
         swap(array, i - 1, random.nextInt(i), 1);
      }
   }

   public static void shuffle(short[] array) {
      shuffle(array, random());
   }

   public static void shuffle(short[] array, Random random) {
      for (int i = array.length; i > 1; i--) {
         swap(array, i - 1, random.nextInt(i), 1);
      }
   }

   public static boolean[] subarray(boolean[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array == null) {
         return null;
      }

      startIndexInclusive = max0(startIndexInclusive);
      endIndexExclusive = Math.min(endIndexExclusive, array.length);
      int newSize = endIndexExclusive - startIndexInclusive;
      return newSize <= 0 ? EMPTY_BOOLEAN_ARRAY : arraycopy(array, startIndexInclusive, 0, newSize, x$0 -> new boolean[x$0]);
   }

   public static byte[] subarray(byte[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array == null) {
         return null;
      }

      startIndexInclusive = max0(startIndexInclusive);
      endIndexExclusive = Math.min(endIndexExclusive, array.length);
      int newSize = endIndexExclusive - startIndexInclusive;
      return newSize <= 0 ? EMPTY_BYTE_ARRAY : arraycopy(array, startIndexInclusive, 0, newSize, x$0 -> new byte[x$0]);
   }

   public static char[] subarray(char[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array == null) {
         return null;
      }

      startIndexInclusive = max0(startIndexInclusive);
      endIndexExclusive = Math.min(endIndexExclusive, array.length);
      int newSize = endIndexExclusive - startIndexInclusive;
      return newSize <= 0 ? EMPTY_CHAR_ARRAY : arraycopy(array, startIndexInclusive, 0, newSize, x$0 -> new char[x$0]);
   }

   public static double[] subarray(double[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array == null) {
         return null;
      }

      startIndexInclusive = max0(startIndexInclusive);
      endIndexExclusive = Math.min(endIndexExclusive, array.length);
      int newSize = endIndexExclusive - startIndexInclusive;
      return newSize <= 0 ? EMPTY_DOUBLE_ARRAY : arraycopy(array, startIndexInclusive, 0, newSize, x$0 -> new double[x$0]);
   }

   public static float[] subarray(float[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array == null) {
         return null;
      }

      startIndexInclusive = max0(startIndexInclusive);
      endIndexExclusive = Math.min(endIndexExclusive, array.length);
      int newSize = endIndexExclusive - startIndexInclusive;
      return newSize <= 0 ? EMPTY_FLOAT_ARRAY : arraycopy(array, startIndexInclusive, 0, newSize, x$0 -> new float[x$0]);
   }

   public static int[] subarray(int[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array == null) {
         return null;
      }

      startIndexInclusive = max0(startIndexInclusive);
      endIndexExclusive = Math.min(endIndexExclusive, array.length);
      int newSize = endIndexExclusive - startIndexInclusive;
      return newSize <= 0 ? EMPTY_INT_ARRAY : arraycopy(array, startIndexInclusive, 0, newSize, x$0 -> new int[x$0]);
   }

   public static long[] subarray(long[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array == null) {
         return null;
      }

      startIndexInclusive = max0(startIndexInclusive);
      endIndexExclusive = Math.min(endIndexExclusive, array.length);
      int newSize = endIndexExclusive - startIndexInclusive;
      return newSize <= 0 ? EMPTY_LONG_ARRAY : arraycopy(array, startIndexInclusive, 0, newSize, x$0 -> new long[x$0]);
   }

   public static short[] subarray(short[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array == null) {
         return null;
      }

      startIndexInclusive = max0(startIndexInclusive);
      endIndexExclusive = Math.min(endIndexExclusive, array.length);
      int newSize = endIndexExclusive - startIndexInclusive;
      return newSize <= 0 ? EMPTY_SHORT_ARRAY : arraycopy(array, startIndexInclusive, 0, newSize, x$0 -> new short[x$0]);
   }

   public static <T> T[] subarray(T[] array, int startIndexInclusive, int endIndexExclusive) {
      if (array == null) {
         return null;
      }

      startIndexInclusive = max0(startIndexInclusive);
      endIndexExclusive = Math.min(endIndexExclusive, array.length);
      int newSize = endIndexExclusive - startIndexInclusive;
      Class<T> type = getComponentType(array);
      return (T[])(newSize <= 0 ? newInstance(type, 0) : arraycopy(array, startIndexInclusive, 0, newSize, () -> newInstance(type, newSize)));
   }

   public static void swap(boolean[] array, int offset1, int offset2) {
      swap(array, offset1, offset2, 1);
   }

   public static void swap(boolean[] array, int offset1, int offset2, int len) {
      if (!isEmpty(array) && offset1 < array.length && offset2 < array.length) {
         offset1 = max0(offset1);
         offset2 = max0(offset2);
         len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);

         for (int i = 0; i < len; offset2++) {
            boolean aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            i++;
            offset1++;
         }
      }
   }

   public static void swap(byte[] array, int offset1, int offset2) {
      swap(array, offset1, offset2, 1);
   }

   public static void swap(byte[] array, int offset1, int offset2, int len) {
      if (!isEmpty(array) && offset1 < array.length && offset2 < array.length) {
         offset1 = max0(offset1);
         offset2 = max0(offset2);
         len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);

         for (int i = 0; i < len; offset2++) {
            byte aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            i++;
            offset1++;
         }
      }
   }

   public static void swap(char[] array, int offset1, int offset2) {
      swap(array, offset1, offset2, 1);
   }

   public static void swap(char[] array, int offset1, int offset2, int len) {
      if (!isEmpty(array) && offset1 < array.length && offset2 < array.length) {
         offset1 = max0(offset1);
         offset2 = max0(offset2);
         len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);

         for (int i = 0; i < len; offset2++) {
            char aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            i++;
            offset1++;
         }
      }
   }

   public static void swap(double[] array, int offset1, int offset2) {
      swap(array, offset1, offset2, 1);
   }

   public static void swap(double[] array, int offset1, int offset2, int len) {
      if (!isEmpty(array) && offset1 < array.length && offset2 < array.length) {
         offset1 = max0(offset1);
         offset2 = max0(offset2);
         len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);

         for (int i = 0; i < len; offset2++) {
            double aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            i++;
            offset1++;
         }
      }
   }

   public static void swap(float[] array, int offset1, int offset2) {
      swap(array, offset1, offset2, 1);
   }

   public static void swap(float[] array, int offset1, int offset2, int len) {
      if (!isEmpty(array) && offset1 < array.length && offset2 < array.length) {
         offset1 = max0(offset1);
         offset2 = max0(offset2);
         len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);

         for (int i = 0; i < len; offset2++) {
            float aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            i++;
            offset1++;
         }
      }
   }

   public static void swap(int[] array, int offset1, int offset2) {
      swap(array, offset1, offset2, 1);
   }

   public static void swap(int[] array, int offset1, int offset2, int len) {
      if (!isEmpty(array) && offset1 < array.length && offset2 < array.length) {
         offset1 = max0(offset1);
         offset2 = max0(offset2);
         len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);

         for (int i = 0; i < len; offset2++) {
            int aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            i++;
            offset1++;
         }
      }
   }

   public static void swap(long[] array, int offset1, int offset2) {
      swap(array, offset1, offset2, 1);
   }

   public static void swap(long[] array, int offset1, int offset2, int len) {
      if (!isEmpty(array) && offset1 < array.length && offset2 < array.length) {
         offset1 = max0(offset1);
         offset2 = max0(offset2);
         len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);

         for (int i = 0; i < len; offset2++) {
            long aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            i++;
            offset1++;
         }
      }
   }

   public static void swap(Object[] array, int offset1, int offset2) {
      swap(array, offset1, offset2, 1);
   }

   public static void swap(Object[] array, int offset1, int offset2, int len) {
      if (!isEmpty(array) && offset1 < array.length && offset2 < array.length) {
         offset1 = max0(offset1);
         offset2 = max0(offset2);
         len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);

         for (int i = 0; i < len; offset2++) {
            Object aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            i++;
            offset1++;
         }
      }
   }

   public static void swap(short[] array, int offset1, int offset2) {
      swap(array, offset1, offset2, 1);
   }

   public static void swap(short[] array, int offset1, int offset2, int len) {
      if (!isEmpty(array) && offset1 < array.length && offset2 < array.length) {
         offset1 = max0(offset1);
         offset2 = max0(offset2);
         if (offset1 != offset2) {
            len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);

            for (int i = 0; i < len; offset2++) {
               short aux = array[offset1];
               array[offset1] = array[offset2];
               array[offset2] = aux;
               i++;
               offset1++;
            }
         }
      }
   }

   public static <T> T[] toArray(T... items) {
      return items;
   }

   public static Map<Object, Object> toMap(Object[] array) {
      if (array == null) {
         return null;
      }

      Map<Object, Object> map = new HashMap<>((int)(array.length * 1.5));

      for (int i = 0; i < array.length; i++) {
         Object object = array[i];
         if (object instanceof Entry) {
            Entry<?, ?> entry = (Entry<?, ?>)object;
            map.put(entry.getKey(), entry.getValue());
         } else {
            if (!(object instanceof Object[])) {
               throw new IllegalArgumentException("Array element " + i + ", '" + object + "', is neither of type Map.Entry nor an Array");
            }

            Object[] entry = (Object[])object;
            if (entry.length < 2) {
               throw new IllegalArgumentException("Array element " + i + ", '" + object + "', has a length less than 2");
            }

            map.put(entry[0], entry[1]);
         }
      }

      return map;
   }

   public static Boolean[] toObject(boolean[] array) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_BOOLEAN_OBJECT_ARRAY;
      }

      Boolean[] result = new Boolean[array.length];
      return setAll(result, i -> array[i] ? Boolean.TRUE : Boolean.FALSE);
   }

   public static Byte[] toObject(byte[] array) {
      if (array == null) {
         return null;
      } else {
         return array.length == 0 ? EMPTY_BYTE_OBJECT_ARRAY : setAll(new Byte[array.length], i -> array[i]);
      }
   }

   public static Character[] toObject(char[] array) {
      if (array == null) {
         return null;
      } else {
         return array.length == 0 ? EMPTY_CHARACTER_OBJECT_ARRAY : setAll(new Character[array.length], i -> array[i]);
      }
   }

   public static Double[] toObject(double[] array) {
      if (array == null) {
         return null;
      } else {
         return array.length == 0 ? EMPTY_DOUBLE_OBJECT_ARRAY : setAll(new Double[array.length], i -> array[i]);
      }
   }

   public static Float[] toObject(float[] array) {
      if (array == null) {
         return null;
      } else {
         return array.length == 0 ? EMPTY_FLOAT_OBJECT_ARRAY : setAll(new Float[array.length], i -> array[i]);
      }
   }

   public static Integer[] toObject(int[] array) {
      if (array == null) {
         return null;
      } else {
         return array.length == 0 ? EMPTY_INTEGER_OBJECT_ARRAY : setAll(new Integer[array.length], i -> array[i]);
      }
   }

   public static Long[] toObject(long[] array) {
      if (array == null) {
         return null;
      } else {
         return array.length == 0 ? EMPTY_LONG_OBJECT_ARRAY : setAll(new Long[array.length], i -> array[i]);
      }
   }

   public static Short[] toObject(short[] array) {
      if (array == null) {
         return null;
      } else {
         return array.length == 0 ? EMPTY_SHORT_OBJECT_ARRAY : setAll(new Short[array.length], i -> array[i]);
      }
   }

   public static boolean[] toPrimitive(Boolean[] array) {
      return toPrimitive(array, false);
   }

   public static boolean[] toPrimitive(Boolean[] array, boolean valueForNull) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_BOOLEAN_ARRAY;
      }

      boolean[] result = new boolean[array.length];

      for (int i = 0; i < array.length; i++) {
         Boolean b = array[i];
         result[i] = b == null ? valueForNull : b;
      }

      return result;
   }

   public static byte[] toPrimitive(Byte[] array) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_BYTE_ARRAY;
      }

      byte[] result = new byte[array.length];

      for (int i = 0; i < array.length; i++) {
         result[i] = array[i];
      }

      return result;
   }

   public static byte[] toPrimitive(Byte[] array, byte valueForNull) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_BYTE_ARRAY;
      }

      byte[] result = new byte[array.length];

      for (int i = 0; i < array.length; i++) {
         Byte b = array[i];
         result[i] = b == null ? valueForNull : b;
      }

      return result;
   }

   public static char[] toPrimitive(Character[] array) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_CHAR_ARRAY;
      }

      char[] result = new char[array.length];

      for (int i = 0; i < array.length; i++) {
         result[i] = array[i];
      }

      return result;
   }

   public static char[] toPrimitive(Character[] array, char valueForNull) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_CHAR_ARRAY;
      }

      char[] result = new char[array.length];

      for (int i = 0; i < array.length; i++) {
         Character b = array[i];
         result[i] = b == null ? valueForNull : b;
      }

      return result;
   }

   public static double[] toPrimitive(Double[] array) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_DOUBLE_ARRAY;
      }

      double[] result = new double[array.length];

      for (int i = 0; i < array.length; i++) {
         result[i] = array[i];
      }

      return result;
   }

   public static double[] toPrimitive(Double[] array, double valueForNull) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_DOUBLE_ARRAY;
      }

      double[] result = new double[array.length];

      for (int i = 0; i < array.length; i++) {
         Double b = array[i];
         result[i] = b == null ? valueForNull : b;
      }

      return result;
   }

   public static float[] toPrimitive(Float[] array) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_FLOAT_ARRAY;
      }

      float[] result = new float[array.length];

      for (int i = 0; i < array.length; i++) {
         result[i] = array[i];
      }

      return result;
   }

   public static float[] toPrimitive(Float[] array, float valueForNull) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_FLOAT_ARRAY;
      }

      float[] result = new float[array.length];

      for (int i = 0; i < array.length; i++) {
         Float b = array[i];
         result[i] = b == null ? valueForNull : b;
      }

      return result;
   }

   public static int[] toPrimitive(Integer[] array) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_INT_ARRAY;
      }

      int[] result = new int[array.length];

      for (int i = 0; i < array.length; i++) {
         result[i] = array[i];
      }

      return result;
   }

   public static int[] toPrimitive(Integer[] array, int valueForNull) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_INT_ARRAY;
      }

      int[] result = new int[array.length];

      for (int i = 0; i < array.length; i++) {
         Integer b = array[i];
         result[i] = b == null ? valueForNull : b;
      }

      return result;
   }

   public static long[] toPrimitive(Long[] array) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_LONG_ARRAY;
      }

      long[] result = new long[array.length];

      for (int i = 0; i < array.length; i++) {
         result[i] = array[i];
      }

      return result;
   }

   public static long[] toPrimitive(Long[] array, long valueForNull) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_LONG_ARRAY;
      }

      long[] result = new long[array.length];

      for (int i = 0; i < array.length; i++) {
         Long b = array[i];
         result[i] = b == null ? valueForNull : b;
      }

      return result;
   }

   public static Object toPrimitive(Object array) {
      if (array == null) {
         return null;
      } else {
         Class<?> ct = array.getClass().getComponentType();
         Class<?> pt = ClassUtils.wrapperToPrimitive(ct);
         if (boolean.class.equals(pt)) {
            return toPrimitive((Boolean[])array);
         } else if (char.class.equals(pt)) {
            return toPrimitive((Character[])array);
         } else if (byte.class.equals(pt)) {
            return toPrimitive((Byte[])array);
         } else if (int.class.equals(pt)) {
            return toPrimitive((Integer[])array);
         } else if (long.class.equals(pt)) {
            return toPrimitive((Long[])array);
         } else if (short.class.equals(pt)) {
            return toPrimitive((Short[])array);
         } else if (double.class.equals(pt)) {
            return toPrimitive((Double[])array);
         } else {
            return float.class.equals(pt) ? toPrimitive((Float[])array) : array;
         }
      }
   }

   public static short[] toPrimitive(Short[] array) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_SHORT_ARRAY;
      }

      short[] result = new short[array.length];

      for (int i = 0; i < array.length; i++) {
         result[i] = array[i];
      }

      return result;
   }

   public static short[] toPrimitive(Short[] array, short valueForNull) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_SHORT_ARRAY;
      }

      short[] result = new short[array.length];

      for (int i = 0; i < array.length; i++) {
         Short b = array[i];
         result[i] = b == null ? valueForNull : b;
      }

      return result;
   }

   public static String toString(Object array) {
      return toString(array, "{}");
   }

   public static String toString(Object array, String stringIfNull) {
      return array == null ? stringIfNull : new ToStringBuilder(array, ToStringStyle.SIMPLE_STYLE).append(array).toString();
   }

   public static String[] toStringArray(Object[] array) {
      if (array == null) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_STRING_ARRAY;
      }

      String[] result = new String[array.length];

      for (int i = 0; i < array.length; i++) {
         result[i] = array[i].toString();
      }

      return result;
   }

   public static String[] toStringArray(Object[] array, String valueForNullElements) {
      if (null == array) {
         return null;
      }

      if (array.length == 0) {
         return EMPTY_STRING_ARRAY;
      }

      String[] result = new String[array.length];

      for (int i = 0; i < array.length; i++) {
         result[i] = Objects.toString(array[i], valueForNullElements);
      }

      return result;
   }
}
