package io.leangen.geantyref;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public abstract class TypeVisitor {
   private final Map<TypeVariable, AnnotatedTypeVariable> varCache = new IdentityHashMap<>();
   private final Map<TypeVisitor.AnnotatedCaptureCacheKey, AnnotatedType> captureCache = new HashMap<>();

   protected AnnotatedType visitParameterizedType(AnnotatedParameterizedType type) {
      AnnotatedType[] params = Arrays.stream(type.getAnnotatedActualTypeArguments())
         .map(param -> GenericTypeReflector.transform(param, this))
         .toArray(AnnotatedType[]::new);
      return GenericTypeReflector.replaceParameters(type, params);
   }

   protected AnnotatedType visitWildcardType(AnnotatedWildcardType type) {
      AnnotatedType[] lowerBounds = Arrays.stream(type.getAnnotatedLowerBounds())
         .map(bound -> GenericTypeReflector.transform(bound, this))
         .toArray(AnnotatedType[]::new);
      AnnotatedType[] upperBounds = Arrays.stream(type.getAnnotatedUpperBounds())
         .map(bound -> GenericTypeReflector.transform(bound, this))
         .toArray(AnnotatedType[]::new);
      WildcardType inner = new WildcardTypeImpl(
         upperBounds.length > 0 ? Arrays.stream(upperBounds).map(AnnotatedType::getType).toArray(Type[]::new) : new Type[]{Object.class},
         Arrays.stream(lowerBounds).map(AnnotatedType::getType).toArray(Type[]::new)
      );
      return new AnnotatedWildcardTypeImpl(inner, type.getAnnotations(), lowerBounds, upperBounds);
   }

   protected AnnotatedType visitVariable(AnnotatedTypeVariable type) {
      TypeVariable var = (TypeVariable)type.getType();
      if (this.varCache.containsKey(var)) {
         return this.varCache.get(var);
      }

      AnnotatedTypeVariableImpl variable = new AnnotatedTypeVariableImpl(var, type.getAnnotations());
      this.varCache.put(var, variable);
      AnnotatedType[] bounds = Arrays.stream(type.getAnnotatedBounds()).map(bound -> GenericTypeReflector.transform(bound, this)).toArray(AnnotatedType[]::new);
      variable.init(bounds);
      return variable;
   }

   protected AnnotatedType visitArray(AnnotatedArrayType type) {
      AnnotatedType componentType = GenericTypeReflector.transform(type.getAnnotatedGenericComponentType(), this);
      return new AnnotatedArrayTypeImpl(GenericArrayTypeImpl.createArrayType(componentType.getType()), type.getAnnotations(), componentType);
   }

   protected AnnotatedType visitCaptureType(AnnotatedCaptureType type) {
      TypeVisitor.AnnotatedCaptureCacheKey key = new TypeVisitor.AnnotatedCaptureCacheKey(type);
      if (this.captureCache.containsKey(key)) {
         return this.captureCache.get(key);
      }

      AnnotatedType[] lowerBounds = type.getAnnotatedLowerBounds();
      if (lowerBounds != null) {
         lowerBounds = Arrays.stream(lowerBounds).map(bound -> GenericTypeReflector.transform(bound, this)).toArray(AnnotatedType[]::new);
      }

      AnnotatedCaptureType annotatedCapture = new AnnotatedCaptureTypeImpl(
         (CaptureType)type.getType(), type.getAnnotatedWildcardType(), type.getAnnotatedTypeVariable(), lowerBounds, null, type.getAnnotations()
      );
      this.captureCache.put(key, annotatedCapture);
      AnnotatedType[] upperBounds = Arrays.stream(type.getAnnotatedUpperBounds())
         .map(bound -> GenericTypeReflector.transform(bound, this))
         .toArray(AnnotatedType[]::new);
      annotatedCapture.setAnnotatedUpperBounds(upperBounds);
      return annotatedCapture;
   }

   protected AnnotatedType visitClass(AnnotatedType type) {
      return type;
   }

   protected AnnotatedType visitUnmatched(AnnotatedType type) {
      return type;
   }

   private static class AnnotatedCaptureCacheKey {
      AnnotatedCaptureType capture;
      CaptureType raw;

      AnnotatedCaptureCacheKey(AnnotatedCaptureType capture) {
         this.capture = capture;
         this.raw = (CaptureType)capture.getType();
      }

      @Override
      public int hashCode() {
         return 127 * this.raw.getWildcardType().hashCode()
            ^ this.raw.getTypeVariable().hashCode()
            ^ GenericTypeReflector.hashCode(Arrays.stream(this.capture.getAnnotations()));
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }

         if (!(obj instanceof TypeVisitor.AnnotatedCaptureCacheKey)) {
            return false;
         }

         TypeVisitor.AnnotatedCaptureCacheKey that = (TypeVisitor.AnnotatedCaptureCacheKey)obj;
         return this.capture == that.capture
            || new GenericTypeReflector.CaptureCacheKey(this.raw).equals(new GenericTypeReflector.CaptureCacheKey(that.raw))
               && Arrays.equals(this.capture.getAnnotations(), that.capture.getAnnotations());
      }
   }
}
