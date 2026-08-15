package com.dfsek.tectonic.api.config.template.dynamic;

import com.dfsek.tectonic.util.ClassAnnotatedTypeImpl;
import java.lang.reflect.AnnotatedType;

public class DynamicValue<T> {
   private final boolean isFinal;
   private final boolean isDefault;
   private final T defaultValue;
   private final Class<T> type;
   private final AnnotatedType annotatedType;
   private final String key;

   public DynamicValue(boolean isFinal, boolean isDefault, T defaultValue, Class<T> type, AnnotatedType annotatedType, String key) {
      this.isFinal = isFinal;
      this.isDefault = isDefault;
      this.defaultValue = defaultValue;
      this.type = type;
      this.annotatedType = annotatedType;
      this.key = key;
   }

   public Class<T> getType() {
      return this.type;
   }

   public AnnotatedType getAnnotatedType() {
      return this.annotatedType;
   }

   public T getDefaultValue() {
      return this.defaultValue;
   }

   public boolean isDefault() {
      return this.isDefault;
   }

   public boolean isFinal() {
      return this.isFinal;
   }

   public String getKey() {
      return this.key;
   }

   public static <T> DynamicValue.Builder<T> builder(String key, Class<T> type) {
      return new DynamicValue.Builder<>(type, key);
   }

   public static final class Builder<T> {
      private final Class<T> clazz;
      private AnnotatedType annotatedType;
      private boolean isFinal = false;
      private boolean isDefault = false;
      private T defaultValue = (T)null;
      private final String key;

      private Builder(Class<T> clazz, String key) {
         this.clazz = clazz;
         this.key = key;
      }

      public DynamicValue.Builder<T> annotatedType(AnnotatedType type) {
         this.annotatedType = type;
         return this;
      }

      public DynamicValue.Builder<T> setFinal() {
         this.isFinal = true;
         return this;
      }

      public DynamicValue.Builder<T> setDefault(T defaultValue) {
         this.defaultValue = defaultValue;
         this.isDefault = true;
         return this;
      }

      public DynamicValue<T> build() {
         if (this.annotatedType == null) {
            this.annotatedType = new ClassAnnotatedTypeImpl(this.clazz);
         }

         return new DynamicValue<>(this.isFinal, this.isDefault, this.defaultValue, this.clazz, this.annotatedType, this.key);
      }
   }
}
