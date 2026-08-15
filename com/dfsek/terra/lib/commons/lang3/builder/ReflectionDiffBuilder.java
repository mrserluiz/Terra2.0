package com.dfsek.terra.lib.commons.lang3.builder;

import com.dfsek.terra.lib.commons.lang3.ArraySorter;
import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import com.dfsek.terra.lib.commons.lang3.reflect.FieldUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ReflectionDiffBuilder<T> implements com.dfsek.terra.lib.commons.lang3.builder.Builder<DiffResult<T>> {
   private final DiffBuilder<T> diffBuilder;
   private String[] excludeFieldNames;

   public static <T> ReflectionDiffBuilder.Builder<T> builder() {
      return new ReflectionDiffBuilder.Builder<>();
   }

   private static String[] toExcludeFieldNames(String[] excludeFieldNames) {
      return excludeFieldNames == null ? ArrayUtils.EMPTY_STRING_ARRAY : ArraySorter.sort(ReflectionToStringBuilder.toNoNullStringArray(excludeFieldNames));
   }

   private ReflectionDiffBuilder(DiffBuilder<T> diffBuilder, String[] excludeFieldNames) {
      this.diffBuilder = diffBuilder;
      this.excludeFieldNames = excludeFieldNames;
   }

   @Deprecated
   public ReflectionDiffBuilder(T left, T right, ToStringStyle style) {
      this(DiffBuilder.<T>builder().setLeft(left).setRight(right).setStyle(style).build(), null);
   }

   private boolean accept(Field field) {
      if (field.getName().indexOf(36) != -1) {
         return false;
      } else if (Modifier.isTransient(field.getModifiers())) {
         return false;
      } else if (Modifier.isStatic(field.getModifiers())) {
         return false;
      } else {
         return this.excludeFieldNames != null && Arrays.binarySearch(this.excludeFieldNames, field.getName()) >= 0
            ? false
            : !field.isAnnotationPresent(DiffExclude.class);
      }
   }

   private void appendFields(Class<?> clazz) {
      for (Field field : FieldUtils.getAllFields(clazz)) {
         if (this.accept(field)) {
            try {
               this.diffBuilder.append(field.getName(), this.readField(field, this.getLeft()), this.readField(field, this.getRight()));
            } catch (IllegalAccessException e) {
               throw new IllegalArgumentException("Unexpected IllegalAccessException: " + e.getMessage(), e);
            }
         }
      }
   }

   public DiffResult<T> build() {
      if (this.getLeft().equals(this.getRight())) {
         return this.diffBuilder.build();
      }

      this.appendFields(this.getLeft().getClass());
      return this.diffBuilder.build();
   }

   public String[] getExcludeFieldNames() {
      return (String[])this.excludeFieldNames.clone();
   }

   private T getLeft() {
      return this.diffBuilder.getLeft();
   }

   private T getRight() {
      return this.diffBuilder.getRight();
   }

   private Object readField(Field field, Object target) throws IllegalAccessException {
      return FieldUtils.readField(field, target, true);
   }

   @Deprecated
   public ReflectionDiffBuilder<T> setExcludeFieldNames(String... excludeFieldNames) {
      this.excludeFieldNames = toExcludeFieldNames(excludeFieldNames);
      return this;
   }

   public static final class Builder<T> {
      private String[] excludeFieldNames = ArrayUtils.EMPTY_STRING_ARRAY;
      private DiffBuilder<T> diffBuilder;

      public ReflectionDiffBuilder<T> build() {
         return new ReflectionDiffBuilder<>(this.diffBuilder, this.excludeFieldNames);
      }

      public ReflectionDiffBuilder.Builder<T> setDiffBuilder(DiffBuilder<T> diffBuilder) {
         this.diffBuilder = diffBuilder;
         return this;
      }

      public ReflectionDiffBuilder.Builder<T> setExcludeFieldNames(String... excludeFieldNames) {
         this.excludeFieldNames = ReflectionDiffBuilder.toExcludeFieldNames(excludeFieldNames);
         return this;
      }
   }
}
