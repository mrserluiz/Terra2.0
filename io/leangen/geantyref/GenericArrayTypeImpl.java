package io.leangen.geantyref;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

class GenericArrayTypeImpl implements GenericArrayType {
   private final Type componentType;

   GenericArrayTypeImpl(Type componentType) {
      this.componentType = componentType;
   }

   static Class<?> createArrayType(Class<?> componentType) {
      return Array.newInstance(componentType, 0).getClass();
   }

   static Type createArrayType(Type componentType) {
      return componentType instanceof Class ? createArrayType((Class<?>)componentType) : new GenericArrayTypeImpl(componentType);
   }

   @Override
   public Type getGenericComponentType() {
      return this.componentType;
   }

   @Override
   public boolean equals(Object other) {
      return other instanceof GenericArrayType && Objects.equals(this.componentType, ((GenericArrayType)other).getGenericComponentType());
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.componentType);
   }

   @Override
   public String toString() {
      return this.componentType + "[]";
   }
}
