package io.leangen.geantyref;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

class ParameterizedTypeImpl implements ParameterizedType {
   private final Class<?> rawType;
   private final Type[] actualTypeArguments;
   private final Type ownerType;

   ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments, Type ownerType) {
      this.rawType = rawType;
      this.actualTypeArguments = actualTypeArguments;
      this.ownerType = ownerType;
   }

   @Override
   public Type getRawType() {
      return this.rawType;
   }

   @Override
   public Type[] getActualTypeArguments() {
      return this.actualTypeArguments;
   }

   @Override
   public Type getOwnerType() {
      return this.ownerType;
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof ParameterizedType)) {
         return false;
      }

      ParameterizedType that = (ParameterizedType)other;
      return this == that
         || Objects.equals(this.ownerType, that.getOwnerType())
            && Objects.equals(this.rawType, that.getRawType())
            && Arrays.equals(this.actualTypeArguments, that.getActualTypeArguments());
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(this.actualTypeArguments) ^ Objects.hashCode(this.ownerType) ^ Objects.hashCode(this.rawType);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      String clazz = this.rawType.getName();
      if (this.ownerType != null) {
         sb.append(GenericTypeReflector.getTypeName(this.ownerType)).append('$');
         String prefix = this.ownerType instanceof ParameterizedType
            ? ((Class)((ParameterizedType)this.ownerType).getRawType()).getName() + '$'
            : ((Class)this.ownerType).getName() + '$';
         if (clazz.startsWith(prefix)) {
            clazz = clazz.substring(prefix.length());
         }
      }

      sb.append(clazz);
      if (this.actualTypeArguments.length != 0) {
         sb.append('<');

         for (int i = 0; i < this.actualTypeArguments.length; i++) {
            Type arg = this.actualTypeArguments[i];
            if (i != 0) {
               sb.append(", ");
            }

            sb.append(GenericTypeReflector.getTypeName(arg));
         }

         sb.append('>');
      }

      return sb.toString();
   }
}
