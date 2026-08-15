package com.dfsek.terra.lib.commons.lang3.builder;

import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import com.dfsek.terra.lib.commons.lang3.ObjectUtils;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

public class CompareToBuilder implements Builder<Integer> {
   private int comparison = 0;

   private static void reflectionAppend(Object lhs, Object rhs, Class<?> clazz, CompareToBuilder builder, boolean useTransients, String[] excludeFields) {
      Field[] fields = clazz.getDeclaredFields();
      AccessibleObject.setAccessible(fields, true);

      for (int i = 0; i < fields.length && builder.comparison == 0; i++) {
         Field field = fields[i];
         if (!ArrayUtils.contains(excludeFields, field.getName())
            && !field.getName().contains("$")
            && (useTransients || !Modifier.isTransient(field.getModifiers()))
            && !Modifier.isStatic(field.getModifiers())) {
            builder.append(Reflection.getUnchecked(field, lhs), Reflection.getUnchecked(field, rhs));
         }
      }
   }

   public static int reflectionCompare(Object lhs, Object rhs) {
      return reflectionCompare(lhs, rhs, false, null);
   }

   public static int reflectionCompare(Object lhs, Object rhs, boolean compareTransients) {
      return reflectionCompare(lhs, rhs, compareTransients, null);
   }

   public static int reflectionCompare(Object lhs, Object rhs, boolean compareTransients, Class<?> reflectUpToClass, String... excludeFields) {
      if (lhs == rhs) {
         return 0;
      }

      Objects.requireNonNull(lhs, "lhs");
      Objects.requireNonNull(rhs, "rhs");
      Class<?> lhsClazz = lhs.getClass();
      if (!lhsClazz.isInstance(rhs)) {
         throw new ClassCastException();
      }

      CompareToBuilder compareToBuilder = new CompareToBuilder();
      reflectionAppend(lhs, rhs, lhsClazz, compareToBuilder, compareTransients, excludeFields);

      while (lhsClazz.getSuperclass() != null && lhsClazz != reflectUpToClass) {
         lhsClazz = lhsClazz.getSuperclass();
         reflectionAppend(lhs, rhs, lhsClazz, compareToBuilder, compareTransients, excludeFields);
      }

      return compareToBuilder.toComparison();
   }

   public static int reflectionCompare(Object lhs, Object rhs, Collection<String> excludeFields) {
      return reflectionCompare(lhs, rhs, ReflectionToStringBuilder.toNoNullStringArray(excludeFields));
   }

   public static int reflectionCompare(Object lhs, Object rhs, String... excludeFields) {
      return reflectionCompare(lhs, rhs, false, null, excludeFields);
   }

   public CompareToBuilder append(boolean lhs, boolean rhs) {
      if (this.comparison != 0) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs) {
         this.comparison = 1;
      } else {
         this.comparison = -1;
      }

      return this;
   }

   public CompareToBuilder append(boolean[] lhs, boolean[] rhs) {
      if (this.comparison != 0) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs == null) {
         this.comparison = -1;
         return this;
      }

      if (rhs == null) {
         this.comparison = 1;
         return this;
      }

      if (lhs.length != rhs.length) {
         this.comparison = lhs.length < rhs.length ? -1 : 1;
         return this;
      }

      for (int i = 0; i < lhs.length && this.comparison == 0; i++) {
         this.append(lhs[i], rhs[i]);
      }

      return this;
   }

   public CompareToBuilder append(byte lhs, byte rhs) {
      if (this.comparison != 0) {
         return this;
      }

      this.comparison = Byte.compare(lhs, rhs);
      return this;
   }

   public CompareToBuilder append(byte[] lhs, byte[] rhs) {
      if (this.comparison != 0) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs == null) {
         this.comparison = -1;
         return this;
      }

      if (rhs == null) {
         this.comparison = 1;
         return this;
      }

      if (lhs.length != rhs.length) {
         this.comparison = lhs.length < rhs.length ? -1 : 1;
         return this;
      }

      for (int i = 0; i < lhs.length && this.comparison == 0; i++) {
         this.append(lhs[i], rhs[i]);
      }

      return this;
   }

   public CompareToBuilder append(char lhs, char rhs) {
      if (this.comparison != 0) {
         return this;
      }

      this.comparison = Character.compare(lhs, rhs);
      return this;
   }

   public CompareToBuilder append(char[] lhs, char[] rhs) {
      if (this.comparison != 0) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs == null) {
         this.comparison = -1;
         return this;
      }

      if (rhs == null) {
         this.comparison = 1;
         return this;
      }

      if (lhs.length != rhs.length) {
         this.comparison = lhs.length < rhs.length ? -1 : 1;
         return this;
      }

      for (int i = 0; i < lhs.length && this.comparison == 0; i++) {
         this.append(lhs[i], rhs[i]);
      }

      return this;
   }

   public CompareToBuilder append(double lhs, double rhs) {
      if (this.comparison != 0) {
         return this;
      }

      this.comparison = Double.compare(lhs, rhs);
      return this;
   }

   public CompareToBuilder append(double[] lhs, double[] rhs) {
      if (this.comparison != 0) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs == null) {
         this.comparison = -1;
         return this;
      }

      if (rhs == null) {
         this.comparison = 1;
         return this;
      }

      if (lhs.length != rhs.length) {
         this.comparison = lhs.length < rhs.length ? -1 : 1;
         return this;
      }

      for (int i = 0; i < lhs.length && this.comparison == 0; i++) {
         this.append(lhs[i], rhs[i]);
      }

      return this;
   }

   public CompareToBuilder append(float lhs, float rhs) {
      if (this.comparison != 0) {
         return this;
      }

      this.comparison = Float.compare(lhs, rhs);
      return this;
   }

   public CompareToBuilder append(float[] lhs, float[] rhs) {
      if (this.comparison != 0) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs == null) {
         this.comparison = -1;
         return this;
      }

      if (rhs == null) {
         this.comparison = 1;
         return this;
      }

      if (lhs.length != rhs.length) {
         this.comparison = lhs.length < rhs.length ? -1 : 1;
         return this;
      }

      for (int i = 0; i < lhs.length && this.comparison == 0; i++) {
         this.append(lhs[i], rhs[i]);
      }

      return this;
   }

   public CompareToBuilder append(int lhs, int rhs) {
      if (this.comparison != 0) {
         return this;
      }

      this.comparison = Integer.compare(lhs, rhs);
      return this;
   }

   public CompareToBuilder append(int[] lhs, int[] rhs) {
      if (this.comparison != 0) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs == null) {
         this.comparison = -1;
         return this;
      }

      if (rhs == null) {
         this.comparison = 1;
         return this;
      }

      if (lhs.length != rhs.length) {
         this.comparison = lhs.length < rhs.length ? -1 : 1;
         return this;
      }

      for (int i = 0; i < lhs.length && this.comparison == 0; i++) {
         this.append(lhs[i], rhs[i]);
      }

      return this;
   }

   public CompareToBuilder append(long lhs, long rhs) {
      if (this.comparison != 0) {
         return this;
      }

      this.comparison = Long.compare(lhs, rhs);
      return this;
   }

   public CompareToBuilder append(long[] lhs, long[] rhs) {
      if (this.comparison != 0) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs == null) {
         this.comparison = -1;
         return this;
      }

      if (rhs == null) {
         this.comparison = 1;
         return this;
      }

      if (lhs.length != rhs.length) {
         this.comparison = lhs.length < rhs.length ? -1 : 1;
         return this;
      }

      for (int i = 0; i < lhs.length && this.comparison == 0; i++) {
         this.append(lhs[i], rhs[i]);
      }

      return this;
   }

   public CompareToBuilder append(Object lhs, Object rhs) {
      return this.append(lhs, rhs, null);
   }

   public CompareToBuilder append(Object lhs, Object rhs, Comparator<?> comparator) {
      if (this.comparison != 0) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs == null) {
         this.comparison = -1;
         return this;
      }

      if (rhs == null) {
         this.comparison = 1;
         return this;
      }

      if (ObjectUtils.isArray(lhs)) {
         this.appendArray(lhs, rhs, comparator);
      } else if (comparator == null) {
         Comparable<Object> comparable = (Comparable<Object>)lhs;
         this.comparison = comparable.compareTo(rhs);
      } else {
         Comparator<Object> comparator2 = (Comparator<Object>)comparator;
         this.comparison = comparator2.compare(lhs, rhs);
      }

      return this;
   }

   public CompareToBuilder append(Object[] lhs, Object[] rhs) {
      return this.append(lhs, rhs, null);
   }

   public CompareToBuilder append(Object[] lhs, Object[] rhs, Comparator<?> comparator) {
      if (this.comparison != 0) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs == null) {
         this.comparison = -1;
         return this;
      }

      if (rhs == null) {
         this.comparison = 1;
         return this;
      }

      if (lhs.length != rhs.length) {
         this.comparison = lhs.length < rhs.length ? -1 : 1;
         return this;
      }

      for (int i = 0; i < lhs.length && this.comparison == 0; i++) {
         this.append(lhs[i], rhs[i], comparator);
      }

      return this;
   }

   public CompareToBuilder append(short lhs, short rhs) {
      if (this.comparison != 0) {
         return this;
      }

      this.comparison = Short.compare(lhs, rhs);
      return this;
   }

   public CompareToBuilder append(short[] lhs, short[] rhs) {
      if (this.comparison != 0) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs == null) {
         this.comparison = -1;
         return this;
      }

      if (rhs == null) {
         this.comparison = 1;
         return this;
      }

      if (lhs.length != rhs.length) {
         this.comparison = lhs.length < rhs.length ? -1 : 1;
         return this;
      }

      for (int i = 0; i < lhs.length && this.comparison == 0; i++) {
         this.append(lhs[i], rhs[i]);
      }

      return this;
   }

   private void appendArray(Object lhs, Object rhs, Comparator<?> comparator) {
      if (lhs instanceof long[]) {
         this.append((long[])lhs, (long[])rhs);
      } else if (lhs instanceof int[]) {
         this.append((int[])lhs, (int[])rhs);
      } else if (lhs instanceof short[]) {
         this.append((short[])lhs, (short[])rhs);
      } else if (lhs instanceof char[]) {
         this.append((char[])lhs, (char[])rhs);
      } else if (lhs instanceof byte[]) {
         this.append((byte[])lhs, (byte[])rhs);
      } else if (lhs instanceof double[]) {
         this.append((double[])lhs, (double[])rhs);
      } else if (lhs instanceof float[]) {
         this.append((float[])lhs, (float[])rhs);
      } else if (lhs instanceof boolean[]) {
         this.append((boolean[])lhs, (boolean[])rhs);
      } else {
         this.append((Object[])lhs, (Object[])rhs, comparator);
      }
   }

   public CompareToBuilder appendSuper(int superCompareTo) {
      if (this.comparison != 0) {
         return this;
      }

      this.comparison = superCompareTo;
      return this;
   }

   public Integer build() {
      return this.toComparison();
   }

   public int toComparison() {
      return this.comparison;
   }
}
