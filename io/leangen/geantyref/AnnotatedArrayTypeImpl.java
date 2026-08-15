package io.leangen.geantyref;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;

class AnnotatedArrayTypeImpl extends AnnotatedTypeImpl implements AnnotatedArrayType {
   private final AnnotatedType componentType;

   AnnotatedArrayTypeImpl(Type type, Annotation[] annotations, AnnotatedType componentType) {
      super(type, annotations);
      this.componentType = componentType;
   }

   static AnnotatedArrayType createArrayType(AnnotatedType componentType, Annotation[] annotations) {
      return new AnnotatedArrayTypeImpl(GenericArrayTypeImpl.createArrayType(componentType.getType()), annotations, componentType);
   }

   @Override
   public AnnotatedType getAnnotatedGenericComponentType() {
      return this.componentType;
   }

   @Override
   public boolean equals(Object other) {
      return this == other
         ? true
         : other instanceof AnnotatedArrayType
            && super.equals(other)
            && this.componentType.equals(((AnnotatedArrayType)other).getAnnotatedGenericComponentType());
   }

   @Override
   public int hashCode() {
      return 127 * super.hashCode() ^ this.componentType.hashCode();
   }

   @Override
   public String toString() {
      return this.componentType.toString() + " " + this.annotationsString() + "[]";
   }
}
