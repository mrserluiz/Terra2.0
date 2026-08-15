package com.dfsek.terra.lib.commons.lang3.builder;

import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import com.dfsek.terra.lib.commons.lang3.ObjectUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class DiffBuilder<T> implements com.dfsek.terra.lib.commons.lang3.builder.Builder<DiffResult<T>> {
   static final String TO_STRING_FORMAT = "%s differs from %s";
   private final List<Diff<?>> diffs;
   private final boolean equals;
   private final T left;
   private final T right;
   private final ToStringStyle style;
   private final String toStringFormat;

   public static <T> DiffBuilder.Builder<T> builder() {
      return new DiffBuilder.Builder<>();
   }

   @Deprecated
   public DiffBuilder(T left, T right, ToStringStyle style) {
      this(left, right, style, true);
   }

   @Deprecated
   public DiffBuilder(T left, T right, ToStringStyle style, boolean testObjectsEquals) {
      this(left, right, style, testObjectsEquals, "%s differs from %s");
   }

   private DiffBuilder(T left, T right, ToStringStyle style, boolean testObjectsEquals, String toStringFormat) {
      this.left = Objects.requireNonNull(left, "left");
      this.right = Objects.requireNonNull(right, "right");
      this.diffs = new ArrayList<>();
      this.toStringFormat = toStringFormat;
      this.style = style != null ? style : ToStringStyle.DEFAULT_STYLE;
      this.equals = testObjectsEquals && Objects.equals(left, right);
   }

   private <F> DiffBuilder<T> add(String fieldName, Supplier<F> left, Supplier<F> right, Class<F> type) {
      this.diffs.add(new DiffBuilder.SDiff(fieldName, left, right, type));
      return this;
   }

   public DiffBuilder<T> append(String fieldName, boolean lhs, boolean rhs) {
      return !this.equals && lhs != rhs ? this.add(fieldName, () -> lhs, () -> rhs, Boolean.class) : this;
   }

   public DiffBuilder<T> append(String fieldName, boolean[] lhs, boolean[] rhs) {
      return !this.equals && !Arrays.equals(lhs, rhs)
         ? this.add(fieldName, () -> ArrayUtils.toObject(lhs), () -> ArrayUtils.toObject(rhs), Boolean[].class)
         : this;
   }

   public DiffBuilder<T> append(String fieldName, byte lhs, byte rhs) {
      return !this.equals && lhs != rhs ? this.add(fieldName, () -> lhs, () -> rhs, Byte.class) : this;
   }

   public DiffBuilder<T> append(String fieldName, byte[] lhs, byte[] rhs) {
      return !this.equals && !Arrays.equals(lhs, rhs)
         ? this.add(fieldName, () -> ArrayUtils.toObject(lhs), () -> ArrayUtils.toObject(rhs), Byte[].class)
         : this;
   }

   public DiffBuilder<T> append(String fieldName, char lhs, char rhs) {
      return !this.equals && lhs != rhs ? this.add(fieldName, () -> lhs, () -> rhs, Character.class) : this;
   }

   public DiffBuilder<T> append(String fieldName, char[] lhs, char[] rhs) {
      return !this.equals && !Arrays.equals(lhs, rhs)
         ? this.add(fieldName, () -> ArrayUtils.toObject(lhs), () -> ArrayUtils.toObject(rhs), Character[].class)
         : this;
   }

   public DiffBuilder<T> append(String fieldName, DiffResult<?> diffResult) {
      Objects.requireNonNull(diffResult, "diffResult");
      if (this.equals) {
         return this;
      }

      diffResult.getDiffs().forEach(diff -> this.append(fieldName + "." + diff.getFieldName(), diff.getLeft(), diff.getRight()));
      return this;
   }

   public DiffBuilder<T> append(String fieldName, double lhs, double rhs) {
      return !this.equals && Double.doubleToLongBits(lhs) != Double.doubleToLongBits(rhs) ? this.add(fieldName, () -> lhs, () -> rhs, Double.class) : this;
   }

   public DiffBuilder<T> append(String fieldName, double[] lhs, double[] rhs) {
      return !this.equals && !Arrays.equals(lhs, rhs)
         ? this.add(fieldName, () -> ArrayUtils.toObject(lhs), () -> ArrayUtils.toObject(rhs), Double[].class)
         : this;
   }

   public DiffBuilder<T> append(String fieldName, float lhs, float rhs) {
      return !this.equals && Float.floatToIntBits(lhs) != Float.floatToIntBits(rhs) ? this.add(fieldName, () -> lhs, () -> rhs, Float.class) : this;
   }

   public DiffBuilder<T> append(String fieldName, float[] lhs, float[] rhs) {
      return !this.equals && !Arrays.equals(lhs, rhs)
         ? this.add(fieldName, () -> ArrayUtils.toObject(lhs), () -> ArrayUtils.toObject(rhs), Float[].class)
         : this;
   }

   public DiffBuilder<T> append(String fieldName, int lhs, int rhs) {
      return !this.equals && lhs != rhs ? this.add(fieldName, () -> lhs, () -> rhs, Integer.class) : this;
   }

   public DiffBuilder<T> append(String fieldName, int[] lhs, int[] rhs) {
      return !this.equals && !Arrays.equals(lhs, rhs)
         ? this.add(fieldName, () -> ArrayUtils.toObject(lhs), () -> ArrayUtils.toObject(rhs), Integer[].class)
         : this;
   }

   public DiffBuilder<T> append(String fieldName, long lhs, long rhs) {
      return !this.equals && lhs != rhs ? this.add(fieldName, () -> lhs, () -> rhs, Long.class) : this;
   }

   public DiffBuilder<T> append(String fieldName, long[] lhs, long[] rhs) {
      return !this.equals && !Arrays.equals(lhs, rhs)
         ? this.add(fieldName, () -> ArrayUtils.toObject(lhs), () -> ArrayUtils.toObject(rhs), Long[].class)
         : this;
   }

   public DiffBuilder<T> append(String fieldName, Object lhs, Object rhs) {
      if (!this.equals && lhs != rhs) {
         Object test = lhs != null ? lhs : rhs;
         if (ObjectUtils.isArray(test)) {
            if (test instanceof boolean[]) {
               return this.append(fieldName, (boolean[])lhs, (boolean[])rhs);
            } else if (test instanceof byte[]) {
               return this.append(fieldName, (byte[])lhs, (byte[])rhs);
            } else if (test instanceof char[]) {
               return this.append(fieldName, (char[])lhs, (char[])rhs);
            } else if (test instanceof double[]) {
               return this.append(fieldName, (double[])lhs, (double[])rhs);
            } else if (test instanceof float[]) {
               return this.append(fieldName, (float[])lhs, (float[])rhs);
            } else if (test instanceof int[]) {
               return this.append(fieldName, (int[])lhs, (int[])rhs);
            } else if (test instanceof long[]) {
               return this.append(fieldName, (long[])lhs, (long[])rhs);
            } else {
               return test instanceof short[] ? this.append(fieldName, (short[])lhs, (short[])rhs) : this.append(fieldName, (Object[])lhs, (Object[])rhs);
            }
         } else {
            return Objects.equals(lhs, rhs) ? this : this.add(fieldName, () -> lhs, () -> rhs, Object.class);
         }
      } else {
         return this;
      }
   }

   public DiffBuilder<T> append(String fieldName, Object[] lhs, Object[] rhs) {
      return !this.equals && !Arrays.equals(lhs, rhs) ? this.add(fieldName, () -> lhs, () -> rhs, Object[].class) : this;
   }

   public DiffBuilder<T> append(String fieldName, short lhs, short rhs) {
      return !this.equals && lhs != rhs ? this.add(fieldName, () -> lhs, () -> rhs, Short.class) : this;
   }

   public DiffBuilder<T> append(String fieldName, short[] lhs, short[] rhs) {
      return !this.equals && !Arrays.equals(lhs, rhs)
         ? this.add(fieldName, () -> ArrayUtils.toObject(lhs), () -> ArrayUtils.toObject(rhs), Short[].class)
         : this;
   }

   public DiffResult<T> build() {
      return new DiffResult<>(this.left, this.right, this.diffs, this.style, this.toStringFormat);
   }

   T getLeft() {
      return this.left;
   }

   T getRight() {
      return this.right;
   }

   public static final class Builder<T> {
      private T left;
      private T right;
      private ToStringStyle style;
      private boolean testObjectsEquals = true;
      private String toStringFormat = "%s differs from %s";

      public DiffBuilder<T> build() {
         return new DiffBuilder<>(this.left, this.right, this.style, this.testObjectsEquals, this.toStringFormat);
      }

      public DiffBuilder.Builder<T> setLeft(T left) {
         this.left = left;
         return this;
      }

      public DiffBuilder.Builder<T> setRight(T right) {
         this.right = right;
         return this;
      }

      public DiffBuilder.Builder<T> setStyle(ToStringStyle style) {
         this.style = style != null ? style : ToStringStyle.DEFAULT_STYLE;
         return this;
      }

      public DiffBuilder.Builder<T> setTestObjectsEquals(boolean testObjectsEquals) {
         this.testObjectsEquals = testObjectsEquals;
         return this;
      }

      public DiffBuilder.Builder<T> setToStringFormat(String toStringFormat) {
         this.toStringFormat = toStringFormat != null ? toStringFormat : "%s differs from %s";
         return this;
      }
   }

   private static final class SDiff<T> extends Diff<T> {
      private static final long serialVersionUID = 1L;
      private final transient Supplier<T> leftSupplier;
      private final transient Supplier<T> rightSupplier;

      private SDiff(String fieldName, Supplier<T> leftSupplier, Supplier<T> rightSupplier, Class<T> type) {
         super(fieldName, type);
         this.leftSupplier = Objects.requireNonNull(leftSupplier);
         this.rightSupplier = Objects.requireNonNull(rightSupplier);
      }

      @Override
      public T getLeft() {
         return this.leftSupplier.get();
      }

      @Override
      public T getRight() {
         return this.rightSupplier.get();
      }
   }
}
