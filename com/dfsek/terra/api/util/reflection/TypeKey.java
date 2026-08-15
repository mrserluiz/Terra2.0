package com.dfsek.terra.api.util.reflection;

import com.dfsek.tectonic.util.ClassAnnotatedTypeImpl;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeKey<T> {
   final Class<? super T> rawType;
   final Type type;
   final AnnotatedType annotatedType;
   final int hashCode;

   protected TypeKey() {
      this.type = getSuperclassTypeParameter(this.getClass());
      this.annotatedType = getAnnotatedSuperclassTypeParameter(this.getClass());
      this.rawType = (Class<? super T>)ReflectionUtil.getRawType(this.type);
      this.hashCode = this.type.hashCode();
   }

   protected TypeKey(Class<T> clazz) {
      this.type = clazz;
      this.rawType = clazz;
      this.annotatedType = new ClassAnnotatedTypeImpl(clazz);
      this.hashCode = this.type.hashCode();
   }

   public static <T> TypeKey<T> of(Class<T> clazz) {
      return new TypeKey<>(clazz);
   }

   private static Type getSuperclassTypeParameter(Class<?> subclass) {
      Type superclass = subclass.getGenericSuperclass();
      if (superclass instanceof Class) {
         throw new RuntimeException("Missing type parameter.");
      }

      ParameterizedType parameterized = (ParameterizedType)superclass;
      return parameterized.getActualTypeArguments()[0];
   }

   private static AnnotatedType getAnnotatedSuperclassTypeParameter(Class<?> subclass) {
      AnnotatedType superclass = subclass.getAnnotatedSuperclass();
      if (superclass.getType() instanceof Class) {
         throw new RuntimeException("Missing type parameter.");
      }

      AnnotatedParameterizedType parameterized = (AnnotatedParameterizedType)superclass;
      return parameterized.getAnnotatedActualTypeArguments()[0];
   }

   public final Class<? super T> getRawType() {
      return this.rawType;
   }

   public final Type getType() {
      return this.type;
   }

   public final AnnotatedType getAnnotatedType() {
      return this.annotatedType;
   }

   @Override
   public final int hashCode() {
      return this.hashCode;
   }

   @Override
   public final boolean equals(Object o) {
      return o instanceof TypeKey && ReflectionUtil.equals(this.type, ((TypeKey)o).type);
   }

   @Override
   public final String toString() {
      return ReflectionUtil.typeToString(this.type);
   }
}
