package io.leangen.geantyref;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class VarMap {
   private final Map<TypeVariable, AnnotatedType> map = new HashMap<>();

   VarMap() {
   }

   VarMap(AnnotatedParameterizedType type) {
      do {
         Class<?> clazz = (Class<?>)((ParameterizedType)type.getType()).getRawType();
         AnnotatedType[] arguments = type.getAnnotatedActualTypeArguments();
         TypeVariable[] typeParameters = clazz.getTypeParameters();
         if (arguments.length != typeParameters.length) {
            throw new IllegalStateException(
               "The given type [" + type + "] is inconsistent: it has " + arguments.length + " arguments instead of " + typeParameters.length
            );
         }

         for (int i = 0; i < arguments.length; i++) {
            this.add(typeParameters[i], arguments[i]);
         }

         Type owner = ((ParameterizedType)type.getType()).getOwnerType();
         type = owner instanceof ParameterizedType ? (AnnotatedParameterizedType)GenericTypeReflector.annotate(owner) : null;
      } while (type != null);
   }

   VarMap(ParameterizedType type) {
      this((AnnotatedParameterizedType)GenericTypeReflector.annotate(type));
   }

   VarMap(TypeVariable[] variables, AnnotatedType[] values) {
      this.addAll(variables, values);
   }

   void add(TypeVariable variable, AnnotatedType value) {
      this.map.put(variable, value);
   }

   void addAll(TypeVariable[] variables, AnnotatedType[] values) {
      assert variables.length == values.length;

      for (int i = 0; i < variables.length; i++) {
         this.map.put(variables[i], values[i]);
      }
   }

   AnnotatedType map(AnnotatedType type) {
      return this.map(type, VarMap.MappingMode.EXACT);
   }

   AnnotatedType map(AnnotatedType type, VarMap.MappingMode mappingMode) {
      if (type.getType() instanceof Class) {
         return GenericTypeReflector.updateAnnotations(type, ((Class)type.getType()).getAnnotations());
      }

      if (type instanceof AnnotatedTypeVariable) {
         TypeVariable<?> tv = (TypeVariable<?>)type.getType();
         if (!this.map.containsKey(tv)) {
            if (mappingMode.equals(VarMap.MappingMode.ALLOW_INCOMPLETE)) {
               AnnotatedTypeVariable variable = (AnnotatedTypeVariable)type;
               AnnotatedType[] bounds = this.map(variable.getAnnotatedBounds(), mappingMode);
               Annotation[] merged = GenericTypeReflector.merge(variable.getAnnotations(), tv.getAnnotations());
               TypeVariableImpl v = new TypeVariableImpl<>(tv, merged, bounds);
               return new AnnotatedTypeVariableImpl(v, merged);
            } else {
               throw new UnresolvedTypeVariableException(tv);
            }
         } else {
            TypeVariable varFromClass = this.map.keySet().stream().filter(key -> key.equals(tv)).findFirst().get();
            Annotation[] merged = GenericTypeReflector.merge(
               type.getAnnotations(), tv.getAnnotations(), this.map.get(tv).getAnnotations(), varFromClass.getAnnotations()
            );
            return GenericTypeReflector.updateAnnotations(this.map.get(tv), merged);
         }
      } else if (type instanceof AnnotatedParameterizedType) {
         AnnotatedParameterizedType pType = (AnnotatedParameterizedType)type;
         ParameterizedType inner = (ParameterizedType)pType.getType();
         Class raw = (Class)inner.getRawType();
         AnnotatedType[] typeParameters = new AnnotatedType[raw.getTypeParameters().length];

         for (int i = 0; i < typeParameters.length; i++) {
            AnnotatedType typeParameter = this.map(pType.getAnnotatedActualTypeArguments()[i], mappingMode);
            typeParameters[i] = GenericTypeReflector.updateAnnotations(typeParameter, raw.getTypeParameters()[i].getAnnotations());
         }

         Type[] rawArgs = Arrays.stream(typeParameters).map(AnnotatedType::getType).toArray(Type[]::new);
         Type innerOwnerType = inner.getOwnerType() == null ? null : this.map(GenericTypeReflector.annotate(inner.getOwnerType()), mappingMode).getType();
         ParameterizedType newInner = new ParameterizedTypeImpl((Class<?>)inner.getRawType(), rawArgs, innerOwnerType);
         return new AnnotatedParameterizedTypeImpl(newInner, GenericTypeReflector.merge(pType.getAnnotations(), raw.getAnnotations()), typeParameters);
      } else if (!(type instanceof AnnotatedWildcardType)) {
         if (type instanceof AnnotatedArrayType) {
            return AnnotatedArrayTypeImpl.createArrayType(
               this.map(((AnnotatedArrayType)type).getAnnotatedGenericComponentType(), mappingMode), type.getAnnotations()
            );
         } else {
            throw new RuntimeException("Not implemented: mapping " + type.getClass() + " (" + type + ")");
         }
      } else {
         AnnotatedWildcardType wType = (AnnotatedWildcardType)type;
         AnnotatedType[] up = this.map(wType.getAnnotatedUpperBounds(), mappingMode);
         AnnotatedType[] lw = this.map(wType.getAnnotatedLowerBounds(), mappingMode);
         Type[] upperBounds;
         if (up != null && up.length != 0) {
            upperBounds = Arrays.stream(up).map(AnnotatedType::getType).toArray(Type[]::new);
         } else {
            upperBounds = ((WildcardType)wType.getType()).getUpperBounds();
         }

         WildcardType w = new WildcardTypeImpl(upperBounds, Arrays.stream(lw).map(AnnotatedType::getType).toArray(Type[]::new));
         return new AnnotatedWildcardTypeImpl(w, wType.getAnnotations(), lw, up);
      }
   }

   AnnotatedType[] map(AnnotatedType[] types) {
      return this.map(types, VarMap.MappingMode.EXACT);
   }

   AnnotatedType[] map(AnnotatedType[] types, VarMap.MappingMode mappingMode) {
      AnnotatedType[] result = new AnnotatedType[types.length];

      for (int i = 0; i < types.length; i++) {
         result[i] = this.map(types[i], mappingMode);
      }

      return result;
   }

   Type[] map(Type[] types) {
      AnnotatedType[] result = this.map(Arrays.stream(types).map(GenericTypeReflector::annotate).toArray(AnnotatedType[]::new));
      return Arrays.stream(result).map(AnnotatedType::getType).toArray(Type[]::new);
   }

   Type map(Type type) {
      return this.map(GenericTypeReflector.annotate(type)).getType();
   }

   public enum MappingMode {
      EXACT,
      ALLOW_INCOMPLETE;
   }
}
